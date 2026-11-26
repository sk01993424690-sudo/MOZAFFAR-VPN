package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ThemeMode
import com.example.data.VpnConnectionState
import com.example.localization.AppLanguage
import com.example.localization.LocalAppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.DeepIndigo
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSecure
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalVpnTokens
import com.example.ui.theme.RoyalViolet

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalVpnTokens.current
    val shape = RoundedCornerShape(20.dp)
    val baseModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = baseModifier,
        shape = shape,
        color = tokens.cardSurface,
        border = BorderStroke(1.dp, borderColor ?: tokens.cardBorder),
        tonalElevation = if (tokens.isDark) 2.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun VpnTopBrandBar(
    themeMode: ThemeMode,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val tokens = LocalVpnTokens.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mozaffar_vpn_logo_1795618520484),
                contentDescription = strings.appName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, RoyalViolet.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = strings.appName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = strings.encryptionBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.cyanAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("language_quick_toggle")
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onToggleLanguage),
                shape = RoundedCornerShape(50),
                color = tokens.cardSurfaceElevated,
                border = BorderStroke(1.dp, tokens.cardBorder)
            ) {
                Text(
                    text = if (language == AppLanguage.ENGLISH) "EN | বাং" else "বাং | EN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("theme_quick_toggle")
                    .clip(CircleShape)
                    .background(tokens.cardSurfaceElevated)
                    .border(1.dp, tokens.cardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (tokens.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = strings.themeModeLabel,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ConnectionStatusBadge(
    state: VpnConnectionState,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val tokens = LocalVpnTokens.current

    val (label, badgeColor, icon) = when (state) {
        is VpnConnectionState.Connected -> Triple(
            strings.statusConnected,
            tokens.connectedColor,
            Icons.Filled.VerifiedUser
        )
        is VpnConnectionState.Connecting -> Triple(
            strings.statusConnecting,
            RoyalViolet,
            Icons.Filled.Sync
        )
        is VpnConnectionState.Reconnecting -> Triple(
            "${strings.statusReconnecting} (#${state.attempt})",
            tokens.reconnectingColor,
            Icons.Filled.Sync
        )
        is VpnConnectionState.Error -> Triple(
            strings.statusError,
            tokens.errorColor,
            Icons.Filled.Shield
        )
        is VpnConnectionState.Disconnected -> Triple(
            strings.statusDisconnected,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Filled.Shield
        )
    }

    val animatedColor by animateColorAsState(
        targetValue = badgeColor,
        animationSpec = tween(300),
        label = "status_color"
    )

    Surface(
        modifier = modifier.testTag("connection_status_badge"),
        shape = RoundedCornerShape(50),
        color = animatedColor.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, animatedColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = animatedColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = animatedColor
            )
        }
    }
}

@Composable
fun ConnectPowerButton(
    connectionState: VpnConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val tokens = LocalVpnTokens.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val isConnected = connectionState is VpnConnectionState.Connected
    val isBusy = connectionState is VpnConnectionState.Connecting ||
        connectionState is VpnConnectionState.Reconnecting

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "button_press_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isBusy) 1400 else 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_progress"
    )
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    val ringPrimaryColor by animateColorAsState(
        targetValue = when (connectionState) {
            is VpnConnectionState.Connected -> EmeraldSecure
            is VpnConnectionState.Connecting -> ElectricCyan
            is VpnConnectionState.Reconnecting -> tokens.reconnectingColor
            is VpnConnectionState.Error -> tokens.errorColor
            is VpnConnectionState.Disconnected -> RoyalViolet
        },
        animationSpec = tween(400),
        label = "ring_color"
    )

    val coreBrush = when (connectionState) {
        is VpnConnectionState.Connected -> Brush.linearGradient(listOf(Color(0xFF065F46), EmeraldSecure))
        is VpnConnectionState.Connecting -> Brush.linearGradient(listOf(RoyalViolet, ElectricCyan))
        is VpnConnectionState.Reconnecting -> Brush.linearGradient(listOf(Color(0xFF92400E), tokens.reconnectingColor))
        is VpnConnectionState.Error -> Brush.linearGradient(listOf(DeepIndigo, Color(0xFF7F1D1D)))
        is VpnConnectionState.Disconnected -> Brush.linearGradient(listOf(DeepIndigo, RoyalViolet, ElectricViolet))
    }

    val actionText = when (connectionState) {
        is VpnConnectionState.Connected -> strings.tapToDisconnect
        is VpnConnectionState.Connecting -> strings.connectingLabel
        is VpnConnectionState.Reconnecting -> strings.reconnectingLabel
        else -> strings.tapToConnect
    }

    Box(
        modifier = modifier
            .size(214.dp)
            .scale(pressScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.36f

            if (isConnected || isBusy) {
                val expandingRadius = baseRadius + (size.minDimension * 0.14f * pulseProgress)
                drawCircle(
                    color = ringPrimaryColor.copy(alpha = (1f - pulseProgress) * 0.42f),
                    radius = expandingRadius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            drawCircle(
                color = ringPrimaryColor.copy(alpha = 0.22f),
                radius = baseRadius + 14.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            if (isBusy) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Color.Transparent, BrightCyan, RoyalViolet, Color.Transparent)
                    ),
                    startAngle = sweepAngle,
                    sweepAngle = 140f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(152.dp)
                .clip(CircleShape)
                .background(coreBrush)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.45f), ringPrimaryColor)
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                )
                .testTag("vpn_connect_power_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PowerSettingsNew,
                    contentDescription = actionText,
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PasteWireGuardConfigDialog(
    serverLabel: String,
    devicePublicKey: String = "",
    initialText: String = "",
    confirmLabel: String? = null,
    onDismiss: () -> Unit,
    onPickFileClick: () -> Unit,
    onCopyPublicKey: (() -> Unit)? = null,
    onSaveConfig: (String) -> String?
) {
    val strings = LocalAppStrings.current
    var configText by remember { mutableStateOf(initialText) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${strings.pasteConfigDialogTitle} ($serverLabel)",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.setupGuideBanner,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (devicePublicKey.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.yourPublicKeyLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = devicePublicKey,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (onCopyPublicKey != null) {
                                TextButton(onClick = onCopyPublicKey) {
                                    Text(strings.copyPublicKey)
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onPickFileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_conf_file_button")
                ) {
                    Text(strings.importConfigButton)
                }

                OutlinedTextField(
                    value = configText,
                    onValueChange = {
                        configText = it
                        errorMessage = null
                    },
                    label = { Text("WireGuard (.conf)") },
                    placeholder = {
                        Text(
                            text = strings.pasteConfigPlaceholder,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    textStyle = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    minLines = 6,
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wg_config_input")
                )

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val err = onSaveConfig(configText)
                    if (err == null) {
                        onDismiss()
                    } else {
                        errorMessage = err
                    }
                },
                modifier = Modifier.testTag("save_wg_config_button")
            ) {
                Text(confirmLabel ?: strings.saveConfig)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
