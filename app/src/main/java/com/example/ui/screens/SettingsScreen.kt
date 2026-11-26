package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DnsProviderOption
import com.example.data.ThemeMode
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.ui.LegalDocTypes
import com.example.ui.VpnDashboardUiState
import com.example.ui.components.GlassCard
import com.example.ui.theme.EmeraldSecure
import com.example.ui.theme.LocalVpnTokens
import com.example.ui.theme.RoyalViolet

@Composable
fun SettingsScreen(
    uiState: VpnDashboardUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onAutoReconnectChanged: (Boolean) -> Unit,
    onDnsLeakProtectionChanged: (Boolean) -> Unit,
    onDnsProviderSelected: (DnsProviderOption) -> Unit,
    onOpenVpnSettings: () -> Unit,
    onRegenerateKeys: () -> Unit,
    onOpenPremium: () -> Unit,
    onOpenLegalDoc: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val tokens = LocalVpnTokens.current
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = strings.settingsTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Premium Subscription Banner
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_premium_card"),
                onClick = onOpenPremium,
                borderColor = RoyalViolet.copy(alpha = 0.6f)
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
                            imageVector = Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = RoyalViolet,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.premiumCardTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.premiumCardSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (uiState.preferences.isPremiumSubscribed) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSecure.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, EmeraldSecure)
                        ) {
                            Text(
                                text = strings.premiumActiveBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSecure,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = strings.upgradeToPro
                        )
                    }
                }
            }

            // Section: Appearance & Language
            SectionHeader(strings.sectionAppearanceLanguage)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = null,
                        tint = tokens.cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.languageLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        val selected = uiState.preferences.language == lang
                        FilterChip(
                            selected = selected,
                            onClick = { onLanguageSelected(lang) },
                            label = { Text(lang.nativeName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalViolet,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("lang_chip_${lang.code}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = tokens.cardBorder)
                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = null,
                        tint = RoyalViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.themeModeLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val modes = listOf(
                        ThemeMode.DARK to strings.themeDark,
                        ThemeMode.LIGHT to strings.themeLight,
                        ThemeMode.SYSTEM to strings.themeSystem
                    )
                    modes.forEach { (mode, label) ->
                        FilterChip(
                            selected = uiState.preferences.themeMode == mode,
                            onClick = { onThemeModeSelected(mode) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalViolet,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("theme_chip_${mode.id}")
                        )
                    }
                }
            }

            // Section: VPN Security & Protocol
            SectionHeader(strings.sectionVpnSecurity)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SettingsSwitchRow(
                    icon = Icons.Filled.Sync,
                    title = strings.autoReconnect,
                    checked = uiState.preferences.autoReconnectEnabled,
                    onCheckedChange = onAutoReconnectChanged,
                    testTag = "switch_auto_reconnect"
                )
                HorizontalDivider(
                    color = tokens.cardBorder,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                SettingsSwitchRow(
                    icon = Icons.Filled.Dns,
                    title = strings.dnsLeakProtection,
                    checked = uiState.preferences.dnsLeakProtectionEnabled,
                    onCheckedChange = onDnsLeakProtectionChanged,
                    testTag = "switch_dns_leak"
                )

                if (uiState.preferences.dnsLeakProtectionEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = strings.dnsProviderLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DnsProviderOption.entries.forEach { provider ->
                            FilterChip(
                                selected = uiState.preferences.dnsProvider == provider,
                                onClick = { onDnsProviderSelected(provider) },
                                label = { Text(provider.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalViolet,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = tokens.cardBorder,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (uiState.isSystemLockdownEnabled) EmeraldSecure else tokens.cyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.killSwitchLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.killSwitchCardDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onOpenVpnSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_system_vpn_settings_btn")
                ) {
                    Text(strings.openAndroidVpnSettings)
                }

                HorizontalDivider(
                    color = tokens.cardBorder,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Key,
                                contentDescription = null,
                                tint = RoyalViolet,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.regenerateKeysLabel,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = strings.regenerateKeysDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            onRegenerateKeys()
                            Toast.makeText(
                                context,
                                strings.keysRegeneratedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("regenerate_keys_btn")
                    ) {
                        Text("Rotate")
                    }
                }
            }

            // Section: Support & Legal
            SectionHeader(strings.sectionSupportLegal)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                SettingsActionRow(
                    icon = Icons.Filled.Email,
                    title = strings.contactSupport,
                    testTag = "action_contact_support",
                    onClick = {
                        val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@mozaffarvpn.example")
                            putExtra(Intent.EXTRA_SUBJECT, strings.supportEmailSubject)
                        }
                        runCatching { context.startActivity(mailIntent) }
                    }
                )
                HorizontalDivider(color = tokens.cardBorder)
                SettingsActionRow(
                    icon = Icons.Filled.Share,
                    title = strings.shareApp,
                    testTag = "action_share_app",
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, strings.shareAppMessage)
                        }
                        runCatching {
                            context.startActivity(Intent.createChooser(shareIntent, strings.shareApp))
                        }
                    }
                )
                HorizontalDivider(color = tokens.cardBorder)
                SettingsActionRow(
                    icon = Icons.Filled.StarRate,
                    title = strings.rateApp,
                    testTag = "action_rate_app",
                    onClick = {
                        val pkg = context.packageName
                        val marketIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$pkg")
                        )
                        val webIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                        )
                        runCatching { context.startActivity(marketIntent) }
                            .onFailure { runCatching { context.startActivity(webIntent) } }
                    }
                )
                HorizontalDivider(color = tokens.cardBorder)
                SettingsActionRow(
                    icon = Icons.Filled.Info,
                    title = strings.aboutApp,
                    testTag = "action_about_app",
                    onClick = { onOpenLegalDoc(LegalDocTypes.ABOUT) }
                )
                HorizontalDivider(color = tokens.cardBorder)
                SettingsActionRow(
                    icon = Icons.Filled.Policy,
                    title = strings.privacyPolicy,
                    testTag = "action_privacy_policy",
                    onClick = { onOpenLegalDoc(LegalDocTypes.PRIVACY) }
                )
                HorizontalDivider(color = tokens.cardBorder)
                SettingsActionRow(
                    icon = Icons.Filled.Description,
                    title = strings.termsConditions,
                    testTag = "action_terms_conditions",
                    onClick = { onOpenLegalDoc(LegalDocTypes.TERMS) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .testTag(testTag),
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
