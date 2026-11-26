package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ProvisionTunnelRequest(
    @Json(name = "region_id") val regionId: String,
    @Json(name = "country_code") val countryCode: String,
    @Json(name = "client_public_key") val clientPublicKey: String,
    @Json(name = "dns_provider") val dnsProvider: String
)

@JsonClass(generateAdapter = true)
data class ProvisionTunnelResponse(
    @Json(name = "server_public_key") val serverPublicKey: String,
    @Json(name = "endpoint") val endpoint: String,
    @Json(name = "client_address") val clientAddress: String,
    @Json(name = "dns_servers") val dnsServers: List<String>? = null,
    @Json(name = "allowed_ips") val allowedIps: List<String>? = null,
    @Json(name = "persistent_keepalive") val persistentKeepalive: Int = 25,
    @Json(name = "preshared_key") val presharedKey: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteServerStatusDto(
    @Json(name = "id") val id: String,
    @Json(name = "endpoint_host") val endpointHost: String? = null,
    @Json(name = "endpoint_port") val endpointPort: Int? = null,
    @Json(name = "online") val online: Boolean = true
)

@JsonClass(generateAdapter = true)
data class IpWhoIsResponse(
    @Json(name = "ip") val ip: String? = null,
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "country") val country: String? = null,
    @Json(name = "connection") val connection: IpConnectionDto? = null
)

@JsonClass(generateAdapter = true)
data class IpConnectionDto(
    @Json(name = "isp") val isp: String? = null,
    @Json(name = "org") val org: String? = null
)

interface VpnControlPlaneApi {
    @POST("v1/tunnels/provision")
    suspend fun provisionTunnel(
        @Header("Authorization") bearerToken: String,
        @Body request: ProvisionTunnelRequest
    ): ProvisionTunnelResponse

    @GET("v1/servers/status")
    suspend fun getServerStatuses(
        @Header("Authorization") bearerToken: String
    ): List<RemoteServerStatusDto>
}

interface PublicIpApi {
    @GET("https://ipwho.is/")
    suspend fun fetchPublicIp(): IpWhoIsResponse
}

object VpnNetworkModule {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logger = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()
    }

    fun parseValidControlPlaneUrl(rawUrl: String, token: String): HttpUrl? {
        val trimmedUrl = rawUrl.trim()
        val trimmedToken = token.trim()
        if (trimmedUrl.isBlank() ||
            trimmedUrl.contains("mozaffarvpn.invalid", ignoreCase = true) ||
            trimmedToken.isBlank() ||
            trimmedToken == "YOUR_VPN_PROVISIONING_TOKEN"
        ) {
            return null
        }
        val normalizedUrl = if (trimmedUrl.endsWith("/")) trimmedUrl else "$trimmedUrl/"
        val parsed = normalizedUrl.toHttpUrlOrNull() ?: return null
        if (parsed.scheme != "http" && parsed.scheme != "https") return null
        if (parsed.host.isBlank()) return null
        return parsed
    }

    fun isControlPlaneConfigured(): Boolean =
        parseValidControlPlaneUrl(BuildConfig.VPN_API_BASE_URL, BuildConfig.VPN_API_TOKEN) != null

    fun createControlPlaneApi(): VpnControlPlaneApi? {
        val validUrl = parseValidControlPlaneUrl(
            BuildConfig.VPN_API_BASE_URL,
            BuildConfig.VPN_API_TOKEN
        ) ?: return null
        return runCatching {
            Retrofit.Builder()
                .baseUrl(validUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(VpnControlPlaneApi::class.java)
        }.getOrNull()
    }

    val publicIpApi: PublicIpApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://ipwho.is/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PublicIpApi::class.java)
    }
}
