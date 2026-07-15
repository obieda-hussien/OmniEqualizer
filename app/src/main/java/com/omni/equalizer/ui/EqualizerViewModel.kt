package com.omni.equalizer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omni.equalizer.audio.OmniAudioEngine
import com.omni.equalizer.audio.OmniVisualizerEngine
import com.omni.equalizer.audio.OmniVolumeController
import com.omni.equalizer.data.EqualizerPreset
import com.omni.equalizer.data.PersistedSettings
import com.omni.equalizer.data.PresetDatabase
import com.omni.equalizer.data.PresetRepository
import com.omni.equalizer.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EqualizerUiState(
    val isEnabled: Boolean = true,
    // 10 band gains in dB (from -15f to +15f)
    val gains: List<Float> = listOf(0.3f, 7.8f, 12.1f, 12.1f, 5.6f, 3.3f, 4.0f, 4.0f, 5.0f, 2.4f),
    
    // FX parameters
    val bassBoost: Float = 40f,
    val bassBoostEnabled: Boolean = false,
    val loudness: Float = 3f,
    val loudnessEnabled: Boolean = false,
    val virtualizer: Float = 48f,
    val virtualizerEnabled: Boolean = true,
    
    // Global parameters
    val volume: Float = 60f,
    val selectedPresetId: Int = -1,
    val selectedPresetName: String = "Custom",
    
    // Smart tuning
    val isSmartOptimized: Boolean = false,
    val smartIntensity: Float = 50f, // Intensity of auto EQ adaptation
    
    // General Settings
    val currentLanguage: String = "en", // Default English
    val currentTheme: String = "Cosmic", // Theme: Cosmic, Dark, Light
    val bassFrequency: String = "80Hz",
    val bassMaxGain: String = "15dB",
    val loudnessMaxGain: String = "20dB",
    val audioBalanceEnabled: Boolean = false,
    val globalMixAlwaysBound: Boolean = false,
    val connectToMusicPlayersOnly: Boolean = false,
    val frameDurationMs: String = "10ms",
    val reverbEnabled: Boolean = false,
    val showVolumeSlider: Boolean = true,
    val use10Bands: Boolean = true,
    val useLegacyEffects: Boolean = false,
    val showNotification: Boolean = true,
    
    // Loading states
    val isSplashActive: Boolean = true,

    // Honest real-engine status — surfaced in the UI instead of silently pretending
    // everything always works.
    val isEngineFullyReal: Boolean = false,
    val engineWarning: String? = null,
    val isSpectrumLive: Boolean = false,

    // Real loudness normalization (AGC-style): keeps perceived volume steady across quiet
    // and loud passages by continuously reading actual measured energy, instead of a fixed
    // static loudness target.
    val autoLoudnessNormalization: Boolean = false,

    // True while the notification's "Bypass" quick-compare toggle is active.
    val isBypassed: Boolean = false
)

@OptIn(FlowPreview::class)
class EqualizerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PresetDatabase.getDatabase(application)
    private val repository = PresetRepository(database.presetDao())
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    /** Real captured FFT-derived levels (10 bands, 0f..1f) — see [OmniVisualizerEngine]. */
    val spectrumLevels: StateFlow<FloatArray> = OmniVisualizerEngine.levels

    // Expose DB presets
    val presets: StateFlow<List<EqualizerPreset>> = repository.allPresets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        OmniVolumeController.attach(application)
        observeBypassState()

        viewModelScope.launch {
            // Restore whatever was saved last session BEFORE seeding/splash logic runs, so the
            // user's actual curve and toggles survive process death instead of resetting to
            // hard-coded defaults every time Android kills the app.
            val saved = settingsRepository.settingsFlow.first()
            if (saved != null) {
                _uiState.value = _uiState.value.copy(
                    isEnabled = saved.isEnabled,
                    gains = saved.gains,
                    bassBoost = saved.bassBoost,
                    bassBoostEnabled = saved.bassBoostEnabled,
                    loudness = saved.loudness,
                    loudnessEnabled = saved.loudnessEnabled,
                    virtualizer = saved.virtualizer,
                    virtualizerEnabled = saved.virtualizerEnabled,
                    selectedPresetId = saved.selectedPresetId,
                    selectedPresetName = saved.selectedPresetName,
                    smartIntensity = saved.smartIntensity,
                    currentLanguage = saved.currentLanguage,
                    currentTheme = saved.currentTheme,
                    bassFrequency = saved.bassFrequency,
                    bassMaxGain = saved.bassMaxGain,
                    loudnessMaxGain = saved.loudnessMaxGain,
                    audioBalanceEnabled = saved.audioBalanceEnabled,
                    globalMixAlwaysBound = saved.globalMixAlwaysBound,
                    connectToMusicPlayersOnly = saved.connectToMusicPlayersOnly,
                    frameDurationMs = saved.frameDurationMs,
                    reverbEnabled = saved.reverbEnabled,
                    showVolumeSlider = saved.showVolumeSlider,
                    use10Bands = saved.use10Bands,
                    useLegacyEffects = saved.useLegacyEffects,
                    showNotification = saved.showNotification
                    // volume deliberately NOT restored from disk — the real system volume
                    // (read below) is always the source of truth, not a stale saved number.
                    // isSmartOptimized deliberately NOT restored — always start with it off so a
                    // killed process doesn't silently resume background adaptive EQ.
                )
                pushStateToEngine()
            }

            // The volume slider reflects the REAL system media volume, not a remembered number.
            OmniVolumeController.getCurrentVolumePercent()?.let { realVolume ->
                _uiState.value = _uiState.value.copy(volume = realVolume)
            }

            // Seed default presets if none exist
            val currentPresets = repository.allPresets.first()
            if (currentPresets.isEmpty()) {
                seedDefaultPresets()
            }
            
            // Deactivate splash after 2.5 seconds
            kotlinx.coroutines.delay(2500)
            _uiState.value = _uiState.value.copy(isSplashActive = false)
        }

        // Auto-save on every change, debounced so rapid slider drags / smart-EQ adaptation
        // don't hammer disk I/O.
        viewModelScope.launch {
            _uiState.debounce(500).collect { state ->
                settingsRepository.save(state.toPersistedSettings())
            }
        }

        // Reflect the REAL engine status in the UI instead of always claiming success.
        viewModelScope.launch {
            OmniAudioEngine.status.collect { status ->
                val (isReal, warning) = when (status) {
                    is OmniAudioEngine.Status.Active -> true to null
                    is OmniAudioEngine.Status.PartiallyActive ->
                        true to "${status.missing.joinToString()} غير مدعوم على هذا الجهاز"
                    is OmniAudioEngine.Status.Unavailable -> false to status.reason
                    is OmniAudioEngine.Status.NotAttached -> false to null
                }
                _uiState.value = _uiState.value.copy(isEngineFullyReal = isReal, engineWarning = warning)
                if (isReal) pushStateToEngine()
            }
        }

        viewModelScope.launch {
            OmniVisualizerEngine.isAvailable.collect { available ->
                _uiState.value = _uiState.value.copy(isSpectrumLive = available)
            }
        }
    }

    private fun EqualizerUiState.toPersistedSettings() = PersistedSettings(
        isEnabled = isEnabled,
        gains = gains,
        bassBoost = bassBoost,
        bassBoostEnabled = bassBoostEnabled,
        loudness = loudness,
        loudnessEnabled = loudnessEnabled,
        virtualizer = virtualizer,
        virtualizerEnabled = virtualizerEnabled,
        selectedPresetId = selectedPresetId,
        selectedPresetName = selectedPresetName,
        smartIntensity = smartIntensity,
        isSmartOptimized = isSmartOptimized,
        currentLanguage = currentLanguage,
        currentTheme = currentTheme,
        bassFrequency = bassFrequency,
        bassMaxGain = bassMaxGain,
        loudnessMaxGain = loudnessMaxGain,
        audioBalanceEnabled = audioBalanceEnabled,
        globalMixAlwaysBound = globalMixAlwaysBound,
        connectToMusicPlayersOnly = connectToMusicPlayersOnly,
        frameDurationMs = frameDurationMs,
        reverbEnabled = reverbEnabled,
        showVolumeSlider = showVolumeSlider,
        use10Bands = use10Bands,
        useLegacyEffects = useLegacyEffects,
        showNotification = showNotification,
        volume = volume
    )

    /** Applies the current UI state to the real audio engine. Idempotent, safe to call often. */
    private fun pushStateToEngine() {
        val s = _uiState.value
        OmniAudioEngine.applyState(
            isEnabled = s.isEnabled,
            gains = s.gains,
            bassBoostEnabled = s.bassBoostEnabled,
            bassBoostStrengthPercent = s.bassBoost,
            loudnessEnabled = s.loudnessEnabled,
            loudnessStrengthPercent = s.loudness,
            virtualizerEnabled = s.virtualizerEnabled,
            virtualizerStrengthPercent = s.virtualizer
        )
    }

    private suspend fun seedDefaultPresets() {
        val defaultPresets = listOf(
            EqualizerPreset(
                name = "Rap", // Rap
                gain31 = 4.5f, gain62 = 8.0f, gain125 = 5.5f, gain250 = 2.0f, gain500 = -1.5f,
                gain1k = -0.5f, gain2k = 3.0f, gain4k = 4.5f, gain8k = 2.0f, gain16k = 6.0f,
                bassBoost = 65f, bassBoostEnabled = true,
                loudness = 5f, loudnessEnabled = true,
                virtualizer = 30f, virtualizerEnabled = true,
                isCustom = false, tags = "BASS, EQ, VIRTUAL"
            ),
            EqualizerPreset(
                name = "General", // General
                gain31 = 0.3f, gain62 = 7.8f, gain125 = 12.1f, gain250 = 12.1f, gain500 = 5.6f,
                gain1k = 3.3f, gain2k = 4.0f, gain4k = 4.0f, gain8k = 5.0f, gain16k = 2.4f,
                bassBoost = 40f, bassBoostEnabled = false,
                loudness = 3f, loudnessEnabled = false,
                virtualizer = 48f, virtualizerEnabled = true,
                isCustom = false, tags = "AUTO APPLY, EQ, VIRTUAL"
            ),
            EqualizerPreset(
                name = "Mahragan", // Mahragan
                gain31 = 9.0f, gain62 = 12.0f, gain125 = 8.0f, gain250 = 3.5f, gain500 = 1.0f,
                gain1k = 2.5f, gain2k = 6.0f, gain4k = 8.0f, gain8k = 10.0f, gain16k = 12.0f,
                bassBoost = 85f, bassBoostEnabled = true,
                loudness = 12f, loudnessEnabled = true,
                virtualizer = 55f, virtualizerEnabled = true,
                isCustom = false, tags = "BASS, EQ, LOUD"
            ),
            EqualizerPreset(
                name = "Natural", // Pop/Natural
                gain31 = 2.0f, gain62 = 4.0f, gain125 = 1.5f, gain250 = -1.0f, gain500 = -2.0f,
                gain1k = -1.0f, gain2k = 1.5f, gain4k = 3.0f, gain8k = 4.0f, gain16k = 3.0f,
                bassBoost = 30f, bassBoostEnabled = false,
                loudness = 2f, loudnessEnabled = false,
                virtualizer = 40f, virtualizerEnabled = true,
                isCustom = false, tags = "EQ"
            ),
            EqualizerPreset(
                name = "Rock", // Rock
                gain31 = 6.0f, gain62 = 4.5f, gain125 = -2.0f, gain250 = -4.0f, gain500 = -1.5f,
                gain1k = 1.0f, gain2k = 3.5f, gain4k = 5.5f, gain8k = 7.0f, gain16k = 6.5f,
                bassBoost = 50f, bassBoostEnabled = true,
                loudness = 4f, loudnessEnabled = true,
                virtualizer = 35f, virtualizerEnabled = false,
                isCustom = false, tags = "BASS, EQ"
            ),
            EqualizerPreset(
                name = "Classical", // Classical
                gain31 = 4.0f, gain62 = 3.0f, gain125 = 2.5f, gain250 = 2.0f, gain500 = -1.0f,
                gain1k = -1.5f, gain2k = -0.5f, gain4k = 1.5f, gain8k = 3.0f, gain16k = 4.5f,
                bassBoost = 15f, bassBoostEnabled = false,
                loudness = 1f, loudnessEnabled = false,
                virtualizer = 60f, virtualizerEnabled = true,
                isCustom = false, tags = "EQ, VIRTUAL"
            )
        )
        for (preset in defaultPresets) {
            repository.insert(preset)
        }
    }

    fun toggleEqualizer() {
        val nextEnabled = !_uiState.value.isEnabled
        _uiState.value = _uiState.value.copy(isEnabled = nextEnabled)
        pushStateToEngine()
        if (nextEnabled && _uiState.value.isSmartOptimized) {
            applySmartOptimization()
        }
    }

    fun updateBand(index: Int, value: Float) {
        if (!_uiState.value.isEnabled) return
        val currentGains = _uiState.value.gains.toMutableList()
        if (index in currentGains.indices) {
            currentGains[index] = value.coerceIn(-15f, 15f)
            _uiState.value = _uiState.value.copy(
                gains = currentGains,
                selectedPresetId = -1,
                selectedPresetName = "Custom"
            )
            OmniAudioEngine.setBand(index, currentGains[index])
        }
    }

    fun updateBassBoost(value: Float) {
        if (!_uiState.value.isEnabled) return
        _uiState.value = _uiState.value.copy(bassBoost = value.coerceIn(0f, 100f))
        OmniAudioEngine.setBassBoostStrength(_uiState.value.bassBoost)
    }

    fun toggleBassBoost() {
        if (!_uiState.value.isEnabled) return
        _uiState.value = _uiState.value.copy(bassBoostEnabled = !_uiState.value.bassBoostEnabled)
        OmniAudioEngine.setBassBoostEnabled(_uiState.value.bassBoostEnabled)
    }

    fun updateLoudness(value: Float) {
        if (!_uiState.value.isEnabled) return
        _uiState.value = _uiState.value.copy(loudness = value.coerceIn(0f, 100f))
        OmniAudioEngine.setLoudnessTarget(_uiState.value.loudness)
    }

    fun toggleLoudness() {
        if (!_uiState.value.isEnabled) return
        _uiState.value = _uiState.value.copy(loudnessEnabled = !_uiState.value.loudnessEnabled)
        OmniAudioEngine.setLoudnessEnabled(_uiState.value.loudnessEnabled)
    }

    fun updateVirtualizer(value: Float) {
        if (!_uiState.value.isEnabled) return
        _uiState.value = _uiState.value.copy(virtualizer = value.coerceIn(0f, 100f))
        OmniAudioEngine.setVirtualizerStrength(_uiState.value.virtualizer)
    }

    fun toggleVirtualizer() {
        if (!_uiState.value.isEnabled) return
        _uiState.value = _uiState.value.copy(virtualizerEnabled = !_uiState.value.virtualizerEnabled)
        OmniAudioEngine.setVirtualizerEnabled(_uiState.value.virtualizerEnabled)
    }

    fun updateVolume(value: Float) {
        val clamped = value.coerceIn(0f, 100f)
        _uiState.value = _uiState.value.copy(volume = clamped)
        OmniVolumeController.setVolumePercent(clamped)
    }

    fun applyPreset(preset: EqualizerPreset) {
        if (!_uiState.value.isEnabled) return
        _uiState.value = _uiState.value.copy(
            gains = listOf(
                preset.gain31, preset.gain62, preset.gain125, preset.gain250, preset.gain500,
                preset.gain1k, preset.gain2k, preset.gain4k, preset.gain8k, preset.gain16k
            ),
            bassBoost = preset.bassBoost,
            bassBoostEnabled = preset.bassBoostEnabled,
            loudness = preset.loudness,
            loudnessEnabled = preset.loudnessEnabled,
            virtualizer = preset.virtualizer,
            virtualizerEnabled = preset.virtualizerEnabled,
            selectedPresetId = preset.id,
            selectedPresetName = preset.name,
            isSmartOptimized = false // Presets override smart optimization
        )
        pushStateToEngine()
    }

    private var smartJob: kotlinx.coroutines.Job? = null
    private var smartBaselineGains: List<Float>? = null
    private var lastSmartUpdateMs = 0L

    fun toggleSmartOptimization() {
        if (!_uiState.value.isEnabled) return
        val nextSmart = !_uiState.value.isSmartOptimized
        _uiState.value = _uiState.value.copy(
            isSmartOptimized = nextSmart,
            selectedPresetId = if (nextSmart) -1 else _uiState.value.selectedPresetId,
            selectedPresetName = if (nextSmart) "Smart AI" else _uiState.value.selectedPresetName
        )
        if (nextSmart) {
            applySmartOptimization()
        } else {
            smartJob?.cancel()
            smartJob = null
            smartBaselineGains = null
        }
    }

    /**
     * Real adaptive EQ: reads the ACTUAL captured spectrum from [OmniVisualizerEngine] (real
     * FFT of session 0, not a fabricated sine wave) and gently balances the curve toward it —
     * bands that are genuinely quiet in the current mix get nudged up, loud bands are left
     * alone, scaled by [EqualizerUiState.smartIntensity]. This is a simple heuristic, not a
     * trained model — it is intentionally described as "adaptive", not oversold as true AI.
     *
     * If the visualizer can't attach (permission denied / device restriction), we fall back to
     * a single static, clearly-labelled "balanced" curve instead of animating fake motion.
     */
    private fun applySmartOptimization() {
        smartJob?.cancel()
        smartBaselineGains = _uiState.value.gains

        val fallbackCurve = listOf(4f, 5.5f, 3f, 1f, -1f, 0.5f, 2f, 3f, 4f, 4.5f)

        smartJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            if (!OmniVisualizerEngine.isAvailable.value) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(gains = fallbackCurve)
                    OmniAudioEngine.applyState(
                        isEnabled = _uiState.value.isEnabled,
                        gains = fallbackCurve,
                        bassBoostEnabled = _uiState.value.bassBoostEnabled,
                        bassBoostStrengthPercent = _uiState.value.bassBoost,
                        loudnessEnabled = _uiState.value.loudnessEnabled,
                        loudnessStrengthPercent = _uiState.value.loudness,
                        virtualizerEnabled = _uiState.value.virtualizerEnabled,
                        virtualizerStrengthPercent = _uiState.value.virtualizer
                    )
                }
                return@launch
            }

            OmniVisualizerEngine.levels.collect { measured ->
                if (!_uiState.value.isEnabled || !_uiState.value.isSmartOptimized) return@collect

                val now = System.currentTimeMillis()
                if (now - lastSmartUpdateMs < 200) return@collect // ~5 updates/sec, smooth not jittery
                lastSmartUpdateMs = now

                val baseline = smartBaselineGains ?: fallbackCurve
                val intensity = (_uiState.value.smartIntensity / 100f).coerceIn(0f, 1f)
                val avgEnergy = measured.average().toFloat()

                val adaptedGains = baseline.mapIndexed { index, baseGain ->
                    val measuredLevel = measured.getOrElse(index) { avgEnergy }
                    // Genuinely quiet band relative to the rest of the real mix -> nudge up.
                    val deficit = (avgEnergy - measuredLevel).coerceIn(-1f, 1f)
                    (baseGain + deficit * 6f * intensity).coerceIn(-15f, 15f)
                }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(gains = adaptedGains)
                    for (i in adaptedGains.indices) OmniAudioEngine.setBand(i, adaptedGains[i])
                }
            }
        }
    }

    private var loudnessAgcJob: kotlinx.coroutines.Job? = null
    private var lastAgcUpdateMs = 0L
    // Rolling long-term average energy, updated slowly so short transients (a single loud
    // drum hit) don't yank the loudness target around — only the sustained level does.
    private var rollingAverageEnergy = 0.35f

    fun toggleAutoLoudnessNormalization() {
        if (!_uiState.value.isEnabled) return
        val next = !_uiState.value.autoLoudnessNormalization
        _uiState.value = _uiState.value.copy(autoLoudnessNormalization = next)
        if (next) {
            startAutoLoudnessNormalization()
        } else {
            loudnessAgcJob?.cancel()
            loudnessAgcJob = null
        }
    }

    /**
     * Real loudness normalization (a lightweight AGC — automatic gain control): reads the
     * ACTUAL measured mix energy from [OmniVisualizerEngine] and continuously retargets
     * [OmniAudioEngine]'s LoudnessEnhancer so quiet passages get boosted more and already-loud
     * passages get boosted less, keeping perceived volume steadier across a whole playlist —
     * instead of one fixed loudness percentage that's too weak on quiet tracks and too strong
     * on loud ones.
     *
     * This is independent from Smart EQ: that one reshapes the *spectral balance* (which
     * bands are loud vs quiet); this one manages *overall* loudness consistency.
     */
    private fun startAutoLoudnessNormalization() {
        loudnessAgcJob?.cancel()
        if (!OmniVisualizerEngine.isAvailable.value) return // no real signal to react to — don't fake it

        loudnessAgcJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            OmniVisualizerEngine.levels.collect { measured ->
                if (!_uiState.value.isEnabled || !_uiState.value.autoLoudnessNormalization) return@collect

                val now = System.currentTimeMillis()
                if (now - lastAgcUpdateMs < 300) return@collect
                lastAgcUpdateMs = now

                val currentEnergy = measured.average().toFloat()
                // Slow exponential moving average -> reacts to the song's overall level, not
                // individual beats.
                rollingAverageEnergy = rollingAverageEnergy * 0.9f + currentEnergy * 0.1f

                // Quiet sustained material (low rollingAverageEnergy) -> push loudness target
                // up; already-hot material -> ease it back down. Inverse relationship, clamped
                // to a sane 0..100 range matching the existing loudness slider's scale.
                val targetLoudnessPercent = ((1f - rollingAverageEnergy) * 90f + 10f).coerceIn(10f, 100f)

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(loudness = targetLoudnessPercent, loudnessEnabled = true)
                    OmniAudioEngine.setLoudnessTarget(targetLoudnessPercent)
                    OmniAudioEngine.setLoudnessEnabled(true)
                }
            }
        }
    }

    /** Mirrors [OmniAudioEngine.isBypassed] into the UI state — driven from the notification. */
    fun observeBypassState() {
        viewModelScope.launch {
            OmniAudioEngine.isBypassed.collect { bypassed ->
                _uiState.value = _uiState.value.copy(isBypassed = bypassed)
            }
        }
    }

    /** Same bypass the notification's quick-action drives — exposed here so the in-app UI
     *  doesn't need to reach into [OmniAudioEngine] directly. */
    fun toggleBypass() {
        OmniAudioEngine.setGlobalBypass(!_uiState.value.isBypassed)
    }

    fun saveCustomPreset(name: String, tags: String = "BASS, EQ") {
        viewModelScope.launch {
            val state = _uiState.value
            val newPreset = EqualizerPreset(
                name = name,
                gain31 = state.gains[0],
                gain62 = state.gains[1],
                gain125 = state.gains[2],
                gain250 = state.gains[3],
                gain500 = state.gains[4],
                gain1k = state.gains[5],
                gain2k = state.gains[6],
                gain4k = state.gains[7],
                gain8k = state.gains[8],
                gain16k = state.gains[9],
                bassBoost = state.bassBoost,
                bassBoostEnabled = state.bassBoostEnabled,
                loudness = state.loudness,
                loudnessEnabled = state.loudnessEnabled,
                virtualizer = state.virtualizer,
                virtualizerEnabled = state.virtualizerEnabled,
                isCustom = true,
                tags = tags
            )
            repository.insert(newPreset)
            // Reload into state
            _uiState.value = _uiState.value.copy(
                selectedPresetName = name
            )
        }
    }

    fun deletePreset(presetId: Int) {
        viewModelScope.launch {
            repository.deleteById(presetId)
            if (_uiState.value.selectedPresetId == presetId) {
                _uiState.value = _uiState.value.copy(
                    selectedPresetId = -1,
                    selectedPresetName = "Custom"
                )
            }
        }
    }

    // Change Settings values
    fun updateLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(currentLanguage = lang)
    }

    fun updateTheme(theme: String) {
        _uiState.value = _uiState.value.copy(currentTheme = theme)
    }

    fun updateBassFrequency(freq: String) {
        _uiState.value = _uiState.value.copy(bassFrequency = freq)
    }

    fun updateBassMaxGain(gain: String) {
        _uiState.value = _uiState.value.copy(bassMaxGain = gain)
    }

    fun updateLoudnessMaxGain(gain: String) {
        _uiState.value = _uiState.value.copy(loudnessMaxGain = gain)
    }

    fun toggleAudioBalance() {
        _uiState.value = _uiState.value.copy(audioBalanceEnabled = !_uiState.value.audioBalanceEnabled)
    }

    fun toggleGlobalMixAlwaysBound() {
        _uiState.value = _uiState.value.copy(globalMixAlwaysBound = !_uiState.value.globalMixAlwaysBound)
    }

    fun toggleConnectToMusicPlayersOnly() {
        _uiState.value = _uiState.value.copy(connectToMusicPlayersOnly = !_uiState.value.connectToMusicPlayersOnly)
    }

    fun updateFrameDurationMs(duration: String) {
        _uiState.value = _uiState.value.copy(frameDurationMs = duration)
    }

    fun toggleReverb() {
        _uiState.value = _uiState.value.copy(reverbEnabled = !_uiState.value.reverbEnabled)
    }

    fun toggleVolumeSlider() {
        _uiState.value = _uiState.value.copy(showVolumeSlider = !_uiState.value.showVolumeSlider)
    }

    fun toggle10Bands() {
        _uiState.value = _uiState.value.copy(use10Bands = !_uiState.value.use10Bands)
    }

    fun toggleLegacyEffects() {
        _uiState.value = _uiState.value.copy(useLegacyEffects = !_uiState.value.useLegacyEffects)
    }

    fun toggleShowNotification() {
        _uiState.value = _uiState.value.copy(showNotification = !_uiState.value.showNotification)
    }
}
