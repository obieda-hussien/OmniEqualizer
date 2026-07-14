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
                "help_body" -> "يعمل OmniEqualizer على مستوى الـ Global Mix مباشرة — أي أنه يعالج كل الصوت الخارج من هاتفك في نقطة واحدة قبل ما يوصل لأي جهاز إخراج.\n\nمعنى كده: هيشتغل بنفس الكفاءة سواء كنت سامع من سماعة بلوتوث متوصلة، أو من سماعة الهاتف الأساسية من غير أي بلوتوث خالص — مش محتاج تربط التطبيق بمشغل موسيقى معين زي سبوتيفاي أو يوتيوب ميوزيك، هو شغال على أي صوت بيطلع من الجهاز تلقائيًا.\n\nملحوظة مهمة: بعض الشركات (زي بعض أجهزة سامسونج أو شاومي) بتقيّد وصول التطبيقات لهذا المستوى من النظام. لو شفت تنبيه في الشاشة الرئيسية بيقول إن تأثير معيّن مش شغال، فده معناه إن جهازك بيمنعه فعليًا — مش أن التطبيق بايظ.\n\nقم بإيقاف تحسينات البطارية لهذا التطبيق للحصول على أفضل تجربة، عشان النظام ميوقفش التأثيرات في الخلفية."
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
                "help_body" -> "OmniEqualizer works directly at the Global Mix level — it processes all audio leaving your phone at a single point, before it reaches any output device.\n\nThat means it works identically whether you're listening through connected Bluetooth headphones or the phone's own built-in speaker with no Bluetooth at all — you don't need to bind it to a specific player like Spotify or YouTube Music, it applies automatically to whatever is playing.\n\nImportant: some OEMs (certain Samsung or Xiaomi devices) restrict third-party apps from reaching this level of the system. If the main screen shows a warning that an effect isn't available, that's your device blocking it — not the app being broken.\n\nTurn off battery optimization for this app for the best experience, so the system doesn't kill the effects in the background."
                else -> key
            }
        }
    }
}

