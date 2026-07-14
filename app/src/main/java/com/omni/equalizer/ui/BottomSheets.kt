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

