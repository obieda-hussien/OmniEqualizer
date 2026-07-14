package com.omni.equalizer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.omni.equalizer.audio.OmniAudioEngine
import com.omni.equalizer.audio.OmniVisualizerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Owns the real audio engines for as long as the process is alive. The engines are
 * process-wide singletons ([OmniAudioEngine], [OmniVisualizerEngine]) — this service's job
 * is purely lifecycle: attach them when the app starts doing work, release them when it's
 * done, and keep the notification text truthful about whether the engine actually took hold.
 */
class EqualizerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob())
    private var statusJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        OmniAudioEngine.attach()
        OmniVisualizerEngine.attach()

        // Keep the notification text truthful as the engine status changes (e.g. if
        // attach() initially fails but a later retry succeeds).
        statusJob = OmniAudioEngine.status.onEach { updateNotification() }.launchIn(serviceScope)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        createNotificationChannel()
        startForeground(1, createNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        statusJob?.cancel()
        serviceScope.cancel()
        OmniVisualizerEngine.release()
        OmniAudioEngine.release()
        super.onDestroy()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(1, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "OMNI_EQ_CHANNEL",
                "OmniEqualizer Smart Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs the AI-powered equalizer engine in the background"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, EqualizerService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = when (val status = OmniAudioEngine.status.value) {
            is OmniAudioEngine.Status.Active -> "Real-time engine is shaping your system audio"
            is OmniAudioEngine.Status.PartiallyActive ->
                "Engine active — ${status.missing.joinToString()} unavailable on this device"
            is OmniAudioEngine.Status.Unavailable -> "Engine unavailable on this device"
            is OmniAudioEngine.Status.NotAttached -> "Starting engine…"
        }

        return NotificationCompat.Builder(this, "OMNI_EQ_CHANNEL")
            .setContentTitle("OmniEqualizer")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_media_play) // Placeholder icon
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
