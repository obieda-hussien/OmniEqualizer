package com.omni.equalizer.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omni.equalizer.ui.theme.OmniRadius
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * ── Redesigned equalizer visuals ──
 * Same public API as before (so [MainScreen] and the view model wiring didn't need to change),
 * but every drawing routine was simplified: fewer overlapping layers, one accent per element,
 * no decorative motion competing with the actual data. The goal is a screen that reads like a
 * clear instrument, not a busy dashboard.
 */

// ── VERTICAL BAND SLIDER ──
@Composable
fun VerticalEqualizerBand(
    freq: String,
    dbValue: Float,
    isEnabled: Boolean,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = (dbValue + 15f) / 30f
    val accent = OmniAccentIndigo

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = String.format(java.util.Locale.ROOT, "%+.1f", dbValue),
            color = if (isEnabled) Color.White.copy(alpha = 0.85f) else OmniFaintText,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .width(24.dp)
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
            // Thin static track — one visual weight, no double-line effect.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight()
                    .width(3.dp)
                    .clip(RoundedCornerShape(OmniRadius.pill))
                    .background(Color.White.copy(alpha = 0.08f))
            )

            // Filled portion up to the current value.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(3.dp)
                    .fillMaxHeight(progressFraction.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(OmniRadius.pill))
                    .background(if (isEnabled) accent.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.08f))
            )

            // Flat, borderless thumb — calmer than a white-ringed dot.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-progressFraction * 200).dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (isEnabled) accent else Color(0xFF3A3C52))
            )
        }

        Text(
            text = freq,
            color = if (isEnabled) OmniMutedText else OmniFaintText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
    }
}

// ── CALM REFERENCE GRID (replaces the old animated wave background) ──
@Composable
fun AdvancedDspBackgroundVisualizer(isActive: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val dash = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 6.dp.toPx()), 0f)

        // Faint guide lines at +10/+5/0/-5/-10 dB so the curve has a readable frame of
        // reference, without the constant animated motion the old wave simulation had.
        val steps = listOf(0.166f, 0.333f, 0.5f, 0.666f, 0.833f)
        steps.forEach { fraction ->
            val y = h * fraction
            val isCenter = fraction == 0.5f
            drawLine(
                color = Color.White.copy(alpha = if (isCenter) 0.10f else 0.045f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = if (isCenter) null else dash
            )
        }

        if (isActive) {
            // A very soft vertical wash so the enabled state still feels alive, without a
            // moving waveform competing for attention.
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        OmniAccentIndigo.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                size = Size(w, h * 0.6f)
            )
        }
    }
}

// ── SMOOTH BEZIER EQUALIZER CURVE ──
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

        val strokeColor = if (isEnabled) OmniAccentIndigo else Color(0xFF475569)
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(
                width = 2.dp.toPx(),
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
                        OmniAccentIndigo.copy(alpha = 0.10f),
                        Color.Transparent
                    )
                )
            )
        }
    }
}

// ── SEMI-CIRCULAR CONTROL DIAL ──
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

    OmniCard(modifier = modifier, padding = PaddingValues(vertical = 14.dp, horizontal = 8.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = if (isEnabled) Color.White else OmniMutedText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .pointerInput(isEnabled) {
                        if (!isEnabled) return@pointerInput
                        detectDragGestures { change, _ ->
                            change.consume()
                            val center = this.size.width / 2f
                            val touchX = change.position.x - center
                            val touchY = change.position.y - center
                            val angleRad = atan2(touchY, touchX)
                            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                            angleDeg = (angleDeg + 360f) % 360f

                            val progress = when {
                                angleDeg in 150f..390f -> (angleDeg - 150f) / sweepAngleLimit
                                angleDeg < 150f && angleDeg <= 30f -> (angleDeg + 360f - 150f) / sweepAngleLimit
                                angleDeg < 90f -> 1.0f
                                else -> 0.0f
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
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = startAngleLimit,
                        sweepAngle = sweepAngleLimit,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    if (isEnabled) {
                        drawArc(
                            color = color,
                            startAngle = startAngleLimit,
                            sweepAngle = progressSweep,
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    val finalAngle = startAngleLimit + progressSweep
                    val rad = Math.toRadians(finalAngle.toDouble())
                    val indicatorX = centerOffset.x + (radius) * cos(rad).toFloat()
                    val indicatorY = centerOffset.y + (radius) * sin(rad).toFloat()

                    if (isEnabled) {
                        drawCircle(
                            color = color,
                            radius = 3.dp.toPx(),
                            center = Offset(indicatorX, indicatorY)
                        )
                    }
                }

                Text(
                    text = "${value.toInt()}%",
                    color = if (isEnabled) Color.White else OmniFaintText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Switch(
                checked = switchChecked,
                onCheckedChange = { onSwitchChange() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = color,
                    uncheckedThumbColor = OmniMutedText,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier
                    .scale(0.8f)
                    .testTag("switch_${label.lowercase().replace(" ", "_")}")
            )
        }
    }
}

// ── LIVE SPECTRUM VISUALIZER ──
@Composable
fun LiveSpectrumVisualizer(
    isActive: Boolean,
    levels: FloatArray,
    modifier: Modifier = Modifier
) {
    val barCount = levels.size.coerceAtLeast(1)

    // Spring-smoothed toward the REAL captured levels — this keeps the premium fluid motion
    // while the underlying numbers are genuine FFT energy, not fake randomness.
    val animatedLevels = List(barCount) { index ->
        val target = if (isActive) levels.getOrElse(index) { 0f } else 0f
        animateFloatAsState(
            targetValue = target.coerceIn(0.05f, 1f),
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "spectrum_bar_$index"
        )
    }

    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height
        val spacing = 3.dp.toPx()
        val barWidth = (width - (spacing * (barCount - 1))) / barCount

        for (i in 0 until barCount) {
            val scale = animatedLevels[i].value
            val barHeight = (scale * height).coerceAtLeast(2.dp.toPx())
            val x = i * (barWidth + spacing)
            val y = height - barHeight

            drawRoundRect(
                color = if (isActive) OmniAccentIndigo else Color.White.copy(alpha = 0.12f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            )
        }
    }
}
