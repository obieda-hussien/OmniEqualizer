package com.omni.equalizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material.icons.rounded.Merge
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omni.equalizer.R
import com.omni.equalizer.ui.theme.OmniSpacing

/**
 * ── Redesigned settings screen ──
 * The old screen wrapped *every single row* in its own bordered card — visually that reads as
 * ~15 competing boxes stacked on top of each other. This version uses one grouped card per
 * section ([OmniGroupedCard]) with hairline dividers between rows, which is what makes a long
 * settings list feel calm instead of busy — the grouping communicates structure, not the
 * borders.
 */
@Composable
fun EqualizerSettingsScreen(
    state: EqualizerUiState,
    viewModel: EqualizerViewModel,
    isArabic: Boolean,
    onNavigateBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(OmniBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OmniSpacing.lg, vertical = OmniSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OmniCircularIconButton(
                    icon = if (isArabic) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onNavigateBack,
                    testTag = "back_button"
                )
                Text(
                    text = stringResource(R.string.settings),
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(modifier = Modifier.size(40.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = OmniSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(OmniSpacing.xl)
            ) {
                // ── Appearance ──
                item {
                    Column {
                        SectionLabel(stringResource(R.string.settings_group_appearance))
                        OmniGroupedCard(
                            rows = omniRows(
                                {
                                    SettingsDropdownRow(
                                        icon = Icons.Rounded.Language,
                                        title = stringResource(R.string.language),
                                        desc = stringResource(R.string.language_desc),
                                        options = listOf("ar", "en"),
                                        selectedOption = state.currentLanguage,
                                        onOptionSelected = { viewModel.updateLanguage(it) },
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsDropdownRow(
                                        icon = Icons.Rounded.ColorLens,
                                        title = stringResource(R.string.appearance),
                                        desc = stringResource(R.string.appearance_desc),
                                        options = listOf("Cosmic", "Dark", "Light"),
                                        selectedOption = state.currentTheme,
                                        onOptionSelected = { viewModel.updateTheme(it) },
                                        isArabic = isArabic
                                    )
                                }
                            )
                        )
                    }
                }

                // ── Audio Engine ──
                item {
                    Column {
                        SectionLabel(stringResource(R.string.settings_group_engine))
                        OmniGroupedCard(
                            rows = omniRows(
                                {
                                    SettingsDropdownRow(
                                        icon = Icons.Rounded.Audiotrack,
                                        title = stringResource(R.string.bass_freq),
                                        desc = "Default: 80Hz",
                                        options = listOf("50Hz", "80Hz", "100Hz", "120Hz"),
                                        selectedOption = state.bassFrequency,
                                        onOptionSelected = { viewModel.updateBassFrequency(it) },
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsDropdownRow(
                                        icon = Icons.Rounded.GraphicEq,
                                        title = stringResource(R.string.bass_gain),
                                        desc = "Default: 15dB",
                                        options = listOf("10dB", "15dB", "20dB", "25dB"),
                                        selectedOption = state.bassMaxGain,
                                        onOptionSelected = { viewModel.updateBassMaxGain(it) },
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsDropdownRow(
                                        icon = Icons.AutoMirrored.Rounded.VolumeUp,
                                        title = stringResource(R.string.loudness_gain),
                                        desc = "Default: 20dB",
                                        options = listOf("15dB", "20dB", "25dB", "30dB"),
                                        selectedOption = state.loudnessMaxGain,
                                        onOptionSelected = { viewModel.updateLoudnessMaxGain(it) },
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsToggleRow(
                                        icon = Icons.Rounded.Balance,
                                        title = stringResource(R.string.audio_balance),
                                        desc = stringResource(R.string.audio_balance_desc),
                                        checked = state.audioBalanceEnabled,
                                        onCheckedChange = { viewModel.toggleAudioBalance() },
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsToggleRow(
                                        icon = Icons.Rounded.Merge,
                                        title = stringResource(R.string.always_bind_global),
                                        desc = stringResource(R.string.always_bind_global_desc),
                                        checked = state.globalMixAlwaysBound,
                                        onCheckedChange = { viewModel.toggleGlobalMixAlwaysBound() },
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsToggleRow(
                                        icon = Icons.Rounded.MusicNote,
                                        title = stringResource(R.string.connect_music_only),
                                        desc = stringResource(R.string.connect_music_only_desc),
                                        checked = state.connectToMusicPlayersOnly,
                                        onCheckedChange = { viewModel.toggleConnectToMusicPlayersOnly() },
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsDropdownRow(
                                        icon = Icons.Rounded.Timelapse,
                                        title = stringResource(R.string.frame_duration),
                                        desc = "Default: 10ms",
                                        options = listOf("5ms", "10ms", "15ms", "20ms"),
                                        selectedOption = state.frameDurationMs,
                                        onOptionSelected = { viewModel.updateFrameDurationMs(it) },
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsToggleRow(
                                        icon = Icons.Rounded.Hearing,
                                        title = stringResource(R.string.reverb),
                                        desc = stringResource(R.string.reverb_desc),
                                        checked = state.reverbEnabled,
                                        onCheckedChange = { viewModel.toggleReverb() },
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsToggleRow(
                                        icon = Icons.Rounded.GridOn,
                                        title = stringResource(R.string.use_10_bands),
                                        desc = stringResource(R.string.use_10_bands_desc),
                                        checked = state.use10Bands,
                                        onCheckedChange = { viewModel.toggle10Bands() },
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsToggleRow(
                                        icon = Icons.Rounded.ReportProblem,
                                        title = stringResource(R.string.use_legacy),
                                        desc = stringResource(R.string.use_legacy_desc),
                                        checked = state.useLegacyEffects,
                                        onCheckedChange = { viewModel.toggleLegacyEffects() },
                                        isArabic = isArabic,
                                        iconTint = OmniAccentAmber
                                    )
                                }
                            )
                        )
                    }
                }

                // ── Notifications ──
                item {
                    Column {
                        SectionLabel(stringResource(R.string.settings_group_notifications))
                        OmniGroupedCard(
                            rows = omniRows(
                                {
                                    SettingsToggleRow(
                                        icon = Icons.Rounded.Notifications,
                                        title = stringResource(R.string.notifications),
                                        desc = stringResource(R.string.notifications_desc),
                                        checked = state.showNotification,
                                        onCheckedChange = { viewModel.toggleShowNotification() },
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsToggleRow(
                                        icon = Icons.Rounded.LinearScale,
                                        title = stringResource(R.string.volume_bar),
                                        desc = stringResource(R.string.volume_bar_desc),
                                        checked = state.showVolumeSlider,
                                        onCheckedChange = { viewModel.toggleVolumeSlider() },
                                        isArabic = isArabic
                                    )
                                }
                            )
                        )
                    }
                }

                // ── Advanced ──
                item {
                    Column {
                        SectionLabel(stringResource(R.string.settings_group_advanced))
                        OmniGroupedCard(
                            rows = omniRows(
                                {
                                    SettingsClickableRow(
                                        icon = Icons.Rounded.Backup,
                                        title = stringResource(R.string.backup),
                                        desc = stringResource(R.string.backup_desc),
                                        onClick = {},
                                        isArabic = isArabic
                                    )
                                },
                                {
                                    SettingsClickableRow(
                                        icon = Icons.Rounded.Bluetooth,
                                        title = stringResource(R.string.saved_bt),
                                        desc = stringResource(R.string.saved_bt_desc),
                                        onClick = {},
                                        isArabic = isArabic
                                    )
                                }
                            )
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(OmniSpacing.xxl)) }
            }
        }
    }
}

// ── SETTINGS ROW: DROPDOWN ──
@Composable
fun SettingsDropdownRow(
    icon: ImageVector,
    title: String,
    desc: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isArabic: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    OmniListRow(
        icon = icon,
        title = title,
        desc = desc,
        isArabic = isArabic,
        onClick = { expanded = true }
    ) {
        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedOption.uppercase(),
                    color = OmniAccentTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = OmniMutedText,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(OmniCardBgElevated)
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt.uppercase(), color = Color.White, fontWeight = FontWeight.Bold) },
                        onClick = {
                            onOptionSelected(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ── SETTINGS ROW: NAVIGATE / CLICKABLE ──
@Composable
fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit,
    isArabic: Boolean
) {
    OmniListRow(
        icon = icon,
        title = title,
        desc = desc,
        isArabic = isArabic,
        onClick = onClick
    ) {
        Icon(
            imageVector = if (isArabic) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = OmniMutedText,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ── SETTINGS ROW: TOGGLE ──
@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isArabic: Boolean,
    iconTint: Color = OmniAccentIndigo
) {
    OmniListRow(
        icon = icon,
        title = title,
        desc = desc,
        isArabic = isArabic,
        iconTint = iconTint
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OmniAccentTeal,
                uncheckedThumbColor = OmniMutedText,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}
