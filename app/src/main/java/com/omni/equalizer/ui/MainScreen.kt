package com.omni.equalizer.ui

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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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

// ── Shared visual constants for this screen's redesign (see also SharedComponents.kt) ──
private val CardBg = Color(0xFF171826)
private val CardBorder = Color(0xFFFFFFFF).copy(alpha = OmniElevation.strokeAlpha)
private val MutedText = Color(0xFF8B8DA8)
private val AccentIndigo = Color(0xFF8B7FF5)
private val AccentTeal = Color(0xFF00D9A6)
private val AccentPink = Color(0xFFFF6B9D)

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(OmniRadius.large),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(OmniRadius.large))
    ) {
        Column(modifier = Modifier.padding(OmniSpacing.lg), content = content)
    }
}

@Composable
private fun CompactToggleChip(
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
            .clip(RoundedCornerShape(OmniRadius.small))
            .background(if (isOn) activeColor.copy(alpha = 0.18f) else Color(0xFF20222E))
            .border(
                1.dp,
                if (isOn) activeColor.copy(alpha = 0.45f) else CardBorder,
                RoundedCornerShape(OmniRadius.small)
            )
            .clickable(enabled = isEnabled) { onClick() }
            .padding(horizontal = OmniSpacing.md, vertical = OmniSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isOn) activeColor else MutedText,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(OmniSpacing.xs))
        Text(
            text = label,
            color = if (isOn) Color.White else MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── MAIN EQUALIZER SCREEN ──
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121220)) // Deep premium dark background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
        ) {
            // ── Toolbar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OmniSpacing.lg, vertical = OmniSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onShowHelp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                            contentDescription = "Help",
                            tint = Color.White
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Loc.get("title", isArabic),
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        state.isBypassed -> Color(0xFFFFB020)
                                        state.isEnabled && state.isEngineFullyReal -> AccentTeal
                                        state.isEnabled -> Color(0xFFEF4444)
                                        else -> MutedText
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                state.isBypassed -> Loc.get("bypassed_badge", isArabic)
                                state.isEnabled && state.isEngineFullyReal -> Loc.get("engine_active_badge", isArabic)
                                state.isEnabled -> Loc.get("engine_unavailable_badge", isArabic)
                                else -> Loc.get("custom", isArabic)
                            },
                            color = MutedText,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // ── Scrollable content: three clearly separated sections ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = OmniSpacing.lg)
            ) {
                // ── SECTION 1: Equalizer Bands ──
                SectionLabel(Loc.get("section_eq_bands", isArabic))
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Loc.get("eq_toggle", isArabic),
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
                                checkedTrackColor = AccentIndigo,
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFF2E2F45)
                            ),
                            modifier = Modifier.testTag("eq_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(OmniSpacing.lg))

                    val frequencies = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
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
                                val dbValue = state.gains[index]
                                VerticalEqualizerBand(
                                    freq = freq,
                                    dbValue = dbValue,
                                    isEnabled = state.isEnabled,
                                    onValueChange = { newValue ->
                                        viewModel.updateBand(index, newValue)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
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
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(OmniRadius.small))
                                .background(if (state.isSmartOptimized) AccentIndigo else Color(0xFF20222E))
                                .clickable(enabled = state.isEnabled) { viewModel.toggleSmartOptimization() }
                                .padding(horizontal = OmniSpacing.md, vertical = OmniSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "Smart AI",
                                tint = if (state.isSmartOptimized) Color.White else AccentTeal,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(OmniSpacing.xs))
                            Text(
                                text = Loc.get("smart_btn", isArabic),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(OmniRadius.small))
                                .border(1.dp, CardBorder, RoundedCornerShape(OmniRadius.small))
                                .clickable(enabled = state.isEnabled) { onShowPresets() }
                                .padding(horizontal = OmniSpacing.md, vertical = OmniSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.isSmartOptimized) Loc.get("smart_ai", isArabic) else state.selectedPresetName,
                                color = AccentTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 90.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "Presets",
                                tint = MutedText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(OmniSpacing.xl))

                // ── SECTION 2: Sound Effects (quick toggles + the three FX dials) ──
                SectionLabel(Loc.get("section_effects", isArabic))
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(OmniSpacing.sm)
                    ) {
                        CompactToggleChip(
                            label = Loc.get("bypass_label", isArabic),
                            icon = Icons.Rounded.CompareArrows,
                            isOn = state.isBypassed,
                            isEnabled = state.isEnabled,
                            activeColor = Color(0xFFFFB020),
                            onClick = { viewModel.toggleBypass() },
                            modifier = Modifier.weight(1f)
                        )
                        CompactToggleChip(
                            label = Loc.get("auto_loudness_label", isArabic),
                            icon = Icons.Rounded.GraphicEq,
                            isOn = state.autoLoudnessNormalization,
                            isEnabled = state.isEnabled,
                            activeColor = AccentPink,
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
                            label = Loc.get("bass_boost", isArabic),
                            value = state.bassBoost,
                            isEnabled = state.isEnabled && state.bassBoostEnabled,
                            onValueChange = { viewModel.updateBassBoost(it) },
                            switchChecked = state.bassBoostEnabled,
                            onSwitchChange = { viewModel.toggleBassBoost() },
                            color = AccentPink,
                            modifier = Modifier.weight(1f)
                        )

                        CircularControlDial(
                            label = Loc.get("loudness", isArabic),
                            value = state.loudness,
                            isEnabled = state.isEnabled && state.loudnessEnabled,
                            onValueChange = { viewModel.updateLoudness(it) },
                            switchChecked = state.loudnessEnabled,
                            onSwitchChange = { viewModel.toggleLoudness() },
                            color = AccentIndigo,
                            modifier = Modifier.weight(1f)
                        )

                        CircularControlDial(
                            label = Loc.get("virtualizer", isArabic),
                            value = state.virtualizer,
                            isEnabled = state.isEnabled && state.virtualizerEnabled,
                            onValueChange = { viewModel.updateVirtualizer(it) },
                            switchChecked = state.virtualizerEnabled,
                            onSwitchChange = { viewModel.toggleVirtualizer() },
                            color = AccentTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(OmniSpacing.xl))

                // ── SECTION 3: System Volume ──
                if (state.showVolumeSlider) {
                    SectionLabel(Loc.get("section_volume", isArabic))
                    SectionCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeUp,
                                    contentDescription = "Volume Icon",
                                    tint = AccentIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(OmniSpacing.sm))
                                Text(
                                    text = Loc.get("volume", isArabic),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${state.volume.toInt()}%",
                                color = MutedText,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Slider(
                            value = state.volume,
                            onValueChange = { viewModel.updateVolume(it) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentIndigo,
                                activeTrackColor = AccentIndigo,
                                inactiveTrackColor = Color(0xFF2E2F45)
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
                        .padding(horizontal = OmniSpacing.lg, vertical = OmniSpacing.xs)
                        .clip(RoundedCornerShape(OmniRadius.small))
                        .background(Color(0xFFFFB020).copy(alpha = 0.14f))
                        .border(1.dp, Color(0xFFFFB020).copy(alpha = 0.35f), RoundedCornerShape(OmniRadius.small))
                        .padding(horizontal = OmniSpacing.md, vertical = OmniSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = Color(0xFFFFB020),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(OmniSpacing.sm))
                    Text(
                        text = state.engineWarning.orEmpty(),
                        color = Color(0xFFFFB020),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Bottom Status Bar: global-mix indicator + live spectrum ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF171826))
                    .border(0.5.dp, CardBorder)
                    .padding(OmniSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(OmniRadius.small))
                        .background(Color(0xFF20222E))
                        .padding(horizontal = OmniSpacing.md, vertical = OmniSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = "Music",
                        tint = AccentIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Loc.get("global_mix", isArabic),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LiveSpectrumVisualizer(
                    isActive = state.isEnabled,
                    levels = viewModel.spectrumLevels.collectAsState().value,
                    modifier = Modifier
                        .width(100.dp)
                        .height(28.dp)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(OmniRadius.small))
                        .background(AccentTeal.copy(alpha = 0.15f))
                        .border(1.dp, AccentTeal.copy(alpha = 0.3f), RoundedCornerShape(OmniRadius.small))
                        .padding(horizontal = OmniSpacing.md, vertical = OmniSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Equalizer,
                        contentDescription = "General",
                        tint = AccentTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Loc.get("general_mode", isArabic),
                        color = AccentTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
