package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MozaffarVpnApp
import com.example.data.DefaultVpnServers
import com.example.data.DnsProviderOption
import com.example.data.PublicIpInfo
import com.example.data.ServerConfigSource
import com.example.data.ThemeMode
import com.example.data.TunnelTelemetry
import com.example.data.VpnConnectionState
import com.example.data.VpnErrorType
import com.example.data.VpnNetworkModule
import com.example.data.VpnServerLocation
import com.example.data.VpnUserPreferences
import com.example.localization.AppLanguage
import com.example.monetization.BillingUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class VpnDashboardUiState(
    val connectionState: VpnConnectionState = VpnConnectionState.Disconnected,
    val telemetry: TunnelTelemetry = TunnelTelemetry(),
    val preferences: VpnUserPreferences = VpnUserPreferences(),
    val servers: List<VpnServerLocation> = DefaultVpnServers.list,
    val selectedServer: VpnServerLocation = DefaultVpnServers.list.first(),
    val publicIpInfo: PublicIpInfo = PublicIpInfo(isLoading = true),
    val devicePublicKey: String = "",
    val isSystemLockdownEnabled: Boolean = false,
    val billingState: BillingUiState = BillingUiState(),
    val transientPermissionError: Boolean = false
)

class MozaffarVpnViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MozaffarVpnApp
    private val prefsRepo = app.preferencesRepository
    private val probeRepo = app.serverProbeRepository
    private val tunnelManager = app.vpnTunnelManager
    private val adMobManager = app.adMobManager
    private val billingManager = app.billingManager

    private val _serversState = MutableStateFlow(refreshConfigSources(DefaultVpnServers.list))
    private val _publicIpState = MutableStateFlow(PublicIpInfo(isLoading = true))
    private val _devicePublicKey = MutableStateFlow(tunnelManager.getDevicePublicKeyBase64())
    private val _lockdownActive = MutableStateFlow(tunnelManager.isSystemLockdownEnabled())
    private val _permissionError = MutableStateFlow(false)

    private var pendingConnectAfterPermission = false

    val uiState: StateFlow<VpnDashboardUiState> = combine(
        tunnelManager.connectionState,
        tunnelManager.telemetry,
        prefsRepo.preferencesFlow,
        _serversState,
        _publicIpState,
        _devicePublicKey,
        _lockdownActive,
        billingManager.billingState,
        _permissionError
    ) { values ->
        val connState = values[0] as VpnConnectionState
        val telemetry = values[1] as TunnelTelemetry
        val prefs = values[2] as VpnUserPreferences
        @Suppress("UNCHECKED_CAST")
        val servers = values[3] as List<VpnServerLocation>
        val ipInfo = values[4] as PublicIpInfo
        val pubKey = values[5] as String
        val lockdown = values[6] as Boolean
        val billing = values[7] as BillingUiState
        val permErr = values[8] as Boolean

        val selected = servers.firstOrNull { it.id == prefs.selectedServerId } ?: servers.first()
        VpnDashboardUiState(
            connectionState = connState,
            telemetry = telemetry,
            preferences = prefs,
            servers = servers,
            selectedServer = selected,
            publicIpInfo = ipInfo,
            devicePublicKey = pubKey,
            isSystemLockdownEnabled = lockdown,
            billingState = billing,
            transientPermissionError = permErr
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VpnDashboardUiState()
    )

    init {
        refreshPublicIp()
        refreshAllServerPings()
        viewModelScope.launch {
            tunnelManager.connectionState.collect { state ->
                _lockdownActive.value = tunnelManager.isSystemLockdownEnabled()
                if (state is VpnConnectionState.Connected || state is VpnConnectionState.Disconnected) {
                    refreshPublicIp()
                }
            }
        }
    }

    private fun refreshConfigSources(list: List<VpnServerLocation>): List<VpnServerLocation> {
        val apiConfigured = VpnNetworkModule.isControlPlaneConfigured()
        return list.map { server ->
            val hasImported = prefsRepo.hasServerConfig(server.id)
            val importedEndpoint = if (hasImported) {
                runCatching {
                    com.example.vpn.WireGuardConfigHelper(prefsRepo)
                        .extractEndpointDisplay(prefsRepo.getServerConfigText(server.id).orEmpty())
                }.getOrNull()
            } else null

            val source = when {
                hasImported -> ServerConfigSource.IMPORTED_PROFILE
                apiConfigured -> ServerConfigSource.PROVISIONING_API
                else -> ServerConfigSource.NONE
            }
            val hostFromImported = importedEndpoint?.substringBeforeLast(":")?.removeSurrounding("[", "]")
            server.copy(
                configSource = source,
                endpointDisplay = importedEndpoint,
                probeHost = hostFromImported?.takeIf { it.isNotBlank() } ?: server.probeHost
            )
        }
    }

    fun toggleVpnConnection(onNeedPermission: (Intent) -> Unit) {
        _permissionError.value = false
        when (uiState.value.connectionState) {
            is VpnConnectionState.Connected,
            is VpnConnectionState.Connecting,
            is VpnConnectionState.Reconnecting -> {
                tunnelManager.disconnect(userInitiated = true)
            }
            is VpnConnectionState.Disconnected,
            is VpnConnectionState.Error -> {
                val prepareIntent = tunnelManager.prepareVpnIntent()
                if (prepareIntent != null) {
                    pendingConnectAfterPermission = true
                    onNeedPermission(prepareIntent)
                } else {
                    tunnelManager.connect(uiState.value.selectedServer)
                }
            }
        }
    }

    fun onVpnPermissionResult(granted: Boolean) {
        if (granted && pendingConnectAfterPermission) {
            pendingConnectAfterPermission = false
            _permissionError.value = false
            tunnelManager.connect(uiState.value.selectedServer)
        } else {
            pendingConnectAfterPermission = false
            if (!granted) {
                _permissionError.value = true
            }
        }
    }

    fun clearPermissionError() {
        _permissionError.value = false
    }

    fun selectServer(serverId: String, activity: Activity?) {
        viewModelScope.launch {
            val wasConnected = uiState.value.connectionState is VpnConnectionState.Connected
            prefsRepo.setSelectedServer(serverId)
            if (wasConnected) {
                tunnelManager.disconnect(userInitiated = true)
            }
            val current = uiState.value
            val isBusy = current.connectionState is VpnConnectionState.Connecting ||
                current.connectionState is VpnConnectionState.Reconnecting
            adMobManager.maybeShowInterstitial(
                activity = activity,
                isAdFree = current.preferences.isAdFreeNow(),
                isVpnHandshakeInProgress = isBusy
            )
        }
    }

    fun refreshAllServerPings() {
        viewModelScope.launch {
            _serversState.update { list -> list.map { it.copy(isProbing = true) } }
            val snapshot = _serversState.value
            val results = probeRepo.probeAllServers(snapshot)
            _serversState.update { list ->
                list.map { server ->
                    val ping = results[server.id]
                    server.copy(
                        pingMs = ping,
                        isReachable = ping != null,
                        isProbing = false
                    )
                }
            }
        }
    }

    fun refreshPublicIp() {
        viewModelScope.launch {
            _publicIpState.update { it.copy(isLoading = true, error = null) }
            val result = withContext(Dispatchers.IO) {
                runCatching { VpnNetworkModule.publicIpApi.fetchPublicIp() }
            }
            result.onSuccess { data ->
                if (!data.ip.isNullOrBlank()) {
                    _publicIpState.value = PublicIpInfo(
                        ipAddress = data.ip,
                        country = data.country,
                        isp = data.connection?.isp ?: data.connection?.org,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _publicIpState.value = PublicIpInfo(isLoading = false, error = "No IP returned")
                }
            }.onFailure { err ->
                _publicIpState.value = PublicIpInfo(isLoading = false, error = err.message)
            }
        }
    }

    fun importConfigForServer(serverId: String, rawConfig: String): Result<String> {
        val result = tunnelManager.validateAndSaveImportedConfig(serverId, rawConfig)
        if (result.isSuccess) {
            _serversState.value = refreshConfigSources(_serversState.value)
            refreshAllServerPings()
        }
        return result
    }

    fun importConfigAndConnect(
        serverId: String,
        rawConfig: String,
        onNeedPermission: (Intent) -> Unit
    ): Result<String> {
        val result = importConfigForServer(serverId, rawConfig)
        if (result.isSuccess) {
            val targetServer = _serversState.value.firstOrNull { it.id == serverId }
                ?: uiState.value.selectedServer
            val prepareIntent = tunnelManager.prepareVpnIntent()
            if (prepareIntent != null) {
                pendingConnectAfterPermission = true
                onNeedPermission(prepareIntent)
            } else {
                tunnelManager.connect(targetServer)
            }
        }
        return result
    }

    fun clearConfigForServer(serverId: String) {
        prefsRepo.removeServerConfigText(serverId)
        _serversState.value = refreshConfigSources(
            DefaultVpnServers.list.map { def ->
                _serversState.value.firstOrNull { it.id == def.id }?.copy(probeHost = def.probeHost) ?: def
            }
        )
        refreshAllServerPings()
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsRepo.setLanguage(language) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setAutoReconnect(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setAutoReconnect(enabled) }
    }

    fun setDnsLeakProtection(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setDnsLeakProtection(enabled) }
    }

    fun setDnsProvider(provider: DnsProviderOption) {
        viewModelScope.launch { prefsRepo.setDnsProvider(provider) }
    }

    fun regenerateDeviceKeyPair() {
        _devicePublicKey.value = tunnelManager.regenerateDeviceKeyPair()
    }

    fun openSystemVpnSettings() {
        tunnelManager.openSystemVpnSettings()
    }

    fun refreshSystemLockdownStatus() {
        _lockdownActive.value = tunnelManager.isSystemLockdownEnabled()
    }

    fun showRewardedAdForPass(activity: Activity?, onUnavailable: () -> Unit) {
        adMobManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                viewModelScope.launch {
                    prefsRepo.grantAdFreePass(2 * 60 * 60 * 1000L)
                }
            },
            onUnavailable = onUnavailable
        )
    }

    fun subscribe(activity: Activity?, productId: String): Boolean =
        billingManager.launchSubscriptionFlow(activity, productId)

    fun restorePurchases() {
        billingManager.queryProductsAndPurchases()
    }
}
