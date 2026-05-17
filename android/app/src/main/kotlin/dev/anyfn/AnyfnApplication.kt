/**
 * AnyfnApplication
 *
 * Application entry point. Responsibilities:
 *  - Initialise Hilt's object graph (the @HiltAndroidApp annotation does the wiring).
 *  - Create the foreground-service notification channel used by the MCP bridge.
 *  - Crash-free: any setup that can fail (DB open, key load) is deferred to its owning component.
 */
package dev.anyfn

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AnyfnApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createBridgeNotificationChannel()
    }

    private fun createBridgeNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            BRIDGE_CHANNEL_ID,
            getString(R.string.mcp_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.mcp_channel_description)
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val BRIDGE_CHANNEL_ID: String = "anyfn.bridge"
    }
}
