package com.omni.equalizer.audio

import android.media.audiofx.Visualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.hypot
import kotlin.math.log10
import kotlin.math.max

/**
 * Captures REAL FFT data from the global audio mix (session 0) via
 * [android.media.audiofx.Visualizer].
 *
 * This replaces the previous "LiveSpectrumVisualizer", whose bars were animated with
 * `(280..650).random()` millisecond tweens and had no relationship to actual audio.
 *
 * If the OS/OEM refuses to let us attach (missing permission, no audio session, HAL
 * restriction), [levels] simply stays at all-zero. The UI should render that as a flat
 * "no live signal" line rather than faking motion — see [isAvailable].
 */
object OmniVisualizerEngine {

    private const val TAG = "OmniVisualizerEngine"
    private const val GLOBAL_MIX_SESSION = 0

    /** Matches OmniAudioEngine.uiBandCount so the spectrum lines up with the EQ sliders. */
    private const val BAND_COUNT = 10

    private val _levels = MutableStateFlow(FloatArray(BAND_COUNT))
    val levels: StateFlow<FloatArray> = _levels

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable

    private var visualizer: Visualizer? = null

    @Synchronized
    fun attach() {
        if (visualizer != null) return
        try {
            val viz = Visualizer(GLOBAL_MIX_SESSION)
            val range = Visualizer.getCaptureSizeRange()
            viz.captureSize = range[1]

            val maxRate = Visualizer.getMaxCaptureRate()
            val captureRate = (maxRate / 2).coerceAtLeast(1)

            viz.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                    // Not used — we render the frequency-domain view, not the raw waveform.
                }

                override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                    if (fft == null) return
                    _levels.value = bucketFft(fft)
                }
            }, captureRate, /* waveform = */ false, /* fft = */ true)

            viz.enabled = true
            visualizer = viz
            _isAvailable.value = true
        } catch (t: Throwable) {
            Log.w(TAG, "Visualizer unavailable: ${t.message}")
            _isAvailable.value = false
        }
    }

    /**
     * The Android FFT capture packs bins as interleaved (real, imaginary) bytes, with bin 0
     * holding the DC component and the last bin holding Nyquist. We take the magnitude of
     * each complex bin, then average into [BAND_COUNT] log-spaced buckets (low buckets get
     * fewer, more tightly-packed bins; high buckets get more) so the bars roughly track the
     * same 31Hz..16kHz spread as the EQ sliders.
     */
    internal fun bucketFft(fft: ByteArray): FloatArray {
        val n = fft.size / 2
        if (n <= 0) return FloatArray(BAND_COUNT)

        val magnitudes = FloatArray(n)
        for (i in 0 until n) {
            val re = fft[2 * i].toInt()
            val im = if (2 * i + 1 < fft.size) fft[2 * i + 1].toInt() else 0
            magnitudes[i] = hypot(re.toFloat(), im.toFloat())
        }

        val bands = FloatArray(BAND_COUNT)
        for (b in 0 until BAND_COUNT) {
            // Log spacing: later bands cover proportionally more raw bins, similar to how
            // human hearing (and our EQ bands) compress the high end.
            val startFrac = logPosition(b, BAND_COUNT)
            val endFrac = logPosition(b + 1, BAND_COUNT)
            val start = max(1, (startFrac * n).toInt())
            val end = max(start + 1, (endFrac * n).toInt()).coerceAtMost(n)

            var sum = 0f
            for (i in start until end) sum += magnitudes[i]
            val avg = if (end > start) sum / (end - start) else 0f

            // Rough perceptual (log) normalisation into 0..1 for direct UI consumption.
            bands[b] = (log10(1f + avg) / log10(256f)).coerceIn(0f, 1f)
        }
        return bands
    }

    private fun logPosition(index: Int, total: Int): Float {
        val t = index.toFloat() / total
        // Simple ease-in curve approximates log-frequency spacing without needing
        // per-device sample-rate math (we only have relative bin position here).
        return t * t
    }

    @Synchronized
    fun release() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (_: Throwable) {
        }
        visualizer = null
        _isAvailable.value = false
        _levels.value = FloatArray(BAND_COUNT)
    }
}
