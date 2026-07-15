package com.omni.equalizer.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Single source of truth for spacing/shape rhythm across the app. Every redesigned screen
 * (Main, Settings, bottom sheets, dialogs) pulls from here instead of scattering ad-hoc dp
 * values, which is what made the previous layouts feel inconsistent and cramped in places.
 */
object OmniSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 40.dp
}

object OmniRadius {
    val small = 12.dp
    val medium = 18.dp
    val large = 24.dp
    /** Outer "hero" containers — the big equalizer card, sheet/dialog frames. */
    val xl = 28.dp
    val pill = 100.dp
}

/** Consistent surface elevation via alpha layering (matches the app's flat dark-mode style). */
object OmniElevation {
    const val cardAlpha = 0.05f
    const val cardAlphaElevated = 0.08f
    const val strokeAlpha = 0.08f
    /** Even quieter hairline used between rows inside a grouped list, so dividers read as
     *  structure rather than as extra borders competing with the card outline. */
    const val dividerAlpha = 0.06f
}
