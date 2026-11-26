package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DefaultVpnServers
import com.example.data.DnsProviderOption
import com.example.data.FormatUtils
import com.example.data.VpnPreferencesRepository
import com.example.localization.BengaliStrings
import com.example.localization.EnglishStrings
import com.example.vpn.WireGuardConfigHelper
import com.wireguard.config.InetNetwork
import com.wireguard.crypto.KeyPair
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MozaffarVpnTest {

    @Test
    fun allEightRequiredCountriesPresentInServersAndLocalizations() {
        val requiredCodes = setOf("US", "GB", "DE", "SG", "JP", "CA", "NL", "FR")
        val actualCodes = DefaultVpnServers.list.map { it.countryCode }.toSet()
        assertEquals(requiredCodes, actualCodes)

        requiredCodes.forEach { code ->
            assertTrue("Missing EN country $code", EnglishStrings.countryNames.containsKey(code))
            assertTrue("Missing BN country $code", BengaliStrings.countryNames.containsKey(code))
            assertTrue("Missing EN city $code", EnglishStrings.cityNames.containsKey(code))
            assertTrue("Missing BN city $code", BengaliStrings.cityNames.containsKey(code))
        }
    }

    @Test
    fun wireGuardConfigParsingAndDnsLeakProtectionWorks() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = VpnPreferencesRepository(context)
        val helper = WireGuardConfigHelper(prefs)

        val clientPair = KeyPair()
        val serverPair = KeyPair()
        val ipv4OnlyRawConfig = """
            # Exported from wg-quick
            [Interface]
            PrivateKey = ${clientPair.privateKey.toBase64()}
            Address = 10.8.0.2/32
            DNS = 8.8.8.8
            SaveConfig = true
            PostUp = iptables -A FORWARD -i wg0 -j ACCEPT

            [Peer]
            PublicKey = ${serverPair.publicKey.toBase64()}
            Endpoint = 198.51.100.10:51820
            AllowedIPs = 10.8.0.0/24
        """.trimIndent()

        val parsed = helper.parseAndValidateConfig(ipv4OnlyRawConfig)
        assertTrue(parsed.isSuccess)

        val hardened = helper.applySecurityPolicies(
            baseConfig = parsed.getOrThrow(),
            dnsLeakProtection = true,
            dnsProvider = DnsProviderOption.QUAD9
        )

        val dnsAddresses = hardened.`interface`.dnsServers.mapNotNull { it.hostAddress }
        assertTrue(dnsAddresses.contains("9.9.9.9"))
        assertTrue(dnsAddresses.contains("149.112.112.112"))

        val allowedIps = hardened.peers.first().allowedIps
        assertTrue(allowedIps.contains(InetNetwork.parse("0.0.0.0/0")))
        assertTrue(!allowedIps.contains(InetNetwork.parse("::/0")))
        assertEquals(25, hardened.peers.first().persistentKeepalive.orElse(0))
    }

    @Test
    fun dualStackConfigIncludesIpv6RouteAndInjectsDeviceKeyWhenOmitted() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = VpnPreferencesRepository(context)
        val helper = WireGuardConfigHelper(prefs)
        val expectedPubKey = helper.getOrCreateDeviceKeyPair().publicKey

        val serverPair = KeyPair()
        val dualStackNoPrivKey = """
            [Interface]
            Address = 10.8.0.5/32, fd42:42:42::5/128

            [Peer]
            PublicKey = ${serverPair.publicKey.toBase64()}
            Endpoint = 203.0.113.5:51820
            AllowedIPs = 0.0.0.0/0, ::/0
        """.trimIndent()

        val parsed = helper.parseAndValidateConfig(dualStackNoPrivKey)
        assertTrue(parsed.isSuccess)
        assertEquals(expectedPubKey, parsed.getOrThrow().`interface`.keyPair.publicKey)

        val hardened = helper.applySecurityPolicies(
            baseConfig = parsed.getOrThrow(),
            dnsLeakProtection = true,
            dnsProvider = DnsProviderOption.CLOUDFLARE
        )
        val allowedIps = hardened.peers.first().allowedIps
        assertTrue(allowedIps.contains(InetNetwork.parse("0.0.0.0/0")))
        assertTrue(allowedIps.contains(InetNetwork.parse("::/0")))
    }

    @Test
    fun controlPlaneUrlValidationRejectsMalformedSecrets() {
        assertEquals(null, com.example.data.VpnNetworkModule.parseValidControlPlaneUrl("VPN MOZAFFAR", "tok"))
        assertEquals(null, com.example.data.VpnNetworkModule.parseValidControlPlaneUrl("", "tok"))
        assertEquals(null, com.example.data.VpnNetworkModule.parseValidControlPlaneUrl("https://api.mozaffarvpn.invalid", "tok"))
        assertEquals(null, com.example.data.VpnNetworkModule.parseValidControlPlaneUrl("ftp://vpn.example.com", "tok"))
        assertEquals(null, com.example.data.VpnNetworkModule.parseValidControlPlaneUrl("https://vpn.example.com", "YOUR_VPN_PROVISIONING_TOKEN"))
        val valid = com.example.data.VpnNetworkModule.parseValidControlPlaneUrl("https://vpn.example.com/api", "secret123")
        assertTrue(valid != null && valid.toString() == "https://vpn.example.com/api/")
    }

    @Test
    fun formatUtilsFormatsSpeedBytesAndDurationCorrectly() {
        assertEquals("00:01:05", FormatUtils.formatDuration(65))
        assertEquals("01:01:01", FormatUtils.formatDuration(3661))
        assertEquals("512 B/s", FormatUtils.formatSpeed(512))
        assertEquals("1.0 KB/s", FormatUtils.formatSpeed(1024))
        assertEquals("1.0 MB", FormatUtils.formatBytes(1024 * 1024))
    }
}
