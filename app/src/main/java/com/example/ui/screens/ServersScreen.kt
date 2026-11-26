package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ServerConfigSource
import com.example.data.VpnServerLocation
import com.example.localization.LocalAppStrings
import com.example.monetization.AdMobNativeCard
import com.example.monetization.SponsoredBannerSection
import com.example.ui.VpnDashboardUiState
import com.example.ui.components.GlassCard
import com.example.ui.components.PasteWireGuardConfigDialog
import com.example.ui.theme.EmeraldSecure
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalVpnTokens
import com.example.ui.theme.RoyalViolet

@Composable
fun ServersScreen(
    uiState: VpnDashboardUiState,
    onSelectServer: (String, Activity?) -> Unit,
    onRefreshPings: () -> Unit,
    onImportConfig: (String, String) -> Result<String>,
    onClearConfig: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val tokens = LocalVpnTokens.current
    val context = LocalContext.current
    val activity = context as? Activity
    val isAdFree = uiState.preferences.isAdFreeNow()

    var editingServer by remember { mutableStateOf<VpnServerLocation?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val target = editingServer
        if (uri != null && target != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            if (!text.isNullOrBlank()) {
                val res = onImportConfig(target.id, text)
                if (res.isSuccess) {
                    Toast.makeText(context, strings.configSavedMessage, Toast.LENGTH_SHORT).show()
                    editingServer = null
                } else {
                    val detail = res.exceptionOrNull()?.message
                    val msg = if (detail.isNullOrBlank()) strings.invalidConfigError else "${strings.invalidConfigError} ($detail)"
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    editingServer?.let { server ->
        val countryLabel = strings.countryNames[server.countryCode] ?: server.countryCode
        PasteWireGuardConfigDialog(
            serverLabel = countryLabel,
            devicePublicKey = uiState.devicePublicKey,
            onDismiss = { editingServer = null },
            onPickFileClick = { filePickerLauncher.launch(arrayOf("*/*")) },
            onCopyPublicKey = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText("WireGuard Public Key", uiState.devicePublicKey)
                )
                Toast.makeText(context, strings.copiedToast, Toast.LENGTH_SHORT).show()
            },
            onSaveConfig = { rawText ->
                val result = onImportConfig(server.id, rawText)
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
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.serversScreenTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.serversScreenSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = onRefreshPings,
                        modifier = Modifier.testTag("refresh_all_pings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = strings.refreshPing,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.refreshPing)
                    }
                }
            }

            // Local Curve25519 Device Public Key Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = RoyalViolet.copy(alpha = 0.4f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Key,
                                contentDescription = null,
                                tint = RoyalViolet,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.publicKeyCardTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("WireGuard Public Key", uiState.devicePublicKey)
                                )
                                Toast.makeText(context, strings.copiedToast, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("copy_wg_pubkey_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = strings.copyPublicKey,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.copyPublicKey)
                        }
                    }
                    Text(
                        text = uiState.devicePublicKey,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = tokens.cyanAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.publicKeyCardDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            itemsIndexed(uiState.servers, key = { _, item -> item.id }) { index, server ->
                val isSelected = server.id == uiState.selectedServer.id
                ServerLocationRow(
                    server = server,
                    isSelected = isSelected,
                    onSelect = { onSelectServer(server.id, activity) },
                    onConfigureClick = { editingServer = server },
                    onClearConfig = { onClearConfig(server.id) }
                )

                // Insert styled Native Ad gracefully after the 3rd server item
                if (index == 2 && !isAdFree) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AdMobNativeCard(isAdFree = false)
                }
            }

            if (!isAdFree) {
                item {
                    SponsoredBannerSection(isAdFree = false)
                }
            }
        }
    }
}

@Composable
private fun ServerLocationRow(
    server: VpnServerLocation,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onConfigureClick: () -> Unit,
    onClearConfig: () -> Unit
) {
    val strings = LocalAppStrings.current
    val tokens = LocalVpnTokens.current
    val countryName = strings.countryNames[server.countryCode] ?: server.countryCode
    val cityName = strings.cityNames[server.countryCode] ?: ""

    val borderColor = if (isSelected) RoyalViolet else tokens.cardBorder

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_card_${server.id}"),
        onClick = onSelect,
        borderColor = borderColor
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = countryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = buildString {
                            append(cityName)
                            if (!server.endpointDisplay.isNullOrBlank()) {
                                append(" • ")
                                append(server.endpointDisplay)
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val (statusText, statusColor) = when {
                            server.isProbing -> strings.measuringPing to MaterialTheme.colorScheme.onSurfaceVariant
                            server.isReachable == true -> strings.serverOnline to EmeraldSecure
                            server.isReachable == false -> strings.serverOffline to tokens.errorColor
                            else -> strings.measuringPing to MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        val configBadge = when (server.configSource) {
                            ServerConfigSource.IMPORTED_PROFILE -> strings.configSourceImported
                            ServerConfigSource.PROVISIONING_API -> strings.configSourceApi
                            ServerConfigSource.NONE -> strings.serverNeedsConfig
                        }
                        Text(
                            text = configBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (server.configSource == ServerConfigSource.NONE)
                                tokens.reconnectingColor
                            else tokens.cyanAccent
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (server.isProbing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.NetworkPing,
                            contentDescription = null,
                            tint = if (server.pingMs != null) EmeraldSecure
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = server.pingMs?.let { "$it ms" } ?: "-- ms",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.CheckCircle
                        else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = countryName,
                        tint = if (isSelected) RoyalViolet else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onConfigureClick,
                        modifier = Modifier.testTag("config_server_btn_${server.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FileUpload,
                            contentDescription = strings.importConfigButton,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.pasteConfigButton,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    if (server.configSource == ServerConfigSource.IMPORTED_PROFILE) {
                        IconButton(
                            onClick = onClearConfig,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("clear_config_btn_${server.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = strings.clearConfigButton,
                                tint = tokens.errorColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
