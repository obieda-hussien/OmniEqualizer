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

// ── Bilingual Localization Dictionary ──
object Loc {
    @Composable
    fun get(key: String, isArabic: Boolean = false): String {
        val context = LocalContext.current
        val resources = if (isArabic) {
            val locale = java.util.Locale("ar")
            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.createConfigurationContext(config).resources
        } else {
            val locale = java.util.Locale("en")
            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.createConfigurationContext(config).resources
        }
        val id = context.resources.getIdentifier(key, "string", context.packageName)
        return if (id != 0) resources.getString(id) else key
    }

    fun legacy_get(key: String, isArabic: Boolean): String {
        return if (isArabic) {
            when (key) {
                "title" -> "OmniEqualizer"
                "eq_toggle" -> "المعادل"
                "custom" -> "مخصص"
                "smart_ai" -> "الموازن الذكي"
                "presets" -> "الإعدادات المسبقة"
                "bass_boost" -> "تعزيز جهارة الصوت"
                "loudness" -> "ارتفاع الصوت"
                "virtualizer" -> "افتراضي"
                "volume" -> "مستوى الصوت"
                "global_mix" -> "Global Mix"
                "general_mode" -> "عام"
                "settings" -> "الإعدادات"
                "help" -> "مساعدة"
                "help_title" -> "المساعدة"
                "load_preset" -> "تحميل الإعداد المسبق"
                "edit_preset" -> "تعديل الإعداد المسبق"
                "share_profile" -> "مشاركة الملف الشخصي"
                "delete_profile" -> "حذف الملف الشخصي"
                "import_profile" -> "تحميل ملف تعريف آخر"
                "appearance" -> "المظهر"
                "appearance_desc" -> "تغيير شكل ومظهر التطبيق"
                "language" -> "لغة التطبيق"
                "language_desc" -> "العربية الفصحى / Arabic"
                "backup" -> "استرجاع البيانات"
                "backup_desc" -> "النسخ الاحتياطي أو استعادة الملفات الشخصية المحفوظة"
                "bass_freq" -> "تردد تعزيز الجهير"
                "bass_freq_desc" -> "80Hz (Default)"
                "bass_gain" -> "Bass Boost Max Gain"
                "bass_gain_desc" -> "15dB (Default)"
                "loudness_gain" -> "Loudness Max Gain"
                "loudness_gain_desc" -> "20dB (Default)"
                "audio_balance" -> "توازن الصوت"
                "audio_balance_desc" -> "ميزة توازن القناة اليسرى واليمنى معطلة"
                "always_bind_global" -> "ربط دائمًا بـ Global Mix"
                "always_bind_global_desc" -> "قم بتمكين هذا فقط إذا كنت تواجه مشكلات في إرفاق التأثيرات"
                "connect_music_only" -> "قم بالتوصيل فقط بمشغلات الموسيقى"
                "connect_music_only_desc" -> "بشكل افتراضي، سيتم إرفاق المعادل بالمزيج العام..."
                "frame_duration" -> "مدة الإطار (بالملي ثانية)"
                "frame_duration_desc" -> "القيمة الحالية هي 10ms. ستؤدي زيادة جودة الصوت..."
                "reverb" -> "تأثيرات الارتداد"
                "reverb_desc" -> "تم تعطيل تأثيرات الارتداد"
                "volume_bar" -> "شريط تمرير مستوى الصوت"
                "volume_bar_desc" -> "يظهر شريط تمرير مستوى الصوت"
                "use_10_bands" -> "استخدم معادل الصوت 10 نطاقات"
                "use_10_bands_desc" -> "باستخدام معادل الصوت 10 نطاقات"
                "use_legacy" -> "استخدم التأثيرات القديمة"
                "use_legacy_desc" -> "لا يُنصح. قم بتمكين هذا فقط إذا كانت التأثيرات لا تعمل..."
                "notifications" -> "إخفاء/إظهار الإشعارات"
                "notifications_desc" -> "افتح إعدادات إشعارات التطبيق"
                "saved_bt" -> "أجهزة البلوتوث المحفوظة"
                "saved_bt_desc" -> "عرض أجهزة Bluetooth المحفوظة"
                "save_preset_title" -> "حفظ كإعداد مسبق"
                "save_preset_placeholder" -> "اسم الإعداد المسبق..."
                "save_btn" -> "حفظ"
                "cancel_btn" -> "إلغاء"
                "delete_confirm" -> "هل أنت متأكد من حذف الإعداد المسبق؟"
                "add_preset_btn" -> "إضافة إعداد مسبق جديد"
                "smart_btn" -> "ذكي وتلقائي"
                "help_body" -> "قم بتمكين المعادل أو أي تأثير قبل فتح مشغل الوسائط.\n\nيرتبط المعادل نفسه بمشغل الوسائط قيد التشغيل حاليًا. تسمح معظم مشغلات الوسائط للتطبيقات الأخرى بتطبيق التأثيرات على أغانيها، وبعضها يتطلب تكوينًا إضافيًا، لكن البعض لا يسمح للتطبيقات الأخرى بتطبيق التأثيرات على أغانيها.\nبناءً على ذلك، قد لا يربط المعادل نفسه ببعض مشغلات الوسائط وسيرتبط نفسه بـ Global Mix لدعم هؤلاء المشغلين، على سبيل سبيل المثال، يوتيوب (قد لا يعمل هذا على جميع الأجهزة).\n\nإذا كنت تستخدم Youtube Music أو Spotify، فتأكد من إعادة تشغيلهما مع الحفاظ على المعادل أو أي تأثير ممكنًا.\n\nسيعرض المعادل أيضًا الإشعار عندما يكتشف أي مشغل وسائط، انقر فوق الإشعار لربط المعادل بمشغل الوسائط هذا.\n\nأيضًا، قم بإيقاف تشغيل جميع تحسينات البطارية لهذا التطبيق للحصول على تجربة أفضل.\n\nالآن، قم بتشغيل بعض الموسيقى واستمتع بالمعادل."
                else -> key
            }
        } else {
            when (key) {
                "title" -> "OmniEqualizer"
                "eq_toggle" -> "Equalizer"
                "custom" -> "Custom"
                "smart_ai" -> "Smart AI"
                "presets" -> "Presets"
                "bass_boost" -> "Bass Boost"
                "loudness" -> "Loudness"
                "virtualizer" -> "Virtualizer"
                "volume" -> "Volume Level"
                "global_mix" -> "Global Mix"
                "general_mode" -> "General"
                "settings" -> "Settings"
                "help" -> "Help"
                "help_title" -> "Help & Instructions"
                "load_preset" -> "Load Preset"
                "edit_preset" -> "Edit Preset"
                "share_profile" -> "Share Profile"
                "delete_profile" -> "Delete Profile"
                "import_profile" -> "Load Another Profile"
                "appearance" -> "Appearance"
                "appearance_desc" -> "Change application theme and visual appearance"
                "language" -> "App Language"
                "language_desc" -> "English / العربية الفصحى"
                "backup" -> "Data Restore/Backup"
                "backup_desc" -> "Backup or restore saved audio profiles"
                "bass_freq" -> "Bass Boost Frequency"
                "bass_freq_desc" -> "80Hz (Default)"
                "bass_gain" -> "Bass Boost Max Gain"
                "bass_gain_desc" -> "15dB (Default)"
                "loudness_gain" -> "Loudness Max Gain"
                "loudness_gain_desc" -> "20dB (Default)"
                "audio_balance" -> "Audio Balance"
                "audio_balance_desc" -> "Left and right channel balance feature disabled"
                "always_bind_global" -> "Always bind to Global Mix"
                "always_bind_global_desc" -> "Enable this only if you experience issues attaching effects"
                "connect_music_only" -> "Connect only to music players"
                "connect_music_only_desc" -> "By default, equalizer attaches to global mix..."
                "frame_duration" -> "Frame Duration (ms)"
                "frame_duration_desc" -> "Current value is 10ms. Increasing will improve quality..."
                "reverb" -> "Reverb Effects"
                "reverb_desc" -> "Reverb effects disabled"
                "volume_bar" -> "Volume Slider Bar"
                "volume_bar_desc" -> "Shows standard volume slider overlay"
                "use_10_bands" -> "Use 10-Band Equalizer"
                "use_10_bands_desc" -> "Customize sound with ultra fine 10-band controls"
                "use_legacy" -> "Use Legacy Effects"
                "use_legacy_desc" -> "Not recommended. Enable only if standard effects fail to load..."
                "notifications" -> "Hide/Show Notifications"
                "notifications_desc" -> "Open app system notification settings"
                "saved_bt" -> "Saved Bluetooth Devices"
                "saved_bt_desc" -> "View saved or historical Bluetooth audio devices"
                "save_preset_title" -> "Save Custom Preset"
                "save_preset_placeholder" -> "Preset Name..."
                "save_btn" -> "Save"
                "cancel_btn" -> "Cancel"
                "delete_confirm" -> "Are you sure you want to delete this preset?"
                "add_preset_btn" -> "Add Custom Preset"
                "smart_btn" -> "Smart Auto"
                "help_body" -> "Enable Equalizer or any effect before opening media player.\n\nThe equalizer binds itself to the media player currently playing. Most media players allow other apps to apply effects, some require additional configuration, but some do not allow other apps to apply effects.\nConsequently, the equalizer may not bind to some media players and will bind to Global Mix to support them, e.g. YouTube (may not work on all devices).\n\nIf you use Youtube Music or Spotify, make sure to restart them while keeping the Equalizer or effect enabled.\n\nThe equalizer also shows a notification when it detects any media player; click it to bind the equalizer to that player.\n\nAlso, turn off battery optimization for this app to get the best experience.\n\nNow, play some music and enjoy!"
                else -> key
            }
        }
    }
}

// ── Splash Screen Component ──
@Composable
fun EqualizerSplashScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .background(Color(0xFF121220)) // Dark brand background
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Equalizer sliders visual inside a rounded card
            Card(
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1B2E)),
                modifier = Modifier
                    .size(180.dp)
                    .scale(scalePulse)
                    .border(1.dp, Color(0xFF6C63FF).copy(alpha = 0.3f), RoundedCornerShape(40.dp))
                    .shadow(16.dp, RoundedCornerShape(40.dp), clip = true, ambientColor = Color(0xFF6C63FF))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AdvancedDspBackgroundVisualizer(isActive = true, modifier = Modifier.fillMaxSize().alpha(0.7f))
                    Row(
                        modifier = Modifier.padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SliderBarMock(heightFraction = 0.8f, activeFraction = 0.6f)
                        SliderBarMock(heightFraction = 0.9f, activeFraction = 0.4f)
                        SliderBarMock(heightFraction = 0.7f, activeFraction = 0.7f)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Loading circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .rotate(rotation)
                    .border(4.dp, Color(0xFF6C63FF).copy(alpha = 0.1f), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .drawBehind {
                            drawArc(
                                color = Color(0xFF6C63FF),
                                startAngle = 0f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Brand Name
            Text(
                text = "Equalizer",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "OMNIEQUALIZER PRO",
                color = Color(0xFF6C63FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun SliderBarMock(heightFraction: Float, activeFraction: Float) {
    Column(
        modifier = Modifier
            .fillMaxHeight(heightFraction)
            .width(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFF2E2F45)),
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(activeFraction)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFF6B9D), Color(0xFF6C63FF))
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            // Knob
            Box(
                modifier = Modifier
                    .offset(y = (-5).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color(0xFFFF6B9D), CircleShape)
            )
        }
    }
}

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
                            if (targetState == "settings") {
                                slideInHorizontally { width -> if (isArabic) -width else width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> if (isArabic) width else -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> if (isArabic) width else -width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> if (isArabic) -width else width } + fadeOut()
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
                            containerColor = Color(0xFF1A1B2E),
                            contentColor = Color.White,
                            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF2E2F45)) }
                        ) {
                            HelpSheetContent(isArabic = isArabic, onDismiss = { showHelpSheet = false })
                        }
                    }

                    // ── Presets Bottom Sheet ──
                    if (showPresetSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showPresetSheet = false },
                            containerColor = Color(0xFF1A1B2E),
                            contentColor = Color.White,
                            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF2E2F45)) }
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
                            containerColor = Color(0xFF1A1B2E),
                            contentColor = Color.White,
                            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF2E2F45)) }
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
                            containerColor = Color(0xFF1A1B2E),
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
                                    placeholder = { Text(Loc.get("save_preset_placeholder", isArabic), color = Color(0xFF64748B)) },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6C63FF),
                                        unfocusedBorderColor = Color(0xFF2E2F45),
                                        focusedContainerColor = Color(0xFF121220),
                                        unfocusedContainerColor = Color(0xFF121220)
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
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                                ) {
                                    Text(Loc.get("save_btn", isArabic), color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showSavePresetDialog = false }) {
                                    Text(Loc.get("cancel_btn", isArabic), color = Color(0xFFFF6B9D))
                                }
                            }
                        )
                    }
                }
            }
        }
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Loc.get("title", isArabic),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (state.isEnabled) Color(0xFF00D9A6) else Color(0xFFEF4444))
                    )
                }
            }

            // ── Interactive Equalizer Canvas & Sliders ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1B2E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E2F45), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
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
                                onCheckedChange = { viewModel.toggleEqualizer() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF6C63FF),
                                    uncheckedThumbColor = Color(0xFF94A3B8),
                                    uncheckedTrackColor = Color(0xFF2E2F45)
                                ),
                                modifier = Modifier.testTag("eq_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val frequencies = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
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

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (state.isSmartOptimized) Color(0xFF6C63FF) else Color(0xFF2E2F45))
                                    .clickable(enabled = state.isEnabled) { viewModel.toggleSmartOptimization() }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = "Smart AI",
                                    tint = if (state.isSmartOptimized) Color.White else Color(0xFF00D9A6),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Loc.get("smart_btn", isArabic),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF2E2F45), RoundedCornerShape(12.dp))
                                    .clickable(enabled = state.isEnabled) { onShowPresets() }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (state.isSmartOptimized) Loc.get("smart_ai", isArabic) else state.selectedPresetName,
                                    color = Color(0xFF00D9A6),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Loc.get("presets", isArabic),
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDropDown,
                                    contentDescription = "Presets Dropdown",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Circular FX Dials (Bass Boost, Loudness, Virtualizer) ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularControlDial(
                        label = Loc.get("bass_boost", isArabic),
                        value = state.bassBoost,
                        isEnabled = state.isEnabled && state.bassBoostEnabled,
                        onValueChange = { viewModel.updateBassBoost(it) },
                        switchChecked = state.bassBoostEnabled,
                        onSwitchChange = { viewModel.toggleBassBoost() },
                        color = Color(0xFFFF6B9D),
                        modifier = Modifier.weight(1f)
                    )

                    CircularControlDial(
                        label = Loc.get("loudness", isArabic),
                        value = state.loudness,
                        isEnabled = state.isEnabled && state.loudnessEnabled,
                        onValueChange = { viewModel.updateLoudness(it) },
                        switchChecked = state.loudnessEnabled,
                        onSwitchChange = { viewModel.toggleLoudness() },
                        color = Color(0xFF6C63FF),
                        modifier = Modifier.weight(1f)
                    )

                    CircularControlDial(
                        label = Loc.get("virtualizer", isArabic),
                        value = state.virtualizer,
                        isEnabled = state.isEnabled && state.virtualizerEnabled,
                        onValueChange = { viewModel.updateVirtualizer(it) },
                        switchChecked = state.virtualizerEnabled,
                        onSwitchChange = { viewModel.toggleVirtualizer() },
                        color = Color(0xFF00D9A6),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Volume Level Control Card ──
                if (state.showVolumeSlider) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1B2E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF2E2F45), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.VolumeUp,
                                        contentDescription = "Volume Icon",
                                        tint = Color(0xFF6C63FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = Loc.get("volume", isArabic),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${state.volume.toInt()}%",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            
                            Slider(
                                value = state.volume,
                                onValueChange = { viewModel.updateVolume(it) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF6C63FF),
                                    activeTrackColor = Color(0xFF6C63FF),
                                    inactiveTrackColor = Color(0xFF2E2F45)
                                ),
                                modifier = Modifier.testTag("volume_slider")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // ── Bottom Navigation / Modes Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1B2E))
                    .border(0.5.dp, Color(0xFF2E2F45))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E2F45))
                        .clickable {}
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = "Music",
                        tint = Color(0xFF6C63FF),
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
                    modifier = Modifier
                        .width(100.dp)
                        .height(28.dp)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00D9A6).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF00D9A6).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Equalizer,
                        contentDescription = "General",
                        tint = Color(0xFF00D9A6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Loc.get("general_mode", isArabic),
                        color = Color(0xFF00D9A6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── CUSTOM SLIDER BAND COMPONENT ──
@Composable
fun VerticalEqualizerBand(
    freq: String,
    dbValue: Float,
    isEnabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = (dbValue + 15f) / 30f

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = String.format("%+.1f", dbValue),
            color = if (isEnabled) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .width(16.dp)
                .pointerInput(isEnabled) {
                    if (!isEnabled) return@pointerInput
                    detectDragGestures { change, _ ->
                        change.consume()
                        val height = this.size.height
                        val touchY = change.position.y
                        val newFraction = (1f - (touchY / height)).coerceIn(0f, 1f)
                        val newValue = (newFraction * 30f) - 15f
                        onValueChange(newValue)
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(Color(0xFF2E2F45))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(progressFraction)
                    .drawBehind {
                        drawLine(
                            color = Color(0xFFFF6B9D).copy(alpha = 0.4f),
                            start = Offset(this.size.width / 2, 0f),
                            end = Offset(this.size.width / 2, this.size.height),
                            strokeWidth = 4.dp.toPx()
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-progressFraction * 200).dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isEnabled) Color(0xFFFF6B9D) else Color(0xFF475569))
                    .border(2.dp, Color.White, CircleShape)
            )
        }

        Text(
            text = freq,
            color = if (isEnabled) Color(0xFF94A3B8) else Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
    }
}

// ── ADVANCED DSP BACKGROUND SIMULATOR ──
@Composable
fun AdvancedDspBackgroundVisualizer(isActive: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dsp")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        if (!isActive) return@Canvas
        val w = size.width
        val h = size.height
        val centerY = h / 2f
        val points = 80
        val path = androidx.compose.ui.graphics.Path()
        path.moveTo(0f, h)
        for (i in 0..points) {
            val x = i * (w / points)
            val nx = i.toFloat() / points
            val wave1 = sin(nx * 12f + time * 3f) * 0.4f
            val wave2 = sin(nx * 20f - time * 5f) * 0.2f
            val wave3 = cos(nx * 35f + time * 7f) * 0.1f
            val window = sin(nx * PI).toFloat()
            val amplitude = (wave1 + wave2 + wave3) * window * h * 0.5f
            val y = centerY + amplitude
            if (i == 0) path.lineTo(x, y) else path.lineTo(x, y)
        }
        path.lineTo(w, h)
        path.close()
        
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF6C63FF).copy(alpha = 0.4f),
                    Color(0xFFFF6B9D).copy(alpha = 0.1f),
                    Color.Transparent
                )
            )
        )
    }
}

// ── CUSTOM BEZIER EQUALIZER SPLINE CURVE ──
@Composable
fun EqualizerSplineCurve(
    gains: List<Float>,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedGains = gains.map {
        animateFloatAsState(
            targetValue = it,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "gainAnimation"
        )
    }

    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height
        val pointsCount = gains.size
        
        val path = Path()
        val spacing = width / (pointsCount - 1)

        val points = mutableListOf<Offset>()
        for (i in 0 until pointsCount) {
            val db = animatedGains[i].value
            val fraction = (db + 15f) / 30f
            val y = height - (fraction * height)
            val x = i * spacing
            points.add(Offset(x, y))
        }

        path.moveTo(points[0].x, points[0].y)
        for (i in 0 until pointsCount - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val controlPointX1 = p0.x + (p1.x - p0.x) / 2
            val controlPointY1 = p0.y
            val controlPointX2 = p0.x + (p1.x - p0.x) / 2
            val controlPointY2 = p1.y
            
            path.cubicTo(
                controlPointX1, controlPointY1,
                controlPointX2, controlPointY2,
                p1.x, p1.y
            )
        }

        val strokeColor = if (isEnabled) Color(0xFFFF6B9D) else Color(0xFF475569)
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        if (isEnabled) {
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF6B9D).copy(alpha = 0.12f),
                        Color.Transparent
                    )
                )
            )
        }
    }
}

// ── CUSTOM SEMI-CIRCULAR CONTROL DIAL ──
@Composable
fun CircularControlDial(
    label: String,
    value: Float,
    isEnabled: Boolean,
    onValueChange: (Float) -> Unit,
    switchChecked: Boolean,
    onSwitchChange: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    val sweepAngleLimit = 240f
    val startAngleLimit = 150f

    val progressSweep = (value / 100f) * sweepAngleLimit

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1B2E)),
        modifier = modifier
            .border(1.dp, Color(0xFF2E2F45), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(75.dp)
                    .pointerInput(isEnabled) {
                        if (!isEnabled) return@pointerInput
                        detectDragGestures { change, _ ->
                            change.consume()
                            val center = this.size.width / 2f
                            val touchX = change.position.x - center
                            val touchY = change.position.y - center
                            var angleRad = atan2(touchY, touchX)
                            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                            angleDeg = (angleDeg + 360f) % 360f
                            
                            var progress = 0f
                            if (angleDeg >= 150f && angleDeg <= 390f) {
                                progress = (angleDeg - 150f) / sweepAngleLimit
                            } else if (angleDeg < 150f && angleDeg <= 30f) {
                                progress = (angleDeg + 360f - 150f) / sweepAngleLimit
                            } else if (angleDeg < 90f) {
                                progress = 1.0f
                            } else {
                                progress = 0.0f
                            }
                            onValueChange(progress.coerceIn(0f, 1f) * 100f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = this.size.minDimension / 2f - 6.dp.toPx()
                    val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)

                    drawArc(
                        color = Color(0xFF2E2F45),
                        startAngle = startAngleLimit,
                        sweepAngle = sweepAngleLimit,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    if (isEnabled) {
                        drawArc(
                            color = color,
                            startAngle = startAngleLimit,
                            sweepAngle = progressSweep,
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    drawCircle(
                        color = Color(0xFF121220),
                        radius = radius - 4.dp.toPx(),
                        center = centerOffset
                    )

                    val finalAngle = startAngleLimit + progressSweep
                    val rad = Math.toRadians(finalAngle.toDouble())
                    val indicatorX = centerOffset.x + (radius - 5.dp.toPx()) * cos(rad).toFloat()
                    val indicatorY = centerOffset.y + (radius - 5.dp.toPx()) * sin(rad).toFloat()

                    drawCircle(
                        color = if (isEnabled) color else Color(0xFF475569),
                        radius = 3.5.dp.toPx(),
                        center = Offset(indicatorX, indicatorY)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${value.toInt()}%",
                        color = if (isEnabled) Color.White else Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Switch(
                checked = switchChecked,
                onCheckedChange = { onSwitchChange() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = color,
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFF2E2F45)
                ),
                modifier = Modifier
                    .scale(0.85f)
                    .testTag("switch_${label.lowercase().replace(" ", "_")}")
            )
        }
    }
}

// ── BOUNCING WAVEFORM SOUND SPECTURM VISUALIZER ──
@Composable
fun LiveSpectrumVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 10
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    
    val heightScaleList = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (280..650).random(),
                    delayMillis = index * 30,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "visualizer_bar_$index"
        )
    }

    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height
        val spacing = 4.dp.toPx()
        val barWidth = (width - (spacing * (barCount - 1))) / barCount

        for (i in 0 until barCount) {
            val scale = if (isActive) heightScaleList[i].value else 0.15f
            val barHeight = scale * height
            val x = i * (barWidth + spacing)
            val y = height - barHeight

            drawRoundRect(
                color = if (isActive) Color(0xFF6C63FF) else Color(0xFF475569),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

// ── BOTTOM SHEET CONTENT: HELP ──
@Composable
fun HelpSheetContent(
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = if (isArabic) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
            }
            Text(
                text = Loc.get("help_title", isArabic),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = Loc.get("help_body", isArabic),
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = if (isArabic) TextAlign.End else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── BOTTOM SHEET CONTENT: PRESETS LIST ──
@Composable
fun PresetsSheetContent(
    presets: List<EqualizerPreset>,
    activePresetId: Int,
    isArabic: Boolean,
    onPresetSelect: (EqualizerPreset) -> Unit,
    onAddCustomPreset: () -> Unit,
    onOptionsClick: (EqualizerPreset) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onAddCustomPreset,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Icon", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(Loc.get("add_preset_btn", isArabic), fontSize = 12.sp, color = Color.White)
            }

            Text(
                text = Loc.get("load_preset", isArabic),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { preset ->
                val isSelected = preset.id == activePresetId
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Color(0xFF2E2F45) else Color(0xFF1A1B2E))
                        .clickable { onPresetSelect(preset) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onOptionsClick(preset) }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = Color(0xFF94A3B8))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        preset.tags.split(",").forEach { tag ->
                            if (tag.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF121220))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tag.trim(),
                                        color = Color(0xFF00D9A6),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = preset.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                        
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Checked", tint = Color(0xFFFF6B9D), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── BOTTOM SHEET CONTENT: PRESET OPTIONS ──
@Composable
fun PresetOptionsSheetContent(
    preset: EqualizerPreset,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = if (isArabic) Alignment.End else Alignment.Start
    ) {
        Text(
            text = preset.name,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = if (isArabic) TextAlign.End else TextAlign.Start
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onEdit()
                    onDismiss()
                }
                .padding(vertical = 12.dp),
            horizontalArrangement = if (isArabic) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isArabic) {
                Text(Loc.get("edit_preset", isArabic), color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit", tint = Color(0xFF6C63FF))
            } else {
                Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit", tint = Color(0xFF6C63FF))
                Spacer(modifier = Modifier.width(12.dp))
                Text(Loc.get("edit_preset", isArabic), color = Color.White, fontSize = 14.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDismiss() }
                .padding(vertical = 12.dp),
            horizontalArrangement = if (isArabic) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isArabic) {
                Text(Loc.get("share_profile", isArabic), color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(imageVector = Icons.Rounded.Share, contentDescription = "Share", tint = Color(0xFF00D9A6))
            } else {
                Icon(imageVector = Icons.Rounded.Share, contentDescription = "Share", tint = Color(0xFF00D9A6))
                Spacer(modifier = Modifier.width(12.dp))
                Text(Loc.get("share_profile", isArabic), color = Color.White, fontSize = 14.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDismiss() }
                .padding(vertical = 12.dp),
            horizontalArrangement = if (isArabic) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isArabic) {
                Text(Loc.get("import_profile", isArabic), color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(imageVector = Icons.Rounded.FileDownload, contentDescription = "Import", tint = Color(0xFF00D9A6))
            } else {
                Icon(imageVector = Icons.Rounded.FileDownload, contentDescription = "Import", tint = Color(0xFF00D9A6))
                Spacer(modifier = Modifier.width(12.dp))
                Text(Loc.get("import_profile", isArabic), color = Color.White, fontSize = 14.sp)
            }
        }

        if (preset.isCustom) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDelete()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = if (isArabic) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isArabic) {
                    Text(Loc.get("delete_profile", isArabic), color = Color(0xFFEF4444), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                } else {
                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Loc.get("delete_profile", isArabic), color = Color(0xFFEF4444), fontSize = 14.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    text = Loc.get("settings", isArabic),
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SettingsDropdownItem(
                        icon = Icons.Rounded.Language,
                        title = Loc.get("language", isArabic),
                        desc = Loc.get("language_desc", isArabic),
                        options = listOf("ar", "en"),
                        selectedOption = state.currentLanguage,
                        onOptionSelected = { viewModel.updateLanguage(it) },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsDropdownItem(
                        icon = Icons.Rounded.ColorLens,
                        title = Loc.get("appearance", isArabic),
                        desc = Loc.get("appearance_desc", isArabic) + ": " + state.currentTheme,
                        options = listOf("Cosmic", "Dark", "Light"),
                        selectedOption = state.currentTheme,
                        onOptionSelected = { viewModel.updateTheme(it) },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsClickableItem(
                        icon = Icons.Rounded.Backup,
                        title = Loc.get("backup", isArabic),
                        desc = Loc.get("backup_desc", isArabic),
                        onClick = {},
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsDropdownItem(
                        icon = Icons.Rounded.Audiotrack,
                        title = Loc.get("bass_freq", isArabic),
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
                        title = Loc.get("bass_gain", isArabic),
                        desc = "Default: 15dB",
                        options = listOf("10dB", "15dB", "20dB", "25dB"),
                        selectedOption = state.bassMaxGain,
                        onOptionSelected = { viewModel.updateBassMaxGain(it) },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsDropdownItem(
                        icon = Icons.Rounded.VolumeUp,
                        title = Loc.get("loudness_gain", isArabic),
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
                        title = Loc.get("audio_balance", isArabic),
                        desc = Loc.get("audio_balance_desc", isArabic),
                        checked = state.audioBalanceEnabled,
                        onCheckedChange = { viewModel.toggleAudioBalance() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.Merge,
                        title = Loc.get("always_bind_global", isArabic),
                        desc = Loc.get("always_bind_global_desc", isArabic),
                        checked = state.globalMixAlwaysBound,
                        onCheckedChange = { viewModel.toggleGlobalMixAlwaysBound() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.MusicNote,
                        title = Loc.get("connect_music_only", isArabic),
                        desc = Loc.get("connect_music_only_desc", isArabic),
                        checked = state.connectToMusicPlayersOnly,
                        onCheckedChange = { viewModel.toggleConnectToMusicPlayersOnly() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsDropdownItem(
                        icon = Icons.Rounded.Timelapse,
                        title = Loc.get("frame_duration", isArabic),
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
                        title = Loc.get("reverb", isArabic),
                        desc = Loc.get("reverb_desc", isArabic),
                        checked = state.reverbEnabled,
                        onCheckedChange = { viewModel.toggleReverb() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.LinearScale,
                        title = Loc.get("volume_bar", isArabic),
                        desc = Loc.get("volume_bar_desc", isArabic),
                        checked = state.showVolumeSlider,
                        onCheckedChange = { viewModel.toggleVolumeSlider() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.GridOn,
                        title = Loc.get("use_10_bands", isArabic),
                        desc = Loc.get("use_10_bands_desc", isArabic),
                        checked = state.use10Bands,
                        onCheckedChange = { viewModel.toggle10Bands() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.ReportProblem,
                        title = Loc.get("use_legacy", isArabic),
                        desc = Loc.get("use_legacy_desc", isArabic),
                        checked = state.useLegacyEffects,
                        onCheckedChange = { viewModel.toggleLegacyEffects() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsToggleItem(
                        icon = Icons.Rounded.Notifications,
                        title = Loc.get("notifications", isArabic),
                        desc = Loc.get("notifications_desc", isArabic),
                        checked = state.showNotification,
                        onCheckedChange = { viewModel.toggleShowNotification() },
                        isArabic = isArabic
                    )
                }

                item {
                    SettingsClickableItem(
                        icon = Icons.Rounded.Bluetooth,
                        title = Loc.get("saved_bt", isArabic),
                        desc = Loc.get("saved_bt_desc", isArabic),
                        onClick = {},
                        isArabic = isArabic
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1B2E)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2E2F45), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = Color(0xFF00D9A6)
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF1A1B2E))
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
                        Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp, textAlign = TextAlign.End)
                    }
                    Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF6C63FF))
                } else {
                    Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF6C63FF))
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
                        Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp, textAlign = TextAlign.Start)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1B2E)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2E2F45), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isArabic) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Color(0xFF00D9A6)
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
                        Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp, textAlign = TextAlign.End)
                    }
                    Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF6C63FF))
                } else {
                    Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF6C63FF))
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
                        Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp, textAlign = TextAlign.Start)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1B2E)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2E2F45), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00D9A6),
                    uncheckedThumbColor = Color(0xFF94A3B8),
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
                        Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp, textAlign = TextAlign.End)
                    }
                    Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF6C63FF))
                } else {
                    Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF6C63FF))
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                    ) {
                        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
                        Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp, textAlign = TextAlign.Start)
                    }
                }
            }
        }
    }
}
