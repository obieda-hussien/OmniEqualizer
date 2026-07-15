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
            text = String.format(java.util.Locale.ROOT, "%+.1f", dbValue),
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
    levels: FloatArray,
    modifier: Modifier = Modifier
) {
    val barCount = levels.size.coerceAtLeast(1)

    // Spring-smoothed toward the REAL captured levels — this keeps the premium fluid motion
    // while the underlying numbers are genuine FFT energy, not `(280..650).random()` fakery.
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
        val spacing = 4.dp.toPx()
        val barWidth = (width - (spacing * (barCount - 1))) / barCount

        for (i in 0 until barCount) {
            val scale = animatedLevels[i].value
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

