package com.omni.equalizer.ui
import androidx.compose.ui.res.stringResource
import com.omni.equalizer.R

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omni.equalizer.data.EqualizerPreset
import com.omni.equalizer.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2
import kotlin.math.PI

// ── SETTINGS SCREEN ──
@Composable
fun EqualizerSettingsScreen(
    state: EqualizerUiState,
    viewModel: EqualizerViewModel,
    isArabic: Boolean,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121220))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OmniSpacing.lg, vertical = OmniSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = if (isArabic) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = stringResource(R.string.settings),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(modifier = Modifier.size(36.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = OmniSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(OmniSpacing.sm)
            ) {
                // ── Appearance ──
                item { SectionLabel(stringResource(R.string.settings_group_appearance)) }

                item {
                    SettingsDropdownItem(
                        icon = Icons.Rounded.Language,
                        title = stringResource(R.string.language),
                        desc = stringResource(R.string.language_desc),
                        options = listOf("ar", "en"),
                        selectedOption = state.currentLanguage,
                        onOptionSelected = { viewModel.updateLanguage(it) },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsDropdownItem(
                        icon = Icons.Rounded.ColorLens,
                        title = stringResource(R.string.appearance),
                        desc = stringResource(R.string.appearance_desc) + ": " + state.currentTheme,
                        options = listOf("Cosmic", "Dark", "Light"),
                        selectedOption = state.currentTheme,
                        onOptionSelected = { viewModel.updateTheme(it) },
                        isArabic = isArabic
                    )
                }

                item { Spacer(modifier = Modifier.height(OmniSpacing.md)) }

                // ── Audio Engine ──
                item { SectionLabel(stringResource(R.string.settings_group_engine)) }

                item {
                    SettingsDropdownItem(
                        icon = Icons.Rounded.Audiotrack,
                        title = stringResource(R.string.bass_freq),
                        desc = "Default: 80Hz",
                        options = listOf("50Hz", "80Hz", "100Hz", "120Hz"),
                        selectedOption = state.bassFrequency,
                        onOptionSelected = { viewModel.updateBassFrequency(it) },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsDropdownItem(
                        icon = Icons.Rounded.GraphicEq,
                        title = stringResource(R.string.bass_gain),
                        desc = "Default: 15dB",
                        options = listOf("10dB", "15dB", "20dB", "25dB"),
                        selectedOption = state.bassMaxGain,
                        onOptionSelected = { viewModel.updateBassMaxGain(it) },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsDropdownItem(
                        icon = Icons.AutoMirrored.Rounded.VolumeUp,
                        title = stringResource(R.string.loudness_gain),
                        desc = "Default: 20dB",
                        options = listOf("15dB", "20dB", "25dB", "30dB"),
                        selectedOption = state.loudnessMaxGain,
                        onOptionSelected = { viewModel.updateLoudnessMaxGain(it) },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.Balance,
                        title = stringResource(R.string.audio_balance),
                        desc = stringResource(R.string.audio_balance_desc),
                        checked = state.audioBalanceEnabled,
                        onCheckedChange = { viewModel.toggleAudioBalance() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.Merge,
                        title = stringResource(R.string.always_bind_global),
                        desc = stringResource(R.string.always_bind_global_desc),
                        checked = state.globalMixAlwaysBound,
                        onCheckedChange = { viewModel.toggleGlobalMixAlwaysBound() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.MusicNote,
                        title = stringResource(R.string.connect_music_only),
                        desc = stringResource(R.string.connect_music_only_desc),
                        checked = state.connectToMusicPlayersOnly,
                        onCheckedChange = { viewModel.toggleConnectToMusicPlayersOnly() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsDropdownItem(
                        icon = Icons.Rounded.Timelapse,
                        title = stringResource(R.string.frame_duration),
                        desc = "Default: 10ms",
                        options = listOf("5ms", "10ms", "15ms", "20ms"),
                        selectedOption = state.frameDurationMs,
                        onOptionSelected = { viewModel.updateFrameDurationMs(it) },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.Hearing,
                        title = stringResource(R.string.reverb),
                        desc = stringResource(R.string.reverb_desc),
                        checked = state.reverbEnabled,
                        onCheckedChange = { viewModel.toggleReverb() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.GridOn,
                        title = stringResource(R.string.use_10_bands),
                        desc = stringResource(R.string.use_10_bands_desc),
                        checked = state.use10Bands,
                        onCheckedChange = { viewModel.toggle10Bands() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.ReportProblem,
                        title = stringResource(R.string.use_legacy),
                        desc = stringResource(R.string.use_legacy_desc),
                        checked = state.useLegacyEffects,
                        onCheckedChange = { viewModel.toggleLegacyEffects() },
                        isArabic = isArabic
                    )
                }

                item { Spacer(modifier = Modifier.height(OmniSpacing.md)) }

                // ── Notifications ──
                item { SectionLabel(stringResource(R.string.settings_group_notifications)) }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.Notifications,
                        title = stringResource(R.string.notifications),
                        desc = stringResource(R.string.notifications_desc),
                        checked = state.showNotification,
                        onCheckedChange = { viewModel.toggleShowNotification() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.LinearScale,
                        title = stringResource(R.string.volume_bar),
                        desc = stringResource(R.string.volume_bar_desc),
                        checked = state.showVolumeSlider,
                        onCheckedChange = { viewModel.toggleVolumeSlider() },
                        isArabic = isArabic
                    )
                }

                item { Spacer(modifier = Modifier.height(OmniSpacing.md)) }

                // ── Advanced ──
                item { SectionLabel(stringResource(R.string.settings_group_advanced)) }

                item {
                    SettingsClickableItem(
                        icon = Icons.Rounded.Backup,
                        title = stringResource(R.string.backup),
                        desc = stringResource(R.string.backup_desc),
                        onClick = {},
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsClickableItem(
                        icon = Icons.Rounded.Bluetooth,
                        title = stringResource(R.string.saved_bt),
                        desc = stringResource(R.string.saved_bt_desc),
                        onClick = {},
                        isArabic = isArabic
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(OmniSpacing.xxl))
                }
            }
        }
    }
}

// ── SETTINGS DROPDOWN ITEM ──
@Composable
fun SettingsDropdownItem(
    icon: ImageVector,
    title: String,
    desc: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isArabic: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(OmniRadius.medium),
        colors = CardDefaults.cardColors(containerColor = OmniCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OmniCardBorder, RoundedCornerShape(OmniRadius.medium))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(OmniSpacing.xl),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = OmniAccentTeal
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(OmniCardBg)
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

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = if (isArabic) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isArabic) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        Text(desc, color = OmniMutedText, fontSize = 11.sp, textAlign = TextAlign.End)
                    }
                    Icon(imageVector = icon, contentDescription = title, tint = OmniAccentIndigo)
                } else {
                    Icon(imageVector = icon, contentDescription = title, tint = OmniAccentIndigo)
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
                        Text(desc, color = OmniMutedText, fontSize = 11.sp, textAlign = TextAlign.Start)
                    }
                }
            }
        }
    }
}

// ── SETTINGS CLICKABLE ITEM ──
@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit,
    isArabic: Boolean
) {
    Card(
        shape = RoundedCornerShape(OmniRadius.medium),
        colors = CardDefaults.cardColors(containerColor = OmniCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OmniCardBorder, RoundedCornerShape(OmniRadius.medium))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(OmniSpacing.xl),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isArabic) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = OmniAccentTeal
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = if (isArabic) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isArabic) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        Text(desc, color = OmniMutedText, fontSize = 11.sp, textAlign = TextAlign.End)
                    }
                    Icon(imageVector = icon, contentDescription = title, tint = OmniAccentIndigo)
                } else {
                    Icon(imageVector = icon, contentDescription = title, tint = OmniAccentIndigo)
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
                        Text(desc, color = OmniMutedText, fontSize = 11.sp, textAlign = TextAlign.Start)
                    }
                }
            }
        }
    }
}

// ── SETTINGS TOGGLE ITEM ──
@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isArabic: Boolean
) {
    Card(
        shape = RoundedCornerShape(OmniRadius.medium),
        colors = CardDefaults.cardColors(containerColor = OmniCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OmniCardBorder, RoundedCornerShape(OmniRadius.medium))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OmniSpacing.xl),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = OmniAccentTeal,
                    uncheckedThumbColor = OmniMutedText,
                    uncheckedTrackColor = Color(0xFF2E2F45)
                )
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = if (isArabic) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isArabic) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 12.dp, start = 8.dp)
                    ) {
                        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        Text(desc, color = OmniMutedText, fontSize = 11.sp, textAlign = TextAlign.End)
                    }
                    Icon(imageVector = icon, contentDescription = title, tint = OmniAccentIndigo)
                } else {
                    Icon(imageVector = icon, contentDescription = title, tint = OmniAccentIndigo)
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                    ) {
                        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
                        Text(desc, color = OmniMutedText, fontSize = 11.sp, textAlign = TextAlign.Start)
                    }
                }
            }
        }
    }
}
