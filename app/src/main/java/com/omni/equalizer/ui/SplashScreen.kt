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

/**
 * Splash screen redesigned to directly mirror the app icon (orbit ring + 5 equalizer bars)
 * instead of an unrelated 3-slider mockup, so the brand feels consistent from launcher tap
 * to first frame. Text corrected to the real product name — the previous "OMNIEQUALIZER PRO"
 * subtitle implied a paid tier that doesn't exist.
 */
@Composable
fun EqualizerSplashScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val barHeights = listOf(0.45f, 0.7f, 1f, 0.7f, 0.45f)

    Box(
        modifier = modifier
            .background(Color(0xFF121220))
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .scale(scalePulse),
                contentAlignment = Alignment.Center
            ) {
                // Soft glow behind everything, matching the app icon's radial glow
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(OmniAccentIndigo.copy(alpha = 0.22f), Color.Transparent)
                            )
                        )
                )

                // The "Omni" orbit ring motif, rotating slowly
                Canvas(
                    modifier = Modifier
                        .size(124.dp)
                        .rotate(ringRotation)
                ) {
                    val strokeWidth = 3.dp.toPx()
                    drawArc(
                        brush = Brush.linearGradient(
                            listOf(OmniAccentIndigo, Color(0xFFB26BE0), OmniAccentPink)
                        ),
                        startAngle = -40f,
                        sweepAngle = 250f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Five equalizer bars, echoing the launcher icon exactly
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(64.dp)
                ) {
                    barHeights.forEach { fraction ->
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(OmniRadius.pill))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(OmniAccentIndigo, Color(0xFFB26BE0), OmniAccentPink)
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(OmniSpacing.xxxl))

            Text(
                text = "OmniEqualizer",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(OmniSpacing.xs))

            Text(
                text = "SYSTEM-WIDE AUDIO ENGINE",
                color = OmniAccentIndigo,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }
    }
}
