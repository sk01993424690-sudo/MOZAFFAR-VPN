package com.example.data

import java.util.Locale

sealed class VpnConnectionState {
    data object Disconnected : VpnConnectionState()
    data class Connecting(val serverId: String) : VpnConnectionState()
    data class Connected(
        val serverId: String,
        val connectedAtEpochMs: Long,
        val tunnelAddress: String
    ) : VpnConnectionState()
    data class Reconnecting(val serverId: String, val attempt: Int) : VpnConnectionState()
    data class Error(val errorType: VpnErrorType, val technicalDetail: String? = null) : VpnConnectionState()
}

enum class VpnErrorType {
    PERMISSION_DENIED,
    NO_SERVER_CONFIG,
    PROVISIONING_FAILED,
    TUNNEL_START_FAILED,
    NETWORK_OFFLINE
}

enum class ServerConfigSource {
    NONE,
    IMPORTED_PROFILE,
    PROVISIONING_API
}

data class VpnServerLocation(
    val id: String,
    val countryCode: String,
    val flagEmoji: String,
    val probeHost: String,
    val probePort: Int = 443,
    val pingMs: Int? = null,
    val isReachable: Boolean? = null,
    val isProbing: Boolean = false,
    val configSource: ServerConfigSource = ServerConfigSource.NONE,
    val endpointDisplay: String? = null
)

data class TunnelTelemetry(
    val durationSeconds: Long = 0L,
    val downloadBytesPerSec: Long = 0L,
    val uploadBytesPerSec: Long = 0L,
    val sessionRxBytes: Long = 0L,
    val sessionTxBytes: Long = 0L,
    val totalRxBytes: Long = 0L,
    val totalTxBytes: Long = 0L,
    val dnsServers: List<String> = emptyList(),
    val endpoint: String? = null
)

data class PublicIpInfo(
    val ipAddress: String? = null,
    val country: String? = null,
    val isp: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class DnsProviderOption(
    val id: String,
    val label: String,
    val primaryDns: String,
    val secondaryDns: String
) {
    CLOUDFLARE("cloudflare", "Cloudflare (1.1.1.1)", "1.1.1.1", "1.0.0.1"),
    QUAD9("quad9", "Quad9 Security (9.9.9.9)", "9.9.9.9", "149.112.112.112"),
    ADGUARD("adguard", "AdGuard DNS (94.140.14.14)", "94.140.14.14", "94.140.15.15");

    companion object {
        fun fromId(id: String?): DnsProviderOption =
            entries.firstOrNull { it.id == id } ?: CLOUDFLARE
    }
}

enum class ThemeMode(val id: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light");

    companion object {
        fun fromId(id: String?): ThemeMode =
            entries.firstOrNull { it.id == id } ?: DARK
    }
}

object DefaultVpnServers {
    val list: List<VpnServerLocation> = listOf(
        VpnServerLocation(
            id = "us-nyc",
            countryCode = "US",
            flagEmoji = "\uD83C\uDDFA\uD83C\uDDF8",
            probeHost = "s3.us-east-1.amazonaws.com"
        ),
        VpnServerLocation(
            id = "gb-lon",
            countryCode = "GB",
            flagEmoji = "\uD83C\uDDEC\uD83C\uDDE7",
            probeHost = "s3.eu-west-2.amazonaws.com"
        ),
        VpnServerLocation(
            id = "de-fra",
            countryCode = "DE",
            flagEmoji = "\uD83C\uDDE9\uD83C\uDDEA",
            probeHost = "s3.eu-central-1.amazonaws.com"
        ),
        VpnServerLocation(
            id = "sg-sin",
            countryCode = "SG",
            flagEmoji = "\uD83C\uDDF8\uD83C\uDDEC",
            probeHost = "s3.ap-southeast-1.amazonaws.com"
        ),
        VpnServerLocation(
            id = "jp-tyo",
            countryCode = "JP",
            flagEmoji = "\uD83C\uDDEF\uD83C\uDDF5",
            probeHost = "s3.ap-northeast-1.amazonaws.com"
        ),
        VpnServerLocation(
            id = "ca-tor",
            countryCode = "CA",
            flagEmoji = "\uD83C\uDDE8\uD83C\uDDE6",
            probeHost = "s3.ca-central-1.amazonaws.com"
        ),
        VpnServerLocation(
            id = "nl-ams",
            countryCode = "NL",
            flagEmoji = "\uD83C\uDDF3\uD83C\uDDF1",
            probeHost = "www.ripe.net"
        ),
        VpnServerLocation(
            id = "fr-par",
            countryCode = "FR",
            flagEmoji = "\uD83C\uDDEB\uD83C\uDDF7",
            probeHost = "s3.eu-west-3.amazonaws.com"
        )
    )
}

object FormatUtils {
    fun formatSpeed(bytesPerSec: Long): String {
        val kb = bytesPerSec / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.US, "%.2f MB/s", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.1f KB/s", kb)
            else -> "$bytesPerSec B/s"
        }
    }

    fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
            else -> "$bytes B"
        }
    }

    fun formatDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }
}
