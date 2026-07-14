package com.omni.equalizer.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The real DSP engine.
 *
 * This attaches [Equalizer], [BassBoost], [Virtualizer] and [LoudnessEnhancer] to audio
 * session 0 — the "global mix" / auxiliary session that (on devices whose audio HAL allows
 * it) lets us shape every app's audio without needing our own media player.
 *
 * HONESTY CONTRACT: this is best-effort. Some OEM audio stacks (certain Samsung, some
 * MIUI builds, some OEM "Sound Enhancer" overlays) block or ignore session-0 effects.
 * Rather than silently doing nothing while claiming to work, this engine exposes
 * [status] so the UI can tell the user the truth.
 */
object OmniAudioEngine {

    private const val TAG = "OmniAudioEngine"
    private const val GLOBAL_MIX_SESSION = 0

    // Highest priority so we win over other apps trying to grab the same session effects.
    private const val PRIORITY = 0

    /** Fixed UI band centre-frequencies in Hz — must stay in sync with the 10 sliders in the UI. */
    val uiBandFrequenciesHz = intArrayOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
    const val uiBandCount = 10

    sealed class Status {
        data object NotAttached : Status()
        data object Active : Status()
        data class PartiallyActive(val missing: List<String>) : Status()
        data class Unavailable(val reason: String) : Status()
    }

    private val _status = MutableStateFlow<Status>(Status.NotAttached)
    val status: StateFlow<Status> = _status

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var deviceBandCount: Short = 0
    private var deviceLevelRangeMb: ShortArray = shortArrayOf(-1500, 1500)

    /** uiBandIndex (0..9) -> nearest real hardware band index on this device, or -1 if none. */
    private var uiToDeviceBand = IntArray(uiBandCount) { -1 }

    private var bassBoostStrengthSupported = false
    private var virtualizerStrengthSupported = false

    // What the UI last asked for, independent of bypass — so un-bypassing restores exactly
    // what the user had configured instead of guessing.
    private var requestedEqEnabled = true
    private var requestedBassEnabled = false
    private var requestedVirtualizerEnabled = false
    private var requestedLoudnessEnabled = false

    private val _isBypassed = MutableStateFlow(false)
    /** True when the user hit "Bypass" from the notification for a quick A/B listen. */
    val isBypassed: StateFlow<Boolean> = _isBypassed

    /** Effects that are ACTUALLY audible right now (requested AND not bypassed) — the honest
     *  truth surfaced to the notification, not just what the UI thinks it asked for. */
    private val _activeEffects = MutableStateFlow<Set<String>>(emptySet())
    val activeEffects: StateFlow<Set<String>> = _activeEffects

    @Synchronized
    fun attach() {
        if (equalizer != null || bassBoost != null || virtualizer != null || loudnessEnhancer != null) return

        val missing = mutableListOf<String>()

        try {
            val eq = Equalizer(PRIORITY, GLOBAL_MIX_SESSION)
            deviceBandCount = eq.numberOfBands
            deviceLevelRangeMb = eq.bandLevelRange
            buildBandMapping(eq)
            eq.enabled = true
            equalizer = eq
        } catch (t: Throwable) {
            Log.w(TAG, "Equalizer unavailable on global session: ${t.message}")
            missing += "Equalizer"
        }

        try {
            val bb = BassBoost(PRIORITY, GLOBAL_MIX_SESSION)
            bassBoostStrengthSupported = bb.strengthSupported
            bb.enabled = false
            bassBoost = bb
        } catch (t: Throwable) {
            Log.w(TAG, "BassBoost unavailable: ${t.message}")
            missing += "BassBoost"
        }

        try {
            val vr = Virtualizer(PRIORITY, GLOBAL_MIX_SESSION)
            virtualizerStrengthSupported = vr.strengthSupported
            vr.enabled = false
            virtualizer = vr
        } catch (t: Throwable) {
            Log.w(TAG, "Virtualizer unavailable: ${t.message}")
            missing += "Virtualizer"
        }

        try {
            val le = LoudnessEnhancer(GLOBAL_MIX_SESSION)
            le.enabled = false
            loudnessEnhancer = le
        } catch (t: Throwable) {
            Log.w(TAG, "LoudnessEnhancer unavailable: ${t.message}")
            missing += "Loudness"
        }

        _status.value = when {
            equalizer == null && bassBoost == null && virtualizer == null && loudnessEnhancer == null ->
                Status.Unavailable("النظام رفض أي تعديل صوتي على الـ session العام. الجهاز ده على الأغلب بيقيّد الوصول لصوت النظام من تطبيقات تالتة.")
            missing.isEmpty() -> Status.Active
            else -> Status.PartiallyActive(missing)
        }
    }

    private fun buildBandMapping(eq: Equalizer) {
        for (uiIndex in 0 until uiBandCount) {
            val targetHz = uiBandFrequenciesHz[uiIndex]
            var bestBand = 0
            var bestDist = Long.MAX_VALUE
            for (b in 0 until deviceBandCount) {
                val centerHz = eq.getCenterFreq(b.toShort()) / 1000 // millihertz -> hertz
                val dist = kotlin.math.abs(centerHz.toLong() - targetHz.toLong())
                if (dist < bestDist) {
                    bestDist = dist
                    bestBand = b
                }
            }
            uiToDeviceBand[uiIndex] = bestBand
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        requestedEqEnabled = enabled
        applyEqEnabled()
    }

    private fun applyEqEnabled() {
        try {
            equalizer?.enabled = requestedEqEnabled && !_isBypassed.value
        } catch (t: Throwable) {
            Log.w(TAG, "setEqualizerEnabled failed: ${t.message}")
        }
        refreshActiveEffects()
    }

    /** gainDb expected in the UI's fixed -15..+15 range. */
    fun setBand(uiIndex: Int, gainDb: Float) {
        val eq = equalizer ?: return
        if (uiIndex !in 0 until uiBandCount) return
        val deviceBand = uiToDeviceBand[uiIndex]
        if (deviceBand < 0) return

        val clampedDb = gainDb.coerceIn(-15f, 15f)
        val fraction = (clampedDb + 15f) / 30f
        val minMb = deviceLevelRangeMb[0]
        val maxMb = deviceLevelRangeMb[1]
        val levelMb = (minMb + fraction * (maxMb - minMb)).toInt().toShort()
        try {
            eq.setBandLevel(deviceBand.toShort(), levelMb)
        } catch (t: Throwable) {
            Log.w(TAG, "setBandLevel($deviceBand) failed: ${t.message}")
        }
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        requestedBassEnabled = enabled
        applyBassEnabled()
    }

    private fun applyBassEnabled() {
        try {
            bassBoost?.enabled = requestedBassEnabled && !_isBypassed.value
        } catch (t: Throwable) {
            Log.w(TAG, "setBassBoostEnabled failed: ${t.message}")
        }
        refreshActiveEffects()
    }

    /** percent expected in 0..100 (matches the UI slider). */
    fun setBassBoostStrength(percent: Float) {
        val bb = bassBoost ?: return
        if (!bassBoostStrengthSupported) return
        val permille = (percent.coerceIn(0f, 100f) * 10).toInt().toShort()
        try {
            bb.setStrength(permille)
        } catch (t: Throwable) {
            Log.w(TAG, "BassBoost.setStrength failed: ${t.message}")
        }
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        requestedVirtualizerEnabled = enabled
        applyVirtualizerEnabled()
    }

    private fun applyVirtualizerEnabled() {
        try {
            virtualizer?.enabled = requestedVirtualizerEnabled && !_isBypassed.value
        } catch (t: Throwable) {
            Log.w(TAG, "setVirtualizerEnabled failed: ${t.message}")
        }
        refreshActiveEffects()
    }

    fun setVirtualizerStrength(percent: Float) {
        val vr = virtualizer ?: return
        if (!virtualizerStrengthSupported) return
        val permille = (percent.coerceIn(0f, 100f) * 10).toInt().toShort()
        try {
            vr.setStrength(permille)
        } catch (t: Throwable) {
            Log.w(TAG, "Virtualizer.setStrength failed: ${t.message}")
        }
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        requestedLoudnessEnabled = enabled
        applyLoudnessEnabled()
    }

    private fun applyLoudnessEnabled() {
        try {
            loudnessEnhancer?.enabled = requestedLoudnessEnabled && !_isBypassed.value
        } catch (t: Throwable) {
            Log.w(TAG, "setLoudnessEnabled failed: ${t.message}")
        }
        refreshActiveEffects()
    }

    /**
     * Quick global A/B toggle: silences every effect without forgetting the user's
     * individual settings, so flipping it back restores exactly what was configured before.
     * Driven from the notification's "Bypass" action so the user can compare with/without
     * the EQ without opening the app.
     */
    fun setGlobalBypass(bypassed: Boolean) {
        if (bypassed == _isBypassed.value) return
        _isBypassed.value = bypassed
        applyEqEnabled()
        applyBassEnabled()
        applyVirtualizerEnabled()
        applyLoudnessEnabled()
    }

    private fun refreshActiveEffects() {
        val active = mutableSetOf<String>()
        if (equalizer?.enabled == true) active += "Equalizer"
        if (bassBoost?.enabled == true) active += "Bass Boost"
        if (virtualizer?.enabled == true) active += "Virtualizer"
        if (loudnessEnhancer?.enabled == true) active += "Loudness"
        _activeEffects.value = active
    }

    /**
     * percent expected in 0..100. Mapped conservatively to 0..1500 millibels (0..15dB) target
     * gain — high enough to be genuinely audible, capped well short of the values that start
     * introducing clipping/distortion on typical device DACs.
     */
    fun setLoudnessTarget(percent: Float) {
        val le = loudnessEnhancer ?: return
        val targetMb = (percent.coerceIn(0f, 100f) / 100f * 1500f).toInt()
        try {
            le.setTargetGain(targetMb)
        } catch (t: Throwable) {
            Log.w(TAG, "LoudnessEnhancer.setTargetGain failed: ${t.message}")
        }
    }

    /** Applies a full UI state to the engine in one shot. Safe to call frequently. */
    fun applyState(
        isEnabled: Boolean,
        gains: List<Float>,
        bassBoostEnabled: Boolean,
        bassBoostStrengthPercent: Float,
        loudnessEnabled: Boolean,
        loudnessStrengthPercent: Float,
        virtualizerEnabled: Boolean,
        virtualizerStrengthPercent: Float
    ) {
        setEqualizerEnabled(isEnabled)
        if (isEnabled) {
            for (i in gains.indices) setBand(i, gains[i])
        }
        setBassBoostStrength(bassBoostStrengthPercent)
        setBassBoostEnabled(isEnabled && bassBoostEnabled)
        setLoudnessTarget(loudnessStrengthPercent)
        setLoudnessEnabled(isEnabled && loudnessEnabled)
        setVirtualizerStrength(virtualizerStrengthPercent)
        setVirtualizerEnabled(isEnabled && virtualizerEnabled)
    }

    @Synchronized
    fun release() {
        try { equalizer?.release() } catch (_: Throwable) {}
        try { bassBoost?.release() } catch (_: Throwable) {}
        try { virtualizer?.release() } catch (_: Throwable) {}
        try { loudnessEnhancer?.release() } catch (_: Throwable) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        uiToDeviceBand = IntArray(uiBandCount) { -1 }
        _status.value = Status.NotAttached
        _isBypassed.value = false
        _activeEffects.value = emptySet()
    }
}
