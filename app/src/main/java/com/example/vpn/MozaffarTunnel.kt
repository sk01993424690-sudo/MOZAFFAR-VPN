package com.example.vpn

import com.wireguard.android.backend.Tunnel

class MozaffarTunnel(
    private val tunnelName: String,
    private val onStateChangedCallback: (Tunnel.State) -> Unit
) : Tunnel {

    override fun getName(): String = tunnelName

    override fun onStateChange(newState: Tunnel.State) {
        onStateChangedCallback(newState)
    }
}
