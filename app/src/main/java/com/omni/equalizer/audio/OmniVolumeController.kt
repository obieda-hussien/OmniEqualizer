package com.omni.equalizer.audio

import android.content.Context
import android.media.AudioManager

/**
 * Wraps [AudioManager] so the in-app volume slider controls the REAL system media volume
 * (STREAM_MUSIC), instead of just moving a number around in memory that had no effect on
 * actual playback — which is what the previous `volume` field in the UI state did.
 */
object OmniVolumeController {

    private var audioManager: AudioManager? = null

    fun attach(context: Context) {
        if (audioManager != null) return
        audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    /** Current system media volume as a 0..100 percent, or null if not attached yet. */
    fun getCurrentVolumePercent(): Float? {
        val am = audioManager ?: return null
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (max <= 0) return null
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        return (current.toFloat() / max.toFloat()) * 100f
    }

    /** Sets the REAL system media volume. percent expected in 0..100. */
    fun setVolumePercent(percent: Float) {
        val am = audioManager ?: return
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (max <= 0) return
        val target = ((percent.coerceIn(0f, 100f) / 100f) * max).toInt().coerceIn(0, max)
        try {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        } catch (_: SecurityException) {
            // Some OEMs restrict volume changes without additional permissions (e.g. Do Not
            // Disturb interactions on notification-adjacent streams) — fail silently rather
            // than crash; the slider simply won't move the real volume on that device.
        }
    }
}
