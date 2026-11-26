package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.MozaffarVpnApp
import com.example.R

class VpnMonitorService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISCONNECT -> {
                (application as? MozaffarVpnApp)?.vpnTunnelManager?.disconnect(userInitiated = true)
                stopForegroundCompat()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_STOP -> {
                stopForegroundCompat()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                val serverLabel = intent?.getStringExtra(EXTRA_SERVER_LABEL) ?: "WireGuard"
                val isReconnecting = intent?.getBooleanExtra(EXTRA_RECONNECTING, false) ?: false
                ensureChannel()
                val notification = buildNotification(serverLabel, isReconnecting)
                runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        startForeground(
                            NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun stopForegroundCompat() {
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java) ?: return
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.vpn_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = getString(R.string.vpn_notification_channel_desc)
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun buildNotification(serverLabel: String, isReconnecting: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            100,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(this, VpnMonitorService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            101,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = if (isReconnecting) {
            getString(R.string.vpn_notification_reconnecting, serverLabel)
        } else {
            getString(R.string.vpn_notification_connected, serverLabel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.vpn_notification_title))
            .setContentText(bodyText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                0,
                getString(R.string.vpn_action_disconnect),
                disconnectPendingIntent
            )
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "mozaffar_vpn_tunnel_status"
        private const val NOTIFICATION_ID = 4102
        const val ACTION_START = "com.example.vpn.ACTION_START"
        const val ACTION_UPDATE = "com.example.vpn.ACTION_UPDATE"
        const val ACTION_STOP = "com.example.vpn.ACTION_STOP"
        const val ACTION_DISCONNECT = "com.example.vpn.ACTION_DISCONNECT"
        const val EXTRA_SERVER_LABEL = "extra_server_label"
        const val EXTRA_RECONNECTING = "extra_reconnecting"

        fun startOrUpdate(context: Context, serverLabel: String, reconnecting: Boolean = false) {
            val intent = Intent(context, VpnMonitorService::class.java).apply {
                action = if (reconnecting) ACTION_UPDATE else ACTION_START
                putExtra(EXTRA_SERVER_LABEL, serverLabel)
                putExtra(EXTRA_RECONNECTING, reconnecting)
            }
            runCatching { ContextCompat.startForegroundService(context, intent) }
        }

        fun stop(context: Context) {
            val intent = Intent(context, VpnMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            runCatching { context.startService(intent) }
        }
    }
}
