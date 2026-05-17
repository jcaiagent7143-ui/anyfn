/**
 * McpForegroundService
 *
 * Keeps the MCP server alive in the foreground so Doze and background limits
 * don't cull it. Posts a low-importance notification with a single "Stop"
 * action.
 */
package dev.anyfn.bridge

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.anyfn.AnyfnApplication
import dev.anyfn.MainActivity
import dev.anyfn.R
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class McpForegroundService : LifecycleService() {

    @Inject lateinit var server: McpServer

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotification("starting…"))
        lifecycleScope.launch {
            val result = server.start()
            val text = result.getOrNull()?.let { "MCP bridge on ${it.wsUrl()}" } ?: "MCP bridge failed to start"
            startForeground(NOTIF_ID, buildNotification(text))
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        server.stop()
        super.onDestroy()
    }

    private fun buildNotification(text: String): Notification {
        val tap = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, McpForegroundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, AnyfnApplication.BRIDGE_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .setContentIntent(tap)
            .addAction(0, "Stop", stop)
            .setCategory(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.CATEGORY_SERVICE else null)
            .build()
    }

    companion object {
        const val NOTIF_ID: Int = 7174
        const val ACTION_STOP: String = "dev.anyfn.bridge.STOP"
    }
}
