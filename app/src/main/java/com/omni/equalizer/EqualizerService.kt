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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Owns the real audio engines for as long as the process is alive, and drives a notification
 * that reflects genuine engine state — active effects, honest status, and a real "Bypass"
 * quick A/B toggle — instead of a static "AI Engine optimizing your audio" string that never
 * changed regardless of what was actually happening.
 */
class EqualizerService : Service() {

    companion object {
        private const val CHANNEL_ID = "OMNI_EQ_CHANNEL"
        private const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "com.omni.equalizer.STOP_SERVICE"
        const val ACTION_TOGGLE_BYPASS = "com.omni.equalizer.TOGGLE_BYPASS"
    }

    private val serviceScope = CoroutineScope(SupervisorJob())
    private var statusJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        OmniAudioEngine.attach()
        OmniVisualizerEngine.attach()

        // Keep the notification truthful as real engine/effect/bypass state changes.
        statusJob = combine(
            OmniAudioEngine.status,
            OmniAudioEngine.activeEffects,
            OmniAudioEngine.isBypassed
        ) { _, _, _ -> Unit }.onEach { updateNotification() }.launchIn(serviceScope)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_BYPASS -> {
                OmniAudioEngine.setGlobalBypass(!OmniAudioEngine.isBypassed.value)
                return START_STICKY
            }
        }
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
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
        manager?.notify(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "OmniEqualizer Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the real-time status of the audio engine and lets you bypass it quickly"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getService(
            this, 1, Intent(this, EqualizerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        val bypassPendingIntent = PendingIntent.getService(
            this, 2, Intent(this, EqualizerService::class.java).apply { action = ACTION_TOGGLE_BYPASS },
            PendingIntent.FLAG_IMMUTABLE
        )

        val isBypassed = OmniAudioEngine.isBypassed.value
        val activeEffects = OmniAudioEngine.activeEffects.value

        val statusLine = when (val status = OmniAudioEngine.status.value) {
            is OmniAudioEngine.Status.Active -> "Engine active on the global mix"
            is OmniAudioEngine.Status.PartiallyActive ->
                "Engine active — ${status.missing.joinToString()} unsupported on this device"
            is OmniAudioEngine.Status.Unavailable -> "Engine unavailable on this device"
            is OmniAudioEngine.Status.NotAttached -> "Starting engine…"
        }

        val effectsLine = when {
            isBypassed -> "Bypassed — playing raw, unprocessed audio"
            activeEffects.isEmpty() -> "No effects currently active"
            else -> "Active: ${activeEffects.joinToString(" • ")}"
        }

        val expandedText = "$statusLine\n$effectsLine"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isBypassed) "OmniEqualizer (Bypassed)" else "OmniEqualizer")
            .setContentText(effectsLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentPendingIntent)
            .addAction(
                R.drawable.ic_notification,
                if (isBypassed) "Re-enable" else "Bypass",
                bypassPendingIntent
            )
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
