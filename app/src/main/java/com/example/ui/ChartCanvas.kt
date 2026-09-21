package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Candle
import kotlin.math.max
import kotlin.math.min

@Composable
fun ChartCanvas(
    candles: List<Candle>,
    currentPrice: Double,
    supportLevel: Double,
    resistanceLevel: Double,
    isScanning: Boolean,
    scanProgressText: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Laser scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    val laserGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_glow"
    )

    Box(modifier = modifier.background(Color(0xFF0C101A))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val rightAxisWidth = 85.dp.toPx()
            val chartWidth = width - rightAxisWidth
            val chartHeight = height - 28.dp.toPx()

            if (candles.isEmpty()) return@Canvas

            // Compute price min and max
            val allHighs = candles.map { it.high } + listOf(resistanceLevel, currentPrice)
            val allLows = candles.map { it.low } + listOf(supportLevel, currentPrice)
            var maxPrice = allHighs.maxOrNull() ?: currentPrice
            var minPrice = allLows.minOrNull() ?: currentPrice
            val priceSpan = max(0.0001, maxPrice - minPrice)

            // Add margin to chart bounds
            val paddedMax = maxPrice + (priceSpan * 0.08)
            val paddedMin = minPrice - (priceSpan * 0.08)
            val paddedSpan = paddedMax - paddedMin

            fun priceToY(price: Double): Float {
                val ratio = ((paddedMax - price) / paddedSpan).toFloat()
                return (ratio * chartHeight).coerceIn(4f, chartHeight)
            }

            // Draw Background Grid
            val gridColor = Color(0xFF1B2436)
            val gridStepsY = 6
            for (i in 0..gridStepsY) {
                val y = (chartHeight / gridStepsY) * i
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f
                )

                // Price text on right axis
                val priceAtY = paddedMax - (paddedSpan * (y / chartHeight))
                val priceStr = "%.5f".format(priceAtY)
                val measured = textMeasurer.measure(
                    text = priceStr,
                    style = TextStyle(
                        color = Color(0xFF6B7B96),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(chartWidth + 8.dp.toPx(), y - (measured.size.height / 2f))
                )
            }

            // Draw Vertical Grid Lines
            val gridStepsX = 5
            for (i in 0..gridStepsX) {
                val x = (chartWidth / gridStepsX) * i
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, chartHeight),
                    strokeWidth = 1f
                )
            }

            // Draw Support and Resistance lines
            val supY = priceToY(supportLevel)
            val resY = priceToY(resistanceLevel)

            // Resistance line (Dotted Red)
            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            drawLine(
                color = Color(0x99FF5252),
                start = Offset(0f, resY),
                end = Offset(chartWidth, resY),
                strokeWidth = 1.5f,
                pathEffect = dashedEffect
            )
            val resText = textMeasurer.measure(
                text = "RES: ${"%.5f".format(resistanceLevel)}",
                style = TextStyle(color = Color(0xFFFF5252), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            )
            drawText(resText, topLeft = Offset(8.dp.toPx(), resY - 14.dp.toPx()))

            // Support line (Dotted Cyan/Green)
            drawLine(
                color = Color(0x9900E5FF),
                start = Offset(0f, supY),
                end = Offset(chartWidth, supY),
                strokeWidth = 1.5f,
                pathEffect = dashedEffect
            )
            val supText = textMeasurer.measure(
                text = "SUP: ${"%.5f".format(supportLevel)}",
                style = TextStyle(color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            )
            drawText(supText, topLeft = Offset(8.dp.toPx(), supY + 4.dp.toPx()))

            // Draw Moving Average (EMA 9)
            if (candles.size >= 3) {
                val emaPath = Path()
                var k = 2.0 / (7.0 + 1.0)
                var prevEma = candles.first().close
                val candleWidth = chartWidth / candles.size

                candles.forEachIndexed { index, candle ->
                    val ema = (candle.close * k) + (prevEma * (1.0 - k))
                    prevEma = ema
                    val x = (index * candleWidth) + (candleWidth / 2f)
                    val y = priceToY(ema)
                    if (index == 0) {
                        emaPath.moveTo(x, y)
                    } else {
                        emaPath.lineTo(x, y)
                    }
                }
                drawPath(
                    path = emaPath,
                    color = Color(0xCCFFD700),
                    style = Stroke(width = 1.8f)
                )
            }

            // Draw Candlesticks
            val numCandles = candles.size
            val candleSpacing = chartWidth / numCandles
            val candleBarWidth = max(4f, candleSpacing * 0.65f)

            candles.forEachIndexed { index, candle ->
                val centerX = (index * candleSpacing) + (candleSpacing / 2f)
                val openY = priceToY(candle.open)
                val closeY = priceToY(candle.close)
                val highY = priceToY(candle.high)
                val lowY = priceToY(candle.low)

                val isGreen = candle.close >= candle.open
                val candleColor = if (isGreen) Color(0xFF00E676) else Color(0xFFFF3D00)

                // Draw Wick
                drawLine(
                    color = candleColor,
                    start = Offset(centerX, highY),
                    end = Offset(centerX, lowY),
                    strokeWidth = 1.5f
                )

                // Draw Candle Body
                val bodyTop = min(openY, closeY)
                val bodyBottom = max(openY, closeY)
                val bodyHeight = max(2f, bodyBottom - bodyTop)

                drawRect(
                    color = candleColor,
                    topLeft = Offset(centerX - (candleBarWidth / 2f), bodyTop),
                    size = Size(candleBarWidth, bodyHeight)
                )

                // Subtle inner glow for the newest candle
                if (index == numCandles - 1) {
                    drawCircle(
                        color = candleColor.copy(alpha = 0.35f),
                        radius = candleBarWidth * 1.5f,
                        center = Offset(centerX, closeY)
                    )
                }
            }

            // Draw Current Live Price Line & Right Badge (Matches Quotex 0.19361 blue tag)
            val currentY = priceToY(currentPrice)
            val liveLineEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            drawLine(
                color = Color(0xFF00B0FF),
                start = Offset(0f, currentY),
                end = Offset(chartWidth, currentY),
                strokeWidth = 1.5f,
                pathEffect = liveLineEffect
            )

            // Live Price Tag Badge on right
            val priceStr = "%.5f".format(currentPrice)
            val priceTagMeas = textMeasurer.measure(
                text = priceStr,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
            val tagHeight = 22.dp.toPx()
            val tagWidth = rightAxisWidth - 6.dp.toPx()
            val tagTop = currentY - (tagHeight / 2f)

            // Draw glowing rounded badge
            drawRect(
                color = Color(0xFF0091EA),
                topLeft = Offset(chartWidth + 2.dp.toPx(), tagTop),
                size = Size(tagWidth, tagHeight)
            )
            drawText(
                textLayoutResult = priceTagMeas,
                topLeft = Offset(
                    chartWidth + 6.dp.toPx(),
                    tagTop + ((tagHeight - priceTagMeas.size.height) / 2f)
                )
            )

            // Trade Timer Expiration Vertical Line (like 00:15 in screenshot)
            val expX = chartWidth * 0.82f
            drawLine(
                color = Color(0x66FFFFFF),
                start = Offset(expX, 0f),
                end = Offset(expX, chartHeight),
                strokeWidth = 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )
            val timerMeas = textMeasurer.measure(
                text = "00:15",
                style = TextStyle(
                    color = Color(0xFF00E5FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            drawText(timerMeas, topLeft = Offset(expX - (timerMeas.size.width / 2f), 8.dp.toPx()))

            // LASER SCANNER OVERLAY
            if (isScanning) {
                val laserY = chartHeight * laserYRatio

                // Translucent scan grid shadow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x0000E5FF),
                            Color(0x2200E5FF),
                            Color(0x5500E5FF).copy(alpha = laserGlowAlpha * 0.4f),
                            Color(0x0000E5FF)
                        ),
                        startY = max(0f, laserY - 60.dp.toPx()),
                        endY = laserY + 60.dp.toPx()
                    ),
                    topLeft = Offset(0f, max(0f, laserY - 60.dp.toPx())),
                    size = Size(chartWidth, 120.dp.toPx())
                )

                // Laser Main Beam Line
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x0000E5FF),
                            Color(0xFF00E5FF),
                            Color.White,
                            Color(0xFF00E5FF),
                            Color(0x0000E5FF)
                        )
                    ),
                    start = Offset(0f, laserY),
                    end = Offset(chartWidth, laserY),
                    strokeWidth = 3.5f
                )

                // Outer Laser Glow
                drawLine(
                    color = Color(0x8800E5FF),
                    start = Offset(0f, laserY),
                    end = Offset(chartWidth, laserY),
                    strokeWidth = 8f
                )

                // Scanning Crosshair Target Circle in center
                val centerX = chartWidth * 0.5f
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = laserGlowAlpha),
                    radius = 24.dp.toPx(),
                    center = Offset(centerX, laserY),
                    style = Stroke(width = 2f)
                )
                drawLine(
                    color = Color.White.copy(alpha = laserGlowAlpha),
                    start = Offset(centerX - 32.dp.toPx(), laserY),
                    end = Offset(centerX + 32.dp.toPx(), laserY),
                    strokeWidth = 1.5f
                )
            }
        }

        // Live Scanning Status Banner Overlay
        if (isScanning) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color(0xDD0A101D), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "⚡ $scanProgressText",
                    color = Color(0xFF00E5FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
