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
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
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
import androidx.compose.ui.unit.IntOffset
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

// ── Main UI Component Container ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniEqualizerApp(viewModel: EqualizerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val presets by viewModel.presets.collectAsState()
    
    val isArabic = state.currentLanguage == "ar"
    val isDark = state.currentTheme == "Cosmic" || state.currentTheme == "Dark"
    
    var activeScreen by rememberSaveable { mutableStateOf("main") } // main, settings
    var showHelpSheet by remember { mutableStateOf(false) }
    var showPresetSheet by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var selectedPresetForOption by remember { mutableStateOf<EqualizerPreset?>(null) }

    AnimatedContent(
        targetState = state.isSplashActive,
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
        },
        label = "splashTransition"
    ) { splash ->
        if (splash) {
            EqualizerSplashScreen()
        } else {
            OmniDevTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = activeScreen,
                        transitionSpec = {
                            val spec = spring<androidx.compose.ui.unit.IntOffset>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                            val fadeSpec = tween<Float>(220)
                            if (targetState == "settings") {
                                slideInHorizontally(animationSpec = spec) { width -> if (isArabic) -width else width } + fadeIn(fadeSpec) togetherWith
                                        slideOutHorizontally(animationSpec = spec) { width -> if (isArabic) width else -width } + fadeOut(fadeSpec)
                            } else {
                                slideInHorizontally(animationSpec = spec) { width -> if (isArabic) width else -width } + fadeIn(fadeSpec) togetherWith
                                        slideOutHorizontally(animationSpec = spec) { width -> if (isArabic) -width else width } + fadeOut(fadeSpec)
                            }
                        },
                        label = "screenNav"
                    ) { screen ->
                        when (screen) {
                            "main" -> {
                                EqualizerMainScreen(
                                    state = state,
                                    presets = presets,
                                    viewModel = viewModel,
                                    isArabic = isArabic,
                                    onNavigateToSettings = { activeScreen = "settings" },
                                    onShowHelp = { showHelpSheet = true },
                                    onShowPresets = { showPresetSheet = true },
                                    onShowSavePreset = { showSavePresetDialog = true }
                                )
                            }
                            "settings" -> {
                                EqualizerSettingsScreen(
                                    state = state,
                                    viewModel = viewModel,
                                    isArabic = isArabic,
                                    onNavigateBack = { activeScreen = "main" }
                                )
                            }
                        }
                    }

                    // ── Help Bottom Sheet ──
                    if (showHelpSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showHelpSheet = false },
                            containerColor = OmniCardBg,
                            contentColor = Color.White,
                            dragHandle = null,
                            shape = RoundedCornerShape(topStart = OmniRadius.xl, topEnd = OmniRadius.xl)
                        ) {
                            HelpSheetContent(isArabic = isArabic, onDismiss = { showHelpSheet = false })
                        }
                    }

                    // ── Presets Bottom Sheet ──
                    if (showPresetSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showPresetSheet = false },
                            containerColor = OmniCardBg,
                            contentColor = Color.White,
                            dragHandle = null,
                            shape = RoundedCornerShape(topStart = OmniRadius.xl, topEnd = OmniRadius.xl)
                        ) {
                            PresetsSheetContent(
                                presets = presets,
                                activePresetId = state.selectedPresetId,
                                isArabic = isArabic,
                                onPresetSelect = { preset ->
                                    viewModel.applyPreset(preset)
                                    showPresetSheet = false
                                },
                                onAddCustomPreset = {
                                    showPresetSheet = false
                                    showSavePresetDialog = true
                                },
                                onOptionsClick = { preset ->
                                    selectedPresetForOption = preset
                                    showPresetSheet = false
                                    showOptionsSheet = true
                                }
                            )
                        }
                    }

                    // ── Preset Actions Options Bottom Sheet ──
                    if (showOptionsSheet && selectedPresetForOption != null) {
                        val preset = selectedPresetForOption!!
                        ModalBottomSheet(
                            onDismissRequest = { showOptionsSheet = false },
                            containerColor = OmniCardBg,
                            contentColor = Color.White,
                            dragHandle = null,
                            shape = RoundedCornerShape(topStart = OmniRadius.xl, topEnd = OmniRadius.xl)
                        ) {
                            PresetOptionsSheetContent(
                                preset = preset,
                                isArabic = isArabic,
                                onDismiss = { showOptionsSheet = false },
                                onEdit = {
                                    viewModel.applyPreset(preset)
                                    showOptionsSheet = false
                                },
                                onDelete = {
                                    viewModel.deletePreset(preset.id)
                                    showOptionsSheet = false
                                }
                            )
                        }
                    }

                    // ── Save Preset Dialog ──
                    if (showSavePresetDialog) {
                        var presetName by remember { mutableStateOf("") }
                        AlertDialog(
                            onDismissRequest = { showSavePresetDialog = false },
                            containerColor = OmniCardBgElevated,
                            shape = RoundedCornerShape(OmniRadius.xl),
                            title = {
                                Text(
                                    text = Loc.get("save_preset_title", isArabic),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = if (isArabic) TextAlign.End else TextAlign.Start
                                )
                            },
                            text = {
                                OutlinedTextField(
                                    value = presetName,
                                    onValueChange = { presetName = it },
                                    placeholder = { Text(Loc.get("save_preset_placeholder", isArabic), color = OmniFaintText) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(OmniRadius.medium),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = OmniAccentIndigo,
                                        unfocusedBorderColor = OmniCardBorder,
                                        focusedContainerColor = OmniBackground,
                                        unfocusedContainerColor = OmniBackground
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("preset_name_input")
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (presetName.isNotBlank()) {
                                            viewModel.saveCustomPreset(presetName.trim())
                                            showSavePresetDialog = false
                                        }
                                    },
                                    shape = RoundedCornerShape(OmniRadius.medium),
                                    colors = ButtonDefaults.buttonColors(containerColor = OmniAccentIndigo)
                                ) {
                                    Text(Loc.get("save_btn", isArabic), color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showSavePresetDialog = false }) {
                                    Text(Loc.get("cancel_btn", isArabic), color = OmniMutedText)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

