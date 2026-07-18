package com.omni.equalizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omni.equalizer.ui.theme.OmniElevation
import com.omni.equalizer.ui.theme.OmniRadius
import com.omni.equalizer.ui.theme.OmniSpacing

/**
 * ── Shared design language for the redesigned UI ──
 * Every screen (Main, Settings, bottom sheets, dialogs) pulls colors and building blocks from
 * here so the app reads as one coherent product instead of four screens that each invented
 * their own card/border/spacing rules. Kept intentionally small: one background, one card
 * surface, one muted-text color, three accent colors used sparingly and only to mean something
 * (state, not decoration).
 */
val OmniBackground = Color(0xFF121220)
val OmniMutedText = Color(0xFF8B8DA8)
val OmniFaintText = Color(0xFF5B5D75)
val OmniCardBg = Color(0xFF181A28)
val OmniCardBgElevated = Color(0xFF1E2032)
val OmniCardBorder = Color(0xFFFFFFFF).copy(alpha = OmniElevation.strokeAlpha)
val OmniAccentIndigo = Color(0xFF8B7FF5)
val OmniAccentTeal = Color(0xFF00D9A6)
val OmniAccentPink = Color(0xFFFF6B9D)
val OmniAccentAmber = Color(0xFFFFB020)
val OmniAccentRed = Color(0xFFEF4444)

/** Small label above a grouped section. Sentence case (not shouty all-caps) with a soft accent
 *  dot so a screen reads as clearly separated groups instead of one dense wall of controls. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(bottom = OmniSpacing.sm, start = OmniSpacing.xs)
    ) {
        Text(
            text = text,
            color = OmniMutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp
        )
    }
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

/** The one card surface used everywhere content needs to be grouped: equalizer sections,
 *  settings groups, sheet lists. Flat, low-contrast, single hairline border — deliberately
 *  undecorated so it recedes and the content on top of it stays the focus. */
@Composable
fun OmniCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    padding: PaddingValues = PaddingValues(OmniSpacing.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(OmniRadius.xl),
        colors = CardDefaults.cardColors(containerColor = if (elevated) OmniCardBgElevated else OmniCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OmniCardBorder, RoundedCornerShape(OmniRadius.xl))
    ) {
        // NOTE: this MUST be a Column, not a Box. A Box stacks every direct child on top of
        // each other at the same position instead of laying them out one after another —
        // that was the cause of the equalizer graph, preset row, effect chips, and dials all
        // overlapping each other on the main screen.
        Column(modifier = Modifier.padding(padding), content = content)
    }
}

/** A small tonal "badge" behind a leading icon — reads as one soft shape instead of a bare
 *  icon floating in space, which is what made older list rows feel thin. Used across settings
 *  rows, sheet rows, and the dial cards. */
@Composable
fun OmniIconBadge(
    icon: ImageVector,
    tint: Color = OmniAccentIndigo,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(OmniRadius.small))
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
    }
}

/** Hairline separator used *inside* a grouped card between rows — quieter than a bordered
 *  card-per-row, which is what made the old Settings screen feel busy. */
@Composable
fun OmniDivider(modifier: Modifier = Modifier, startIndent: androidx.compose.ui.unit.Dp = 0.dp) {
    Row(modifier = modifier.fillMaxWidth()) {
        if (startIndent > 0.dp) Box(modifier = Modifier.width(startIndent))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = OmniElevation.dividerAlpha))
        )
    }
}

/** One grouped-list row: icon badge, title + description, trailing control. This single
 *  component replaces what used to be three near-identical bordered-Card composables
 *  (dropdown / clickable / toggle) — same visual rhythm everywhere a setting is listed,
 *  and the RTL handling lives in exactly one place. */
@Composable
fun OmniListRow(
    icon: ImageVector,
    title: String,
    desc: String,
    isArabic: Boolean,
    onClick: (() -> Unit)? = null,
    iconTint: Color = OmniAccentIndigo,
    titleColor: Color = Color.White,
    trailing: @Composable RowScope.() -> Unit
) {
    // isArabic is accepted for API consistency with the rest of the screen, but it is NOT
    // used to manually reorder anything below: once the app locale is actually Arabic,
    // Compose already mirrors Row/Column layout automatically (start<->end, left<->right).
    // Manually flipping order here on top of that automatic mirroring is what caused icons
    // to land in the wrong place — it mirrored twice. TextAlign.Start / Alignment.Start are
    // themselves direction-aware, so leaving them as "Start" is correct in both languages.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = OmniSpacing.lg, vertical = OmniSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = true),
            horizontalArrangement = Arrangement.spacedBy(OmniSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OmniIconBadge(icon, iconTint)
            Column(modifier = Modifier.weight(1f, fill = true), horizontalAlignment = Alignment.Start) {
                Text(
                    title,
                    color = titleColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                )
                if (desc.isNotBlank()) {
                    Text(
                        desc,
                        color = OmniMutedText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}

/** Flat circular tonal icon button — used for the settings/help/back actions in the top bars.
 *  A soft filled circle reads as "tappable" without needing a border or a Material ripple
 *  square, which is what made the old plain [IconButton]s feel a little bare. */
@Composable
fun OmniCircularIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    testTag: String? = null
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(20.dp))
    }
}

/** Helper for building the [OmniGroupedCard] row list — using a vararg here (rather than
 *  `listOf(...)` directly at the call site) keeps each row lambda's composable context
 *  unambiguous for the compiler. */
fun omniRows(vararg rows: @Composable () -> Unit): List<@Composable () -> Unit> = rows.toList()

/** A grouped card that lays out [rows] with a single hairline divider between each — the
 *  "iOS-style settings group" pattern. Pass row content via [OmniListRow] children. */
@Composable
fun OmniGroupedCard(modifier: Modifier = Modifier, rows: List<@Composable () -> Unit>) {
    Card(
        shape = RoundedCornerShape(OmniRadius.xl),
        colors = CardDefaults.cardColors(containerColor = OmniCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OmniCardBorder, RoundedCornerShape(OmniRadius.xl))
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                row()
                if (index != rows.lastIndex) OmniDivider(startIndent = 66.dp)
            }
        }
    }
}
