package com.omni.equalizer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "omni_equalizer_settings")

/**
 * Plain data holder for everything we persist. Kept independent of [com.omni.equalizer.ui.EqualizerUiState]
 * so the data layer doesn't depend on the UI layer — the ViewModel maps between the two.
 */
data class PersistedSettings(
    val isEnabled: Boolean = true,
    val gains: List<Float> = listOf(0.3f, 7.8f, 12.1f, 12.1f, 5.6f, 3.3f, 4.0f, 4.0f, 5.0f, 2.4f),
    val bassBoost: Float = 40f,
    val bassBoostEnabled: Boolean = false,
    val loudness: Float = 3f,
    val loudnessEnabled: Boolean = false,
    val virtualizer: Float = 48f,
    val virtualizerEnabled: Boolean = true,
    val selectedPresetId: Int = -1,
    val selectedPresetName: String = "Custom",
    val smartIntensity: Float = 50f,
    val isSmartOptimized: Boolean = false,
    val currentLanguage: String = "en",
    val currentTheme: String = "Cosmic",
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
    val volume: Float = 60f
)

/**
 * Persists the equalizer's live tuning and general settings across process death using Jetpack
 * DataStore. Before this, EVERY setting (theme, language, EQ curve, FX toggles, volume...) was
 * held only in an in-memory [kotlinx.coroutines.flow.MutableStateFlow] and silently reset the
 * moment Android killed the app's process — a real, user-visible data-loss bug, not a
 * hypothetical one.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val GAINS = stringPreferencesKey("gains")
        val BASS_BOOST = floatPreferencesKey("bass_boost")
        val BASS_BOOST_ENABLED = booleanPreferencesKey("bass_boost_enabled")
        val LOUDNESS = floatPreferencesKey("loudness")
        val LOUDNESS_ENABLED = booleanPreferencesKey("loudness_enabled")
        val VIRTUALIZER = floatPreferencesKey("virtualizer")
        val VIRTUALIZER_ENABLED = booleanPreferencesKey("virtualizer_enabled")
        val SELECTED_PRESET_ID = intPreferencesKey("selected_preset_id")
        val SELECTED_PRESET_NAME = stringPreferencesKey("selected_preset_name")
        val SMART_INTENSITY = floatPreferencesKey("smart_intensity")
        val IS_SMART_OPTIMIZED = booleanPreferencesKey("is_smart_optimized")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME = stringPreferencesKey("theme")
        val BASS_FREQUENCY = stringPreferencesKey("bass_frequency")
        val BASS_MAX_GAIN = stringPreferencesKey("bass_max_gain")
        val LOUDNESS_MAX_GAIN = stringPreferencesKey("loudness_max_gain")
        val AUDIO_BALANCE_ENABLED = booleanPreferencesKey("audio_balance_enabled")
        val GLOBAL_MIX_ALWAYS_BOUND = booleanPreferencesKey("global_mix_always_bound")
        val CONNECT_TO_MUSIC_PLAYERS_ONLY = booleanPreferencesKey("connect_to_music_players_only")
        val FRAME_DURATION_MS = stringPreferencesKey("frame_duration_ms")
        val REVERB_ENABLED = booleanPreferencesKey("reverb_enabled")
        val SHOW_VOLUME_SLIDER = booleanPreferencesKey("show_volume_slider")
        val USE_10_BANDS = booleanPreferencesKey("use_10_bands")
        val USE_LEGACY_EFFECTS = booleanPreferencesKey("use_legacy_effects")
        val SHOW_NOTIFICATION = booleanPreferencesKey("show_notification")
        val VOLUME = floatPreferencesKey("volume")
    }

    /** Null on first-ever launch (no saved data yet) — caller should fall back to defaults. */
    val settingsFlow: Flow<PersistedSettings?> = context.settingsDataStore.data.map { prefs ->
        val gainsRaw = prefs[Keys.GAINS] ?: return@map null
        PersistedSettings(
            isEnabled = prefs[Keys.IS_ENABLED] ?: true,
            gains = gainsRaw.split(",").mapNotNull { it.toFloatOrNull() }
                .let { if (it.size == 10) it else PersistedSettings().gains },
            bassBoost = prefs[Keys.BASS_BOOST] ?: 40f,
            bassBoostEnabled = prefs[Keys.BASS_BOOST_ENABLED] ?: false,
            loudness = prefs[Keys.LOUDNESS] ?: 3f,
            loudnessEnabled = prefs[Keys.LOUDNESS_ENABLED] ?: false,
            virtualizer = prefs[Keys.VIRTUALIZER] ?: 48f,
            virtualizerEnabled = prefs[Keys.VIRTUALIZER_ENABLED] ?: true,
            selectedPresetId = prefs[Keys.SELECTED_PRESET_ID] ?: -1,
            selectedPresetName = prefs[Keys.SELECTED_PRESET_NAME] ?: "Custom",
            smartIntensity = prefs[Keys.SMART_INTENSITY] ?: 50f,
            isSmartOptimized = prefs[Keys.IS_SMART_OPTIMIZED] ?: false,
            currentLanguage = prefs[Keys.LANGUAGE] ?: "en",
            currentTheme = prefs[Keys.THEME] ?: "Cosmic",
            bassFrequency = prefs[Keys.BASS_FREQUENCY] ?: "80Hz",
            bassMaxGain = prefs[Keys.BASS_MAX_GAIN] ?: "15dB",
            loudnessMaxGain = prefs[Keys.LOUDNESS_MAX_GAIN] ?: "20dB",
            audioBalanceEnabled = prefs[Keys.AUDIO_BALANCE_ENABLED] ?: false,
            globalMixAlwaysBound = prefs[Keys.GLOBAL_MIX_ALWAYS_BOUND] ?: false,
            connectToMusicPlayersOnly = prefs[Keys.CONNECT_TO_MUSIC_PLAYERS_ONLY] ?: false,
            frameDurationMs = prefs[Keys.FRAME_DURATION_MS] ?: "10ms",
            reverbEnabled = prefs[Keys.REVERB_ENABLED] ?: false,
            showVolumeSlider = prefs[Keys.SHOW_VOLUME_SLIDER] ?: true,
            use10Bands = prefs[Keys.USE_10_BANDS] ?: true,
            useLegacyEffects = prefs[Keys.USE_LEGACY_EFFECTS] ?: false,
            showNotification = prefs[Keys.SHOW_NOTIFICATION] ?: true,
            volume = prefs[Keys.VOLUME] ?: 60f
        )
    }

    suspend fun save(settings: PersistedSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.IS_ENABLED] = settings.isEnabled
            prefs[Keys.GAINS] = settings.gains.joinToString(",")
            prefs[Keys.BASS_BOOST] = settings.bassBoost
            prefs[Keys.BASS_BOOST_ENABLED] = settings.bassBoostEnabled
            prefs[Keys.LOUDNESS] = settings.loudness
            prefs[Keys.LOUDNESS_ENABLED] = settings.loudnessEnabled
            prefs[Keys.VIRTUALIZER] = settings.virtualizer
            prefs[Keys.VIRTUALIZER_ENABLED] = settings.virtualizerEnabled
            prefs[Keys.SELECTED_PRESET_ID] = settings.selectedPresetId
            prefs[Keys.SELECTED_PRESET_NAME] = settings.selectedPresetName
            prefs[Keys.SMART_INTENSITY] = settings.smartIntensity
            prefs[Keys.IS_SMART_OPTIMIZED] = settings.isSmartOptimized
            prefs[Keys.LANGUAGE] = settings.currentLanguage
            prefs[Keys.THEME] = settings.currentTheme
            prefs[Keys.BASS_FREQUENCY] = settings.bassFrequency
            prefs[Keys.BASS_MAX_GAIN] = settings.bassMaxGain
            prefs[Keys.LOUDNESS_MAX_GAIN] = settings.loudnessMaxGain
            prefs[Keys.AUDIO_BALANCE_ENABLED] = settings.audioBalanceEnabled
            prefs[Keys.GLOBAL_MIX_ALWAYS_BOUND] = settings.globalMixAlwaysBound
            prefs[Keys.CONNECT_TO_MUSIC_PLAYERS_ONLY] = settings.connectToMusicPlayersOnly
            prefs[Keys.FRAME_DURATION_MS] = settings.frameDurationMs
            prefs[Keys.REVERB_ENABLED] = settings.reverbEnabled
            prefs[Keys.SHOW_VOLUME_SLIDER] = settings.showVolumeSlider
            prefs[Keys.USE_10_BANDS] = settings.use10Bands
            prefs[Keys.USE_LEGACY_EFFECTS] = settings.useLegacyEffects
            prefs[Keys.SHOW_NOTIFICATION] = settings.showNotification
            prefs[Keys.VOLUME] = settings.volume
        }
    }
}
