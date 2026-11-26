package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.FormatUtils
import com.example.data.ServerConfigSource
import com.example.data.ThemeMode
import com.example.data.VpnConnectionState
import com.example.data.VpnErrorType
import com.example.localization.LocalAppStrings
import com.example.monetization.AdMobBannerBar
import com.example.monetization.SponsoredBannerSection
import com.example.ui.VpnDashboardUiState
import com.example.ui.components.ConnectPowerButton
import com.example.ui.components.ConnectionStatusBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.PasteWireGuardConfigDialog
import com.example.ui.components.VpnTopBrandBar
import com.example.ui.theme.EmeraldSecure
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalVpnTokens
import com.example.ui.theme.RoyalViolet

@Composable
fun HomeScreen(
    uiState: VpnDashboardUiState,
    onConnectClick: () -> Unit,
    onOpenServers: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenVpnSettings: () -> Unit,
    onRefreshIp: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onWatchRewardedAd: (Activity?) -> Unit,
    onImportConfigAndConnect: (String, String) -> Result<String>,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val tokens = LocalVpnTokens.current
    val context = LocalContext.current
    val activity = context as? Activity
    val isAdFree = uiState.preferences.isAdFreeNow()
    var showQuickSetupDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.connectionState) {
        val state = uiState.connectionState
        if (state is VpnConnectionState.Error && state.errorType == VpnErrorType.NO_SERVER_CONFIG) {
            showQuickSetupDialog = true
        }
    }

    val quickFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            if (!text.isNullOrBlank()) {
                val res = onImportConfigAndConnect(uiState.selectedServer.id, text)
                if (res.isSuccess) {
                    Toast.makeText(context, strings.configSavedMessage, Toast.LENGTH_SHORT).show()
                    showQuickSetupDialog = false
                } else {
                    val detail = res.exceptionOrNull()?.message
                    val msg = if (detail.isNullOrBlank()) strings.invalidConfigError else "${strings.invalidConfigError} ($detail)"
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (showQuickSetupDialog) {
        val server = uiState.selectedServer
        val countryLabel = strings.countryNames[server.countryCode] ?: server.countryCode
        PasteWireGuardConfigDialog(
            serverLabel = countryLabel,
            devicePublicKey = uiState.devicePublicKey,
            confirmLabel = strings.saveAndConnect,
            onDismiss = { showQuickSetupDialog = false },
            onPickFileClick = { quickFilePickerLauncher.launch(arrayOf("*/*")) },
            onCopyPublicKey = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText("WireGuard Public Key", uiState.devicePublicKey)
                )
                Toast.makeText(context, strings.copiedToast, Toast.LENGTH_SHORT).show()
            },
            onSaveConfig = { rawText ->
                val result = onImportConfigAndConnect(server.id, rawText)
                if (result.isSuccess) {
                    Toast.makeText(context, strings.configSavedMessage, Toast.LENGTH_SHORT).show()
                    null
                } else {
                    val detail = result.exceptionOrNull()?.message
                    if (detail.isNullOrBlank()) strings.invalidConfigError else "${strings.invalidConfigError} ($detail)"
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VpnTopBrandBar(
                themeMode = uiState.preferences.themeMode,
                onToggleLanguage = onToggleLanguage,
                onToggleTheme = onToggleTheme
            )

            Spacer(modifier = Modifier.height(16.dp))

            ConnectionStatusBadge(state = uiState.connectionState)

            Spacer(modifier = Modifier.height(14.dp))

            ConnectPowerButton(
                connectionState = uiState.connectionState,
                onClick = onConnectClick
            )

            // Honest Error Card (shown if permission denied, no server config, or tunnel failure)
            val errorText = when {
                uiState.transientPermissionError -> strings.vpnPermissionDenied
                uiState.connectionState is VpnConnectionState.Error -> {
                    when (uiState.connectionState.errorType) {
                        VpnErrorType.PERMISSION_DENIED -> strings.vpnPermissionDenied
                        VpnErrorType.NO_SERVER_CONFIG -> strings.errNoServerConfig
                        VpnErrorType.PROVISIONING_FAILED -> strings.errProvisioningFailed
                        VpnErrorType.TUNNEL_START_FAILED -> {
                            val detail = uiState.connectionState.technicalDetail
                            if (detail.isNullOrBlank()) strings.errTunnelStartFailed
                            else "${strings.errTunnelStartFailed} ($detail)"
                        }
                        VpnErrorType.NETWORK_OFFLINE -> strings.errNetworkOffline
                    }
                }
                else -> null
            }

            AnimatedVisibility(visible = errorText != null) {
                errorText?.let { msg ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        borderColor = tokens.errorColor.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = tokens.errorColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (uiState.connectionState is VpnConnectionState.Error &&
                                    (uiState.connectionState.errorType == VpnErrorType.NO_SERVER_CONFIG ||
                                        uiState.connectionState.errorType == VpnErrorType.PROVISIONING_FAILED ||
                                        uiState.connectionState.errorType == VpnErrorType.TUNNEL_START_FAILED)
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { showQuickSetupDialog = true },
                                            modifier = Modifier.testTag("error_quick_setup_btn")
                                        ) {
                                            Text(strings.quickSetupButton)
                                        }
                                        TextButton(
                                            onClick = onOpenServers,
                                            modifier = Modifier.testTag("error_open_servers_btn")
                                        ) {
                                            Text(strings.switchServer)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Server Location Card
            val server = uiState.selectedServer
            val countryName = strings.countryNames[server.countryCode] ?: server.countryCode
            val cityName = strings.cityNames[server.countryCode] ?: ""
            val configLabel = when (server.configSource) {
                ServerConfigSource.IMPORTED_PROFILE -> strings.configSourceImported
                ServerConfigSource.PROVISIONING_API -> strings.configSourceApi
                ServerConfigSource.NONE -> strings.configSourceNone
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_server_card"),
                onClick = onOpenServers
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = server.flagEmoji,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.selectedLocationTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$countryName • $cityName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = server.pingMs?.let { "$it ms" } ?: "-- ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (server.pingMs != null) EmeraldSecure
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• $configLabel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (server.configSource == ServerConfigSource.NONE)
                                        tokens.reconnectingColor
                                    else tokens.cyanAccent
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strings.switchServer,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = strings.switchServer,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Public IP + Duration Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1.25f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Public,
                                contentDescription = null,
                                tint = tokens.cyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.currentIpTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onRefreshIp,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("refresh_ip_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = strings.refreshIp,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val ipText = when {
                        uiState.publicIpInfo.isLoading -> strings.ipChecking
                        !uiState.publicIpInfo.ipAddress.isNullOrBlank() -> uiState.publicIpInfo.ipAddress
                        else -> strings.ipUnavailable
                    }
                    Text(
                        text = ipText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val ispSub = listOfNotNull(
                        uiState.publicIpInfo.country,
                        uiState.publicIpInfo.isp
                    ).joinToString(" • ")
                    if (ispSub.isNotBlank()) {
                        Text(
                            text = ispSub,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                GlassCard(modifier = Modifier.weight(0.85f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = RoyalViolet,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.durationTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = FormatUtils.formatDuration(uiState.telemetry.durationSeconds),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.connectionState is VpnConnectionState.Connected)
                            uiState.connectionState.tunnelAddress
                        else "WireGuard",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Download / Upload Speed & Data Usage Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TelemetryMetricTile(
                        icon = Icons.Filled.ArrowDownward,
                        iconTint = EmeraldSecure,
                        title = strings.downloadSpeedTitle,
                        value = FormatUtils.formatSpeed(uiState.telemetry.downloadBytesPerSec),
                        subtitle = "${strings.sessionUsageLabel}: ${FormatUtils.formatBytes(uiState.telemetry.sessionRxBytes)}",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricTile(
                        icon = Icons.Filled.ArrowUpward,
                        iconTint = tokens.cyanAccent,
                        title = strings.uploadSpeedTitle,
                        value = FormatUtils.formatSpeed(uiState.telemetry.uploadBytesPerSec),
                        subtitle = "${strings.sessionUsageLabel}: ${FormatUtils.formatBytes(uiState.telemetry.sessionTxBytes)}",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = tokens.cardSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.dataUsageTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val totalAll = uiState.preferences.totalRxBytes + uiState.preferences.totalTxBytes
                        Text(
                            text = "${strings.totalUsageLabel}: ${FormatUtils.formatBytes(totalAll)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Security Protection Strip (DNS Leak Protection, Auto-Reconnect, Kill Switch)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.securityProtectionTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                SecurityIndicatorRow(
                    icon = Icons.Filled.Dns,
                    label = strings.dnsLeakProtection,
                    value = if (uiState.preferences.dnsLeakProtectionEnabled)
                        "${strings.activeLabel} (${uiState.preferences.dnsProvider.primaryDns})"
                    else strings.disabledLabel,
                    isPositive = uiState.preferences.dnsLeakProtectionEnabled
                )
                Spacer(modifier = Modifier.height(8.dp))
                SecurityIndicatorRow(
                    icon = Icons.Filled.Sync,
                    label = strings.autoReconnect,
                    value = if (uiState.preferences.autoReconnectEnabled)
                        strings.enabledLabel else strings.disabledLabel,
                    isPositive = uiState.preferences.autoReconnectEnabled
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (uiState.isSystemLockdownEnabled) EmeraldSecure else tokens.cyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.killSwitchLabel,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(
                        onClick = onOpenVpnSettings,
                        modifier = Modifier.testTag("kill_switch_settings_btn")
                    ) {
                        Text(
                            text = if (uiState.isSystemLockdownEnabled)
                                strings.systemManagedLabel else strings.configureKillSwitch,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Optional Rewarded Ad Card or Premium Promo (hidden if Pro subscribed)
            if (!uiState.preferences.isPremiumSubscribed) {
                Spacer(modifier = Modifier.height(12.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = RoyalViolet.copy(alpha = 0.45f)
                ) {
                    if (isAdFree) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = EmeraldSecure
                            )
                            Text(
                                text = strings.adFreePassActive,
                                style = MaterialTheme.typography.titleMedium,
                                color = EmeraldSecure
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.rewardedAdTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.rewardedAdDesc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = { onWatchRewardedAd(activity) },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalViolet),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("watch_rewarded_ad_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.watchAdButton)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            onClick = onOpenPremium,
                            shape = RoundedCornerShape(12.dp),
                            color = RoyalViolet.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, RoyalViolet.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_go_premium_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.WorkspacePremium,
                                        contentDescription = null,
                                        tint = RoyalViolet,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.premiumCardTitle,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                Text(
                                    text = strings.upgradeToPro,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = RoyalViolet,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (!isAdFree) {
                Spacer(modifier = Modifier.height(12.dp))
                SponsoredBannerSection(
                    isAdFree = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Non-intrusive bottom Banner Ad (only shown when not Ad-Free)
        AdMobBannerBar(
            isAdFree = isAdFree,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun TelemetryMetricTile(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = JetBrainsMonoFontFamily
            ),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SecurityIndicatorRow(
    icon: ImageVector,
    label: String,
    value: String,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPositive) EmeraldSecure else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = if (isPositive) EmeraldSecure else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}
