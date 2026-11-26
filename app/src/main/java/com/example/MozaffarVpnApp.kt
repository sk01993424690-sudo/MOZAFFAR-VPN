package com.example

import android.app.Application
import com.example.data.ServerProbeRepository
import com.example.data.VpnPreferencesRepository
import com.example.monetization.AdMobManager
import com.example.monetization.BillingManager
import com.example.vpn.VpnTunnelManager

class MozaffarVpnApp : Application() {

    lateinit var preferencesRepository: VpnPreferencesRepository
        private set

    lateinit var serverProbeRepository: ServerProbeRepository
        private set

    lateinit var vpnTunnelManager: VpnTunnelManager
        private set

    lateinit var adMobManager: AdMobManager
        private set

    lateinit var billingManager: BillingManager
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = VpnPreferencesRepository(this)
        serverProbeRepository = ServerProbeRepository()
        vpnTunnelManager = VpnTunnelManager(this, preferencesRepository)
        adMobManager = AdMobManager(this)
        billingManager = BillingManager(this, preferencesRepository)

        // Initialize AdMob non-blockingly so it never interferes with VPN startup or traffic
        adMobManager.initializeAsync()
    }
}
