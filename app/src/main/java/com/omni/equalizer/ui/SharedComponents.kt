package com.omni.equalizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omni.equalizer.ui.theme.OmniRadius
import com.omni.equalizer.ui.theme.OmniSpacing

/** Shared muted/neutral colors used across the redesigned screens for consistency. */
val OmniMutedText = Color(0xFF8B8DA8)
val OmniCardBg = Color(0xFF171826)
val OmniCardBorder = Color(0xFFFFFFFF).copy(alpha = 0.08f)
val OmniAccentIndigo = Color(0xFF8B7FF5)
val OmniAccentTeal = Color(0xFF00D9A6)
val OmniAccentPink = Color(0xFFFF6B9D)

/** Small uppercase label used above every grouped section so a screen reads as clearly
 *  separated groups instead of one dense wall of controls. Shared by Main, Settings, and
 *  bottom sheets. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = OmniMutedText,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        modifier = modifier.padding(bottom = OmniSpacing.sm, start = OmniSpacing.xs)
    )
}

/** Small "grabber" bar shown at the top of every bottom sheet — a standard affordance that
 *  signals "this is a draggable sheet" and gives the content some breathing room before it
 *  starts, instead of content butting right up against the top edge. */
@Composable
fun SheetGrabber(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = OmniSpacing.sm, bottom = OmniSpacing.xs)
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(OmniRadius.pill))
            .background(OmniMutedText.copy(alpha = 0.3f))
    )
}
