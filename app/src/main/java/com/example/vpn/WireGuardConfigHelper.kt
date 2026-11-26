package com.example.vpn

import com.example.data.DnsProviderOption
import com.example.data.ProvisionTunnelResponse
import com.example.data.VpnPreferencesRepository
import com.wireguard.config.Config
import com.wireguard.config.InetEndpoint
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import java.io.BufferedReader
import java.io.StringReader
import java.net.Inet6Address
import java.net.InetAddress

class WireGuardConfigHelper(
    private val prefsRepo: VpnPreferencesRepository
) {
    fun getOrCreateDeviceKeyPair(): KeyPair {
        val storedPrivate = prefsRepo.getDevicePrivateKeyBase64()
        if (!storedPrivate.isNullOrBlank()) {
            runCatching {
                return KeyPair(Key.fromBase64(storedPrivate))
            }
        }
        return regenerateDeviceKeyPair()
    }

    fun regenerateDeviceKeyPair(): KeyPair {
        val pair = KeyPair()
        prefsRepo.saveDevicePrivateKeyBase64(pair.privateKey.toBase64())
        return pair
    }

    fun sanitizeConfigText(rawConfig: String): String {
        val normalized = rawConfig
            .replace("\r\n", "\n")
            .replace('\r', '\n')

        val wgQuickOnlyKeys = setOf(
            "saveconfig",
            "preup",
            "postup",
            "predown",
            "postdown",
            "table",
            "fwmark"
        )

        val cleanedLines = mutableListOf<String>()
        var currentSection = ""
        var hasPrivateKey = false

        for (rawLine in normalized.lines()) {
            val withoutComment = stripInlineComment(rawLine).trim()
            if (withoutComment.isEmpty()) continue

            if (withoutComment.startsWith("[") && withoutComment.endsWith("]")) {
                currentSection = withoutComment.lowercase()
                cleanedLines.add(withoutComment)
                continue
            }

            val eqIdx = withoutComment.indexOf('=')
            if (eqIdx > 0) {
                val key = withoutComment.substring(0, eqIdx).trim().lowercase()
                if (currentSection == "[interface]" && key in wgQuickOnlyKeys) {
                    continue
                }
                if (currentSection == "[interface]" && key == "privatekey") {
                    hasPrivateKey = true
                }
            }
            cleanedLines.add(withoutComment)
        }

        // If the user imported a peer-only config without [Interface] PrivateKey,
        // automatically supply this device's Curve25519 PrivateKey so it matches Device Public Key.
        if (!hasPrivateKey) {
            val ifaceIdx = cleanedLines.indexOfFirst { it.equals("[Interface]", ignoreCase = true) }
            if (ifaceIdx >= 0) {
                cleanedLines.add(ifaceIdx + 1, "PrivateKey = ${getOrCreateDeviceKeyPair().privateKey.toBase64()}")
            }
        }

        return cleanedLines.joinToString("\n")
    }

    private fun stripInlineComment(line: String): String {
        val hashIdx = line.indexOf('#')
        val semiIdx = line.indexOf(';')
        val cutIdx = listOf(hashIdx, semiIdx).filter { it >= 0 }.minOrNull() ?: return line
        return line.substring(0, cutIdx)
    }

    fun parseAndValidateConfig(rawConfig: String): Result<Config> = runCatching {
        val sanitized = sanitizeConfigText(rawConfig)
        require(sanitized.isNotEmpty()) { "Empty WireGuard configuration" }
        val config = Config.parse(BufferedReader(StringReader(sanitized)))
        require(config.`interface`.addresses.isNotEmpty()) {
            "[Interface] must include at least one Address (e.g. 10.0.0.2/32)"
        }
        require(config.peers.isNotEmpty()) { "At least one [Peer] section is required" }
        require(config.peers.any { it.endpoint.isPresent }) {
            "[Peer] must include a valid Endpoint (host:port)"
        }
        config
    }

    fun applySecurityPolicies(
        baseConfig: Config,
        dnsLeakProtection: Boolean,
        dnsProvider: DnsProviderOption,
        forceIpv4OnlyRoutes: Boolean = false
    ): Config {
        val ifaceBuilder = Interface.Builder()
        ifaceBuilder.setKeyPair(baseConfig.`interface`.keyPair)
        baseConfig.`interface`.addresses.forEach { ifaceBuilder.addAddress(it) }
        baseConfig.`interface`.mtu.ifPresent { ifaceBuilder.setMtu(it) }
        baseConfig.`interface`.listenPort.ifPresent { ifaceBuilder.setListenPort(it) }
        baseConfig.`interface`.excludedApplications.forEach { ifaceBuilder.excludeApplication(it) }
        baseConfig.`interface`.includedApplications.forEach { ifaceBuilder.includeApplication(it) }

        val hasInterfaceIpv6 = baseConfig.`interface`.addresses.any { it.address is Inet6Address }

        if (dnsLeakProtection) {
            ifaceBuilder.addDnsServer(InetAddress.getByName(dnsProvider.primaryDns))
            ifaceBuilder.addDnsServer(InetAddress.getByName(dnsProvider.secondaryDns))
        } else if (baseConfig.`interface`.dnsServers.isNotEmpty()) {
            baseConfig.`interface`.dnsServers.forEach { ifaceBuilder.addDnsServer(it) }
        } else {
            ifaceBuilder.addDnsServer(InetAddress.getByName(dnsProvider.primaryDns))
        }

        val configBuilder = Config.Builder()
        configBuilder.setInterface(ifaceBuilder.build())

        baseConfig.peers.forEach { peer ->
            val peerBuilder = Peer.Builder()
            peerBuilder.setPublicKey(peer.publicKey)
            peer.preSharedKey.ifPresent { peerBuilder.setPreSharedKey(it) }
            peer.endpoint.ifPresent { peerBuilder.setEndpoint(it) }
            val keepalive = peer.persistentKeepalive.orElse(25).coerceIn(15, 120)
            peerBuilder.setPersistentKeepalive(keepalive)

            val peerHasIpv6 = peer.allowedIps.any { it.address is Inet6Address }
            val allowIpv6 = !forceIpv4OnlyRoutes && (hasInterfaceIpv6 || peerHasIpv6)

            if (dnsLeakProtection) {
                // Full-tunnel IPv4 (+ IPv6 when configured/supported) prevents DNS/IP leaks
                peerBuilder.addAllowedIp(InetNetwork.parse("0.0.0.0/0"))
                if (allowIpv6) {
                    peerBuilder.addAllowedIp(InetNetwork.parse("::/0"))
                }
            } else {
                val rawAllowed = peer.allowedIps.filter { network ->
                    !forceIpv4OnlyRoutes || network.address !is Inet6Address
                }
                if (rawAllowed.isEmpty()) {
                    peerBuilder.addAllowedIp(InetNetwork.parse("0.0.0.0/0"))
                } else {
                    rawAllowed.forEach { peerBuilder.addAllowedIp(it) }
                }
            }
            configBuilder.addPeer(peerBuilder.build())
        }

        return configBuilder.build()
    }

    fun buildConfigFromProvisionResponse(
        response: ProvisionTunnelResponse,
        deviceKeyPair: KeyPair,
        dnsLeakProtection: Boolean,
        dnsProvider: DnsProviderOption
    ): Config {
        val clientNet = InetNetwork.parse(response.clientAddress.trim())
        val hasInterfaceIpv6 = clientNet.address is Inet6Address

        val ifaceBuilder = Interface.Builder()
            .setKeyPair(deviceKeyPair)
            .addAddress(clientNet)

        val dnsList = if (dnsLeakProtection) {
            listOf(dnsProvider.primaryDns, dnsProvider.secondaryDns)
        } else {
            response.dnsServers?.takeIf { it.isNotEmpty() }
                ?: listOf(dnsProvider.primaryDns, dnsProvider.secondaryDns)
        }
        dnsList.forEach { dns ->
            ifaceBuilder.addDnsServer(InetAddress.getByName(dns.trim()))
        }

        val peerBuilder = Peer.Builder()
            .parsePublicKey(response.serverPublicKey.trim())
            .setEndpoint(InetEndpoint.parse(response.endpoint.trim()))
            .setPersistentKeepalive(response.persistentKeepalive.coerceIn(15, 60))

        response.presharedKey?.takeIf { it.isNotBlank() }?.let {
            peerBuilder.parsePreSharedKey(it.trim())
        }

        val allowedIps = if (dnsLeakProtection || response.allowedIps.isNullOrEmpty()) {
            if (hasInterfaceIpv6) listOf("0.0.0.0/0", "::/0") else listOf("0.0.0.0/0")
        } else {
            response.allowedIps
        }
        allowedIps.forEach { cidr ->
            peerBuilder.addAllowedIp(InetNetwork.parse(cidr.trim()))
        }

        return Config.Builder()
            .setInterface(ifaceBuilder.build())
            .addPeer(peerBuilder.build())
            .build()
    }

    fun extractEndpointDisplay(rawConfig: String): String? = runCatching {
        val sanitized = sanitizeConfigText(rawConfig)
        val cfg = Config.parse(BufferedReader(StringReader(sanitized)))
        cfg.peers.firstOrNull()?.endpoint?.map { it.toString() }?.orElse(null)
    }.getOrNull()
}
