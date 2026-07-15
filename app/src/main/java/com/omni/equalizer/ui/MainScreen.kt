package com.omni.equalizer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omni.equalizer.R
import com.omni.equalizer.data.EqualizerPreset
import com.omni.equalizer.ui.theme.OmniRadius
import com.omni.equalizer.ui.theme.OmniSpacing

/**
 * ── Redesigned home / equalizer screen ──
 * Same three groups as before (bands / effects / volume) but rebuilt for clarity: one card
 * style throughout ([OmniCard]), one accent used for the primary interactions, decorative
 * motion removed from the background so the actual curve and levels are what draw the eye,
 * and generous spacing so each group reads as its own thing instead of one dense scroll.
 */

private val frequencies = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

@Composable
fun EqualizerMainScreen(
    state: EqualizerUiState,
    presets: List<EqualizerPreset>,
    viewModel: EqualizerViewModel,
    isArabic: Boolean,
    onNavigateToSettings: () -> Unit,
    onShowHelp: () -> Unit,
    onShowPresets: () -> Unit,
    onShowSavePreset: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize().background(OmniBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
        ) {
            TopBar(
                state = state,
                onNavigateToSettings = onNavigateToSettings,
                onShowHelp = onShowHelp
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = OmniSpacing.xl)
            ) {
                // ── SECTION 1: Equalizer Bands ──
                SectionLabel(stringResource(R.string.section_eq_bands))
                OmniCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.eq_toggle),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = state.isEnabled,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleEqualizer()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = OmniAccentIndigo,
                                uncheckedThumbColor = OmniMutedText,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("eq_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(OmniSpacing.lg))

                    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                        AdvancedDspBackgroundVisualizer(
                            isActive = state.isEnabled,
                            modifier = Modifier.fillMaxSize()
                        )
                        EqualizerSplineCurve(
                            gains = state.gains,
                            isEnabled = state.isEnabled,
                            modifier = Modifier.fillMaxSize()
                        )
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            frequencies.forEachIndexed { index, freq ->
                                VerticalEqualizerBand(
                                    freq = freq,
                                    dbValue = state.gains[index],
                                    isEnabled = state.isEnabled,
                                    onValueChange = { newValue -> viewModel.updateBand(index, newValue) },
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(OmniSpacing.lg))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EffectChip(
                            label = stringResource(R.string.smart_btn),
                            icon = Icons.Rounded.AutoAwesome,
                            isOn = state.isSmartOptimized,
                            isEnabled = state.isEnabled,
                            activeColor = OmniAccentTeal,
                            onClick = { viewModel.toggleSmartOptimization() }
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PresetSelectorButton(
                                label = if (state.isSmartOptimized) stringResource(R.string.smart_ai) else state.selectedPresetName,
                                isEnabled = state.isEnabled,
                                onClick = onShowPresets
                            )
                            Spacer(modifier = Modifier.width(OmniSpacing.xs))
                            IconButton(
                                onClick = onShowSavePreset,
                                enabled = state.isEnabled,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = stringResource(R.string.add_preset_btn),
                                    tint = if (state.isEnabled) OmniMutedText else OmniFaintText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(OmniSpacing.xl))

                // ── SECTION 2: Sound Effects ──
                SectionLabel(stringResource(R.string.section_effects))
                OmniCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(OmniSpacing.sm)
                    ) {
                        EffectChip(
                            label = stringResource(R.string.bypass_label),
                            icon = Icons.AutoMirrored.Rounded.CompareArrows,
                            isOn = state.isBypassed,
                            isEnabled = state.isEnabled,
                            activeColor = OmniAccentAmber,
                            onClick = { viewModel.toggleBypass() },
                            modifier = Modifier.weight(1f)
                        )
                        EffectChip(
                            label = stringResource(R.string.auto_loudness_label),
                            icon = Icons.Rounded.GraphicEq,
                            isOn = state.autoLoudnessNormalization,
                            isEnabled = state.isEnabled,
                            activeColor = OmniAccentPink,
                            onClick = { viewModel.toggleAutoLoudnessNormalization() },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(OmniSpacing.lg))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(OmniSpacing.md)
                    ) {
                        CircularControlDial(
                            label = stringResource(R.string.bass_boost),
                            value = state.bassBoost,
                            isEnabled = state.isEnabled && state.bassBoostEnabled,
                            onValueChange = { viewModel.updateBassBoost(it) },
                            switchChecked = state.bassBoostEnabled,
                            onSwitchChange = { viewModel.toggleBassBoost() },
                            color = OmniAccentPink,
                            modifier = Modifier.weight(1f)
                        )
                        CircularControlDial(
                            label = stringResource(R.string.loudness),
                            value = state.loudness,
                            isEnabled = state.isEnabled && state.loudnessEnabled,
                            onValueChange = { viewModel.updateLoudness(it) },
                            switchChecked = state.loudnessEnabled,
                            onSwitchChange = { viewModel.toggleLoudness() },
                            color = OmniAccentIndigo,
                            modifier = Modifier.weight(1f)
                        )
                        CircularControlDial(
                            label = stringResource(R.string.virtualizer),
                            value = state.virtualizer,
                            isEnabled = state.isEnabled && state.virtualizerEnabled,
                            onValueChange = { viewModel.updateVirtualizer(it) },
                            switchChecked = state.virtualizerEnabled,
                            onSwitchChange = { viewModel.toggleVirtualizer() },
                            color = OmniAccentTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(OmniSpacing.xl))

                // ── SECTION 3: System Volume ──
                if (state.showVolumeSlider) {
                    SectionLabel(stringResource(R.string.section_volume))
                    OmniCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = null,
                                    tint = OmniAccentIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(OmniSpacing.sm))
                                Text(
                                    text = stringResource(R.string.volume),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "${state.volume.toInt()}%",
                                color = OmniMutedText,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Slider(
                            value = state.volume,
                            onValueChange = { viewModel.updateVolume(it) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = OmniAccentIndigo,
                                activeTrackColor = OmniAccentIndigo,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("volume_slider")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(OmniSpacing.xxl))
            }

            // ── Honest engine status banner — only shown when there's something to say ──
            AnimatedVisibility(visible = state.engineWarning != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = OmniSpacing.xl, vertical = OmniSpacing.sm)
                        .clip(RoundedCornerShape(OmniRadius.medium))
                        .background(OmniAccentAmber.copy(alpha = 0.10f))
                        .padding(horizontal = OmniSpacing.md, vertical = OmniSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = OmniAccentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(OmniSpacing.sm))
                    Text(
                        text = state.engineWarning.orEmpty(),
                        color = OmniAccentAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Bottom status bar: live spectrum + a single quiet mode label ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OmniCardBg)
                    .padding(horizontal = OmniSpacing.xl, vertical = OmniSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(OmniSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = OmniMutedText,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.global_mix),
                    color = OmniMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                LiveSpectrumVisualizer(
                    isActive = state.isEnabled,
                    levels = viewModel.spectrumLevels.collectAsState().value,
                    modifier = Modifier.weight(1f).height(22.dp)
                )

                Text(
                    text = stringResource(R.string.general_mode),
                    color = if (state.isEnabled) OmniAccentTeal else OmniMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    state: EqualizerUiState,
    onNavigateToSettings: () -> Unit,
    onShowHelp: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OmniSpacing.lg, vertical = OmniSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(OmniSpacing.xs)) {
            OmniCircularIconButton(
                icon = Icons.Rounded.Settings,
                contentDescription = "Settings",
                onClick = onNavigateToSettings,
                testTag = "settings_button"
            )
            OmniCircularIconButton(
                icon = Icons.AutoMirrored.Rounded.HelpOutline,
                contentDescription = "Help",
                onClick = onShowHelp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.title),
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            StatusPill(state = state)
        }
    }
}

@Composable
private fun StatusPill(state: EqualizerUiState) {
    val (dotColor, label) = when {
        state.isBypassed -> OmniAccentAmber to stringResource(R.string.bypassed_badge)
        state.isEnabled && state.isEngineFullyReal -> OmniAccentTeal to stringResource(R.string.engine_active_badge)
        state.isEnabled -> OmniAccentRed to stringResource(R.string.engine_unavailable_badge)
        else -> OmniMutedText to stringResource(R.string.custom)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(OmniRadius.pill))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(horizontal = OmniSpacing.sm, vertical = 3.dp)
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dotColor))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = OmniMutedText, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

/** Pill-shaped quick toggle — used for Smart Auto, Bypass, and Auto Loudness. Flat fill when
 *  off, soft tinted fill when on; no borders, so a row of these reads as one light group. */
@Composable
private fun EffectChip(
    label: String,
    icon: ImageVector,
    isOn: Boolean,
    isEnabled: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(OmniRadius.pill))
            .background(if (isOn) activeColor.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.05f))
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = OmniSpacing.md, vertical = OmniSpacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isOn) activeColor else OmniMutedText,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(OmniSpacing.xs))
        Text(
            text = label,
            color = if (isOn) Color.White else OmniMutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** The active-preset control on the equalizer card — a single readable pill instead of a
 *  cramped 90dp-wide truncated label. */
@Composable
private fun PresetSelectorButton(
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(OmniRadius.pill))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = OmniSpacing.md, vertical = OmniSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.GraphicEq,
            contentDescription = null,
            tint = OmniAccentTeal,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = OmniAccentTeal,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 130.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Rounded.ArrowDropDown,
            contentDescription = stringResource(R.string.presets),
            tint = OmniMutedText,
            modifier = Modifier.size(16.dp)
        )
    }
}
