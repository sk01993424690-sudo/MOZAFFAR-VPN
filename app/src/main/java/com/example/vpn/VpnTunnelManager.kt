package com.example.vpn

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Build
import android.provider.Settings
import com.example.BuildConfig
import com.example.data.DnsProviderOption
import com.example.data.ProvisionTunnelRequest
import com.example.data.TunnelTelemetry
import com.example.data.VpnConnectionState
import com.example.data.VpnErrorType
import com.example.data.VpnNetworkModule
import com.example.data.VpnPreferencesRepository
import com.example.data.VpnServerLocation
import com.wireguard.android.backend.BackendException
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.net.Inet6Address
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VpnTunnelManager(
    private val context: Context,
    private val prefsRepo: VpnPreferencesRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val configHelper = WireGuardConfigHelper(prefsRepo)
    private val backend: GoBackend by lazy { GoBackend(context.applicationContext) }

    private val _connectionState = MutableStateFlow<VpnConnectionState>(VpnConnectionState.Disconnected)
    val connectionState: StateFlow<VpnConnectionState> = _connectionState.asStateFlow()

    private val _telemetry = MutableStateFlow(TunnelTelemetry())
    val telemetry: StateFlow<TunnelTelemetry> = _telemetry.asStateFlow()

    private var activeTunnel: MozaffarTunnel? = null
    private var lastConfig: Config? = null
    private var lastServer: VpnServerLocation? = null
    private var userRequestedDisconnect = false
    private var telemetryJob: Job? = null
    private var reconnectJob: Job? = null
    private var connectJob: Job? = null

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            scope.launch {
                val state = _connectionState.value
                val prefs = prefsRepo.preferencesFlow.first()
                if (state is VpnConnectionState.Reconnecting && prefs.autoReconnectEnabled && !userRequestedDisconnect) {
                    attemptAutoReconnect()
                }
            }
        }

        override fun onLost(network: Network) {
            scope.launch {
                val state = _connectionState.value
                val prefs = prefsRepo.preferencesFlow.first()
                if (state is VpnConnectionState.Connected && prefs.autoReconnectEnabled && !userRequestedDisconnect) {
                    val server = lastServer ?: return@launch
                    _connectionState.value = VpnConnectionState.Reconnecting(server.id, 1)
                    VpnMonitorService.startOrUpdate(context, server.countryCode, reconnecting = true)
                    attemptAutoReconnect()
                }
            }
        }
    }

    init {
        runCatching { connectivityManager.registerDefaultNetworkCallback(networkCallback) }
    }

    fun getDevicePublicKeyBase64(): String =
        configHelper.getOrCreateDeviceKeyPair().publicKey.toBase64()

    fun regenerateDeviceKeyPair(): String =
        configHelper.regenerateDeviceKeyPair().publicKey.toBase64()

    fun validateAndSaveImportedConfig(serverId: String, rawConfig: String): Result<String> {
        val sanitized = configHelper.sanitizeConfigText(rawConfig)
        val parsed = configHelper.parseAndValidateConfig(sanitized)
        return parsed.map {
            prefsRepo.saveServerConfigText(serverId, sanitized)
            configHelper.extractEndpointDisplay(sanitized) ?: "WireGuard"
        }
    }

    fun prepareVpnIntent(): Intent? = VpnService.prepare(context)

    @Suppress("DEPRECATION")
    fun isNetworkOnline(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork != null) {
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            if (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return true
            }
        }
        return runCatching {
            connectivityManager.allNetworks.any { net ->
                connectivityManager.getNetworkCapabilities(net)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            }
        }.getOrDefault(false)
    }

    fun isSystemLockdownEnabled(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        val hasVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        return hasVpn && !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
    }

    fun openSystemVpnSettings() {
        val intent = Intent(Settings.ACTION_VPN_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun connect(server: VpnServerLocation) {
        if (VpnService.prepare(context) != null) {
            _connectionState.value = VpnConnectionState.Error(VpnErrorType.PERMISSION_DENIED)
            return
        }
        if (!isNetworkOnline()) {
            _connectionState.value = VpnConnectionState.Error(VpnErrorType.NETWORK_OFFLINE)
            return
        }

        connectJob?.cancel()
        reconnectJob?.cancel()
        userRequestedDisconnect = false
        lastServer = server
        _connectionState.value = VpnConnectionState.Connecting(server.id)

        connectJob = scope.launch {
            val prefs = prefsRepo.preferencesFlow.first()
            val configResult = resolveRealWireGuardConfig(
                server = server,
                dnsLeakProtection = prefs.dnsLeakProtectionEnabled,
                dnsProvider = prefs.dnsProvider
            )

            configResult.onFailure { err ->
                val errorType = when (err) {
                    is MissingConfigException -> VpnErrorType.NO_SERVER_CONFIG
                    is ProvisioningException -> VpnErrorType.PROVISIONING_FAILED
                    else -> VpnErrorType.TUNNEL_START_FAILED
                }
                _connectionState.value = VpnConnectionState.Error(errorType, formatErrorDetail(err))
                return@launch
            }

            val config = configResult.getOrThrow()
            lastConfig = config
            establishTunnel(server, config)
        }
    }

    private suspend fun resolveRealWireGuardConfig(
        server: VpnServerLocation,
        dnsLeakProtection: Boolean,
        dnsProvider: DnsProviderOption
    ): Result<Config> = withContext(Dispatchers.IO) {
        // 1. Prefer user-imported real WireGuard .conf profile for this server
        val savedRaw = prefsRepo.getServerConfigText(server.id)
        if (!savedRaw.isNullOrBlank()) {
            return@withContext configHelper.parseAndValidateConfig(savedRaw).map { base ->
                configHelper.applySecurityPolicies(base, dnsLeakProtection, dnsProvider)
            }
        }

        // 2. Otherwise request authenticated provisioning from real VPN control-plane API
        val api = VpnNetworkModule.createControlPlaneApi()
            ?: return@withContext Result.failure(
                MissingConfigException("No WireGuard .conf profile or configured VPN_API_BASE_URL")
            )

        return@withContext runCatching {
            val keyPair = configHelper.getOrCreateDeviceKeyPair()
            val response = api.provisionTunnel(
                bearerToken = "Bearer ${BuildConfig.VPN_API_TOKEN}",
                request = ProvisionTunnelRequest(
                    regionId = server.id,
                    countryCode = server.countryCode,
                    clientPublicKey = keyPair.publicKey.toBase64(),
                    dnsProvider = dnsProvider.id
                )
            )
            configHelper.buildConfigFromProvisionResponse(
                response = response,
                deviceKeyPair = keyPair,
                dnsLeakProtection = dnsLeakProtection,
                dnsProvider = dnsProvider
            )
        }.recoverCatching { cause ->
            throw ProvisioningException(cause.message ?: "Provisioning request failed", cause)
        }
    }

    private suspend fun establishTunnel(server: VpnServerLocation, config: Config) {
        val previousTunnel = activeTunnel
        activeTunnel = null
        if (previousTunnel != null) {
            withContext(Dispatchers.IO) {
                runCatching { backend.setState(previousTunnel, Tunnel.State.DOWN, null) }
            }
        }

        // WireGuard interface names must stay <= 15 alphanumeric/underscore chars
        val tunnelName = "mozaffar_${server.countryCode.lowercase()}"
        val tunnel = MozaffarTunnel(tunnelName) { state ->
            scope.launch {
                handleTunnelStateCallback(server, state)
            }
        }
        activeTunnel = tunnel

        var effectiveConfig = config
        val result = withContext(Dispatchers.IO) {
            runCatching {
                startBackendTunnel(tunnel, effectiveConfig)
            }.recoverCatching { firstError ->
                // If an IPv6 route (::/0) fails on an IPv4-only device/emulator without IPv6 interface address,
                // retry once with IPv4-only AllowedIPs.
                val hasIpv6Address = effectiveConfig.`interface`.addresses.any { it.address is Inet6Address }
                val hasIpv6Route = effectiveConfig.peers.any { peer ->
                    peer.allowedIps.any { it.address is Inet6Address }
                }
                if (!hasIpv6Address && hasIpv6Route) {
                    val prefs = prefsRepo.preferencesFlow.first()
                    effectiveConfig = configHelper.applySecurityPolicies(
                        baseConfig = effectiveConfig,
                        dnsLeakProtection = prefs.dnsLeakProtectionEnabled,
                        dnsProvider = prefs.dnsProvider,
                        forceIpv4OnlyRoutes = true
                    )
                    lastConfig = effectiveConfig
                    startBackendTunnel(tunnel, effectiveConfig)
                } else {
                    throw firstError
                }
            }
        }

        result.onFailure { err ->
            activeTunnel = null
            _connectionState.value = VpnConnectionState.Error(
                VpnErrorType.TUNNEL_START_FAILED,
                formatErrorDetail(err)
            )
            VpnMonitorService.stop(context)
        }.onSuccess {
            val connectedAt = System.currentTimeMillis()
            val tunnelAddress = effectiveConfig.`interface`.addresses.firstOrNull()?.toString() ?: "10.0.0.2/32"
            val dnsList = effectiveConfig.`interface`.dnsServers.map { it.hostAddress ?: it.toString() }
            val endpoint = effectiveConfig.peers.firstOrNull()?.endpoint?.map { it.toString() }?.orElse(null)

            _connectionState.value = VpnConnectionState.Connected(
                serverId = server.id,
                connectedAtEpochMs = connectedAt,
                tunnelAddress = tunnelAddress
            )
            VpnMonitorService.startOrUpdate(context, server.countryCode, reconnecting = false)
            startTelemetryLoop(tunnel, connectedAt, dnsList, endpoint)
        }
    }

    private fun startBackendTunnel(tunnel: MozaffarTunnel, config: Config): Tunnel.State {
        val finalState = backend.setState(tunnel, Tunnel.State.UP, config)
        val verifiedState = backend.getState(tunnel)
        require(finalState == Tunnel.State.UP && verifiedState == Tunnel.State.UP) {
            "WireGuard GoBackend failed to enter UP state (state=$verifiedState)"
        }
        return verifiedState
    }

    private fun formatErrorDetail(err: Throwable): String? = when (err) {
        is BackendException -> {
            val args = err.format.joinToString(", ") { it.toString() }
            if (args.isNotBlank()) "${err.reason.name}: $args" else err.reason.name
        }
        else -> err.message ?: err.cause?.message ?: err::class.simpleName
    }

    private suspend fun handleTunnelStateCallback(server: VpnServerLocation, state: Tunnel.State) {
        if (state == Tunnel.State.DOWN && !userRequestedDisconnect) {
            val current = _connectionState.value
            if (current is VpnConnectionState.Connected) {
                val prefs = prefsRepo.preferencesFlow.first()
                if (prefs.autoReconnectEnabled) {
                    _connectionState.value = VpnConnectionState.Reconnecting(server.id, 1)
                    VpnMonitorService.startOrUpdate(context, server.countryCode, reconnecting = true)
                    attemptAutoReconnect()
                } else {
                    teardownStateOnly()
                }
            }
        }
    }

    private fun attemptAutoReconnect() {
        val server = lastServer ?: return
        val config = lastConfig ?: return
        if (reconnectJob?.isActive == true) return

        reconnectJob = scope.launch {
            val backoffDelaysMs = listOf(1500L, 3000L, 6000L)
            for ((index, waitMs) in backoffDelaysMs.withIndex()) {
                if (userRequestedDisconnect) return@launch
                _connectionState.value = VpnConnectionState.Reconnecting(server.id, index + 1)
                delay(waitMs)
                if (!isNetworkOnline()) continue
                if (VpnService.prepare(context) != null) {
                    _connectionState.value = VpnConnectionState.Error(VpnErrorType.PERMISSION_DENIED)
                    return@launch
                }
                establishTunnel(server, config)
                if (_connectionState.value is VpnConnectionState.Connected) {
                    return@launch
                }
            }
            if (!userRequestedDisconnect && _connectionState.value !is VpnConnectionState.Connected) {
                _connectionState.value = VpnConnectionState.Error(
                    VpnErrorType.TUNNEL_START_FAILED,
                    "Auto-reconnect exhausted after 3 attempts"
                )
                VpnMonitorService.stop(context)
            }
        }
    }

    private fun startTelemetryLoop(
        tunnel: MozaffarTunnel,
        connectedAtEpochMs: Long,
        dnsList: List<String>,
        endpoint: String?
    ) {
        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            var prevRx = 0L
            var prevTx = 0L
            while (isActive && _connectionState.value is VpnConnectionState.Connected) {
                val stats = withContext(Dispatchers.IO) {
                    runCatching { backend.getStatistics(tunnel) }.getOrNull()
                }
                val sessionRx = stats?.totalRx() ?: prevRx
                val sessionTx = stats?.totalTx() ?: prevTx
                val rxDelta = (sessionRx - prevRx).coerceAtLeast(0L)
                val txDelta = (sessionTx - prevTx).coerceAtLeast(0L)
                prevRx = sessionRx
                prevTx = sessionTx

                if (rxDelta > 0L || txDelta > 0L) {
                    prefsRepo.addTrafficUsage(rxDelta, txDelta)
                }
                val prefs = prefsRepo.preferencesFlow.first()
                val durationSec = ((System.currentTimeMillis() - connectedAtEpochMs) / 1000L).coerceAtLeast(0L)

                _telemetry.value = TunnelTelemetry(
                    durationSeconds = durationSec,
                    downloadBytesPerSec = rxDelta,
                    uploadBytesPerSec = txDelta,
                    sessionRxBytes = sessionRx,
                    sessionTxBytes = sessionTx,
                    totalRxBytes = prefs.totalRxBytes,
                    totalTxBytes = prefs.totalTxBytes,
                    dnsServers = dnsList,
                    endpoint = endpoint
                )
                delay(1000L)
            }
        }
    }

    fun disconnect(userInitiated: Boolean = true) {
        userRequestedDisconnect = userInitiated
        connectJob?.cancel()
        reconnectJob?.cancel()
        telemetryJob?.cancel()

        val tunnelToStop = activeTunnel
        activeTunnel = null

        scope.launch {
            if (tunnelToStop != null) {
                withContext(Dispatchers.IO) {
                    runCatching { backend.setState(tunnelToStop, Tunnel.State.DOWN, null) }
                }
            }
            teardownStateOnly()
        }
    }

    private suspend fun teardownStateOnly() {
        telemetryJob?.cancel()
        VpnMonitorService.stop(context)
        val prefs = prefsRepo.preferencesFlow.first()
        _telemetry.value = TunnelTelemetry(
            totalRxBytes = prefs.totalRxBytes,
            totalTxBytes = prefs.totalTxBytes
        )
        _connectionState.value = VpnConnectionState.Disconnected
    }

    private class MissingConfigException(message: String) : Exception(message)
    private class ProvisioningException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
