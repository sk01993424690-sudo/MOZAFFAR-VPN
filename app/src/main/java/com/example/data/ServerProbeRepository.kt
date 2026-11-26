package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class ServerProbeRepository {

    suspend fun probeServerLatency(host: String, port: Int = 443, timeoutMs: Int = 2500): Int? =
        withContext(Dispatchers.IO) {
            runCatching {
                val startNs = System.nanoTime()
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), timeoutMs)
                }
                val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).toInt()
                elapsedMs.coerceAtLeast(1)
            }.getOrNull()
        }

    suspend fun probeAllServers(servers: List<VpnServerLocation>): Map<String, Int?> =
        coroutineScope {
            servers.map { server ->
                async {
                    val latency = probeServerLatency(server.probeHost, server.probePort)
                    server.id to latency
                }
            }.awaitAll().toMap()
        }
}
