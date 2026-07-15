package com.omni.equalizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omni.equalizer.R
import com.omni.equalizer.data.EqualizerPreset
import com.omni.equalizer.ui.theme.OmniRadius
import com.omni.equalizer.ui.theme.OmniSpacing

/**
 * ── Redesigned bottom sheets ──
 * Same three sheets (Help / Presets list / Preset options), rebuilt on the shared grabber +
 * spacing rhythm from [SharedComponents]. Preset rows go from "kebab menu on the left, a row
 * of tiny tag chips and a name crammed on the right" to a single clear line per preset: a
 * radio-style selection indicator, the name, one small muted tag caption, and the options menu
 * — each with one job. The options sheet collapses four near-duplicated RTL-aware Row blocks
 * into one reusable row.
 */

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
            .padding(horizontal = OmniSpacing.xl, vertical = OmniSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SheetGrabber()

        Spacer(modifier = Modifier.height(OmniSpacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OmniIconBadge(icon = Icons.AutoMirrored.Rounded.HelpOutline, tint = OmniAccentIndigo)
            Text(
                text = stringResource(R.string.help_title),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            OmniCircularIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = "Close",
                onClick = onDismiss
            )
        }

        Spacer(modifier = Modifier.height(OmniSpacing.lg))

        Text(
            text = stringResource(R.string.help_body),
            color = OmniMutedText,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = if (isArabic) TextAlign.End else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(OmniSpacing.xl))
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
            .heightIn(max = 560.dp)
            .padding(top = OmniSpacing.sm, bottom = OmniSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SheetGrabber()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OmniSpacing.xl, vertical = OmniSpacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.load_preset),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onAddCustomPreset,
                colors = ButtonDefaults.buttonColors(containerColor = OmniAccentIndigo),
                shape = RoundedCornerShape(OmniRadius.medium),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.add_preset_btn), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .padding(horizontal = OmniSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(OmniSpacing.xs)
        ) {
            items(presets) { preset ->
                val isSelected = preset.id == activePresetId
                PresetRow(
                    preset = preset,
                    isSelected = isSelected,
                    isArabic = isArabic,
                    onClick = { onPresetSelect(preset) },
                    onOptionsClick = { onOptionsClick(preset) }
                )
            }
        }
    }
}

@Composable
private fun PresetRow(
    preset: EqualizerPreset,
    isSelected: Boolean,
    isArabic: Boolean,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    val tagCaption = preset.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.large))
            .background(if (isSelected) OmniAccentIndigo.copy(alpha = 0.14f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = OmniSpacing.md, vertical = OmniSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(OmniSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator — a simple filled-vs-outline dot reads clearer at a glance
            // than a checkmark icon competing with the name for attention.
            Box(
                modifier = Modifier.size(18.dp).clip(CircleShape).background(
                    if (isSelected) OmniAccentIndigo else Color.White.copy(alpha = 0.08f)
                ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            val textColumn = @Composable {
                Column(horizontalAlignment = if (isArabic) Alignment.End else Alignment.Start) {
                    Text(
                        text = preset.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = if (isArabic) TextAlign.End else TextAlign.Start
                    )
                    if (tagCaption.isNotBlank()) {
                        Text(
                            text = tagCaption,
                            color = OmniAccentTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = if (isArabic) TextAlign.End else TextAlign.Start
                        )
                    }
                }
            }
            textColumn()
        }

        IconButton(onClick = onOptionsClick, modifier = Modifier.size(36.dp)) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = OmniMutedText, modifier = Modifier.size(19.dp))
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
            .padding(horizontal = OmniSpacing.xl, vertical = OmniSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SheetGrabber()

        Spacer(modifier = Modifier.height(OmniSpacing.sm))

        Text(
            text = preset.name,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = if (isArabic) TextAlign.End else TextAlign.Start
        )

        Spacer(modifier = Modifier.height(OmniSpacing.md))

        OptionRow(
            icon = Icons.Rounded.Edit,
            label = stringResource(R.string.edit_preset),
            tint = OmniAccentIndigo,
            isArabic = isArabic,
            onClick = { onEdit(); onDismiss() }
        )
        OptionRow(
            icon = Icons.Rounded.Share,
            label = stringResource(R.string.share_profile),
            tint = OmniAccentTeal,
            isArabic = isArabic,
            onClick = onDismiss
        )
        OptionRow(
            icon = Icons.Rounded.FileDownload,
            label = stringResource(R.string.import_profile),
            tint = OmniAccentTeal,
            isArabic = isArabic,
            onClick = onDismiss
        )
        if (preset.isCustom) {
            OptionRow(
                icon = Icons.Rounded.Delete,
                label = stringResource(R.string.delete_profile),
                tint = OmniAccentRed,
                isArabic = isArabic,
                onClick = { onDelete(); onDismiss() }
            )
        }

        Spacer(modifier = Modifier.height(OmniSpacing.lg))
    }
}

/** One reusable action row — replaces four copies of the same "icon + label, order flipped
 *  for RTL" block that used to be written out by hand for Edit/Share/Import/Delete. */
@Composable
private fun OptionRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    isArabic: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OmniRadius.medium))
            .clickable(onClick = onClick)
            .padding(vertical = OmniSpacing.md, horizontal = OmniSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(OmniSpacing.md, if (isArabic) Alignment.End else Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconEl = @Composable { OmniIconBadge(icon = icon, tint = tint) }
        val labelEl = @Composable { Text(label, color = if (tint == OmniAccentRed) tint else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium) }

        if (isArabic) {
            labelEl()
            iconEl()
        } else {
            iconEl()
            labelEl()
        }
    }
}
