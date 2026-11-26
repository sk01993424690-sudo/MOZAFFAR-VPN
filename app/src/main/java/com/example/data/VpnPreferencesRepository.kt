package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.vpnDataStore: DataStore<Preferences> by preferencesDataStore(name = "mozaffar_vpn_prefs")

data class VpnUserPreferences(
    val selectedServerId: String = DefaultVpnServers.list.first().id,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val autoReconnectEnabled: Boolean = true,
    val dnsLeakProtectionEnabled: Boolean = true,
    val dnsProvider: DnsProviderOption = DnsProviderOption.CLOUDFLARE,
    val totalRxBytes: Long = 0L,
    val totalTxBytes: Long = 0L,
    val adFreeUntilEpochMs: Long = 0L,
    val isPremiumSubscribed: Boolean = false
) {
    fun isAdFreeNow(nowEpochMs: Long = System.currentTimeMillis()): Boolean =
        isPremiumSubscribed || adFreeUntilEpochMs > nowEpochMs
}

class VpnPreferencesRepository(private val context: Context) {

    private object Keys {
        val SELECTED_SERVER_ID = stringPreferencesKey("selected_server_id")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTO_RECONNECT = booleanPreferencesKey("auto_reconnect")
        val DNS_LEAK_PROTECTION = booleanPreferencesKey("dns_leak_protection")
        val DNS_PROVIDER = stringPreferencesKey("dns_provider")
        val TOTAL_RX_BYTES = longPreferencesKey("total_rx_bytes")
        val TOTAL_TX_BYTES = longPreferencesKey("total_tx_bytes")
        val AD_FREE_UNTIL = longPreferencesKey("ad_free_until_ms")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
    }

    private val securePrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "mozaffar_vpn_vault",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Throwable) {
            context.getSharedPreferences("mozaffar_vpn_vault_fallback", Context.MODE_PRIVATE)
        }
    }

    val preferencesFlow: Flow<VpnUserPreferences> = context.vpnDataStore.data.map { prefs ->
        VpnUserPreferences(
            selectedServerId = prefs[Keys.SELECTED_SERVER_ID] ?: DefaultVpnServers.list.first().id,
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE]),
            themeMode = ThemeMode.fromId(prefs[Keys.THEME_MODE]),
            autoReconnectEnabled = prefs[Keys.AUTO_RECONNECT] ?: true,
            dnsLeakProtectionEnabled = prefs[Keys.DNS_LEAK_PROTECTION] ?: true,
            dnsProvider = DnsProviderOption.fromId(prefs[Keys.DNS_PROVIDER]),
            totalRxBytes = prefs[Keys.TOTAL_RX_BYTES] ?: 0L,
            totalTxBytes = prefs[Keys.TOTAL_TX_BYTES] ?: 0L,
            adFreeUntilEpochMs = prefs[Keys.AD_FREE_UNTIL] ?: 0L,
            isPremiumSubscribed = prefs[Keys.IS_PREMIUM] ?: false
        )
    }

    suspend fun setSelectedServer(serverId: String) {
        context.vpnDataStore.edit { it[Keys.SELECTED_SERVER_ID] = serverId }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.vpnDataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.vpnDataStore.edit { it[Keys.THEME_MODE] = mode.id }
    }

    suspend fun setAutoReconnect(enabled: Boolean) {
        context.vpnDataStore.edit { it[Keys.AUTO_RECONNECT] = enabled }
    }

    suspend fun setDnsLeakProtection(enabled: Boolean) {
        context.vpnDataStore.edit { it[Keys.DNS_LEAK_PROTECTION] = enabled }
    }

    suspend fun setDnsProvider(provider: DnsProviderOption) {
        context.vpnDataStore.edit { it[Keys.DNS_PROVIDER] = provider.id }
    }

    suspend fun addTrafficUsage(rxDelta: Long, txDelta: Long) {
        if (rxDelta <= 0L && txDelta <= 0L) return
        context.vpnDataStore.edit { prefs ->
            prefs[Keys.TOTAL_RX_BYTES] = (prefs[Keys.TOTAL_RX_BYTES] ?: 0L) + rxDelta.coerceAtLeast(0L)
            prefs[Keys.TOTAL_TX_BYTES] = (prefs[Keys.TOTAL_TX_BYTES] ?: 0L) + txDelta.coerceAtLeast(0L)
        }
    }

    suspend fun grantAdFreePass(durationMillis: Long) {
        val until = System.currentTimeMillis() + durationMillis
        context.vpnDataStore.edit { it[Keys.AD_FREE_UNTIL] = until }
    }

    suspend fun setPremiumSubscribed(subscribed: Boolean) {
        context.vpnDataStore.edit { it[Keys.IS_PREMIUM] = subscribed }
    }

    fun getDevicePrivateKeyBase64(): String? = securePrefs.getString(KEY_WG_PRIVATE, null)

    fun saveDevicePrivateKeyBase64(privateKeyBase64: String) {
        securePrefs.edit().putString(KEY_WG_PRIVATE, privateKeyBase64).apply()
    }

    fun saveServerConfigText(serverId: String, wgConfigText: String) {
        securePrefs.edit().putString(CONFIG_PREFIX + serverId, wgConfigText).apply()
    }

    fun getServerConfigText(serverId: String): String? =
        securePrefs.getString(CONFIG_PREFIX + serverId, null)

    fun removeServerConfigText(serverId: String) {
        securePrefs.edit().remove(CONFIG_PREFIX + serverId).apply()
    }

    fun hasServerConfig(serverId: String): Boolean =
        !getServerConfigText(serverId).isNullOrBlank()

    companion object {
        private const val KEY_WG_PRIVATE = "wg_device_private_key"
        private const val CONFIG_PREFIX = "wg_conf_"
    }
}
