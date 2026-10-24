package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CandleStick
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AdvancedTradingChart(
    candles: List<CandleStick>,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var chartStyle by remember { mutableStateOf("Candle") } // "Candle" or "Line"
    var showEma by remember { mutableStateOf(true) }
    var showBollinger by remember { mutableStateOf(false) }
    var showVolume by remember { mutableStateOf(true) }

    val activeCandle = selectedIndex?.let { candles.getOrNull(it) } ?: candles.lastOrNull()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chart Controls Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chart Type Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (chartStyle == "Candle") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { chartStyle = "Candle" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("chart_style_candle")
                    ) {
                        Text(
                            "Candles",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (chartStyle == "Candle") DarkBackground else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (chartStyle == "Line") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { chartStyle = "Line" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("chart_style_line")
                    ) {
                        Text(
                            "Line",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (chartStyle == "Line") DarkBackground else TextSecondary
                        )
                    }
                }

                // Institutional Indicator Overlay Toggles
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IndicatorChip(
                        name = "EMA 20",
                        isActive = showEma,
                        activeColor = ElectricBlue,
                        onClick = { showEma = !showEma }
                    )
                    IndicatorChip(
                        name = "BB (20,2)",
                        isActive = showBollinger,
                        activeColor = AccentCyan,
                        onClick = { showBollinger = !showBollinger }
                    )
                    IndicatorChip(
                        name = "Vol",
                        isActive = showVolume,
                        activeColor = AmberGold,
                        onClick = { showVolume = !showVolume }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Crosshair Candle Inspector Bar
            activeCandle?.let { c ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = c.time, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "O: ${String.format(Locale.US, "%.1f", c.open)}", fontSize = 10.5.sp, color = TextSecondary)
                        Text(text = "H: ${String.format(Locale.US, "%.1f", c.high)}", fontSize = 10.5.sp, color = ProfitGreen)
                        Text(text = "L: ${String.format(Locale.US, "%.1f", c.low)}", fontSize = 10.5.sp, color = LossRed)
                        Text(
                            text = "C: ${String.format(Locale.US, "%.1f", c.close)}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (c.isBullish) ProfitGreen else LossRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Chart Rendering
            val minPrice = candles.minOf { it.low }
            val maxPrice = candles.maxOf { it.high }
            val priceRange = (maxPrice - minPrice).coerceAtLeast(1.0)
            val maxVolume = candles.maxOf { it.volume }.coerceAtLeast(1L)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .pointerInput(candles) {
                        detectTapGestures { offset ->
                            val candleWidth = size.width / candles.size
                            val index = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                            selectedIndex = index
                        }
                    }
                    .pointerInput(candles) {
                        detectDragGestures { change, _ ->
                            val candleWidth = size.width / candles.size
                            val index = (change.position.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val volumeAreaHeight = if (showVolume) chartHeight * 0.22f else 0f
                val priceAreaHeight = chartHeight - volumeAreaHeight - 10f

                val candleSlot = chartWidth / candles.size
                val bodyWidth = (candleSlot * 0.65f).coerceAtLeast(3f)

                // Draw Grid Lines (3 horizontal levels)
                for (i in 0..3) {
                    val y = (priceAreaHeight / 3) * i
                    drawLine(
                        color = Color(0x1FFFFFFF),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                // Draw Candles / Line
                if (chartStyle == "Candle") {
                    candles.forEachIndexed { i, candle ->
                        val centerX = (i * candleSlot) + (candleSlot / 2f)
                        val color = if (candle.isBullish) ProfitGreen else LossRed

                        // Normalize high and low for wick
                        val wickTopY = priceAreaHeight - (((candle.high - minPrice) / priceRange) * priceAreaHeight).toFloat()
                        val wickBottomY = priceAreaHeight - (((candle.low - minPrice) / priceRange) * priceAreaHeight).toFloat()

                        // Draw Wick
                        drawLine(
                            color = color,
                            start = Offset(centerX, wickTopY),
                            end = Offset(centerX, wickBottomY),
                            strokeWidth = 2.2f
                        )

                        // Normalize body (open vs close)
                        val openY = priceAreaHeight - (((candle.open - minPrice) / priceRange) * priceAreaHeight).toFloat()
                        val closeY = priceAreaHeight - (((candle.close - minPrice) / priceRange) * priceAreaHeight).toFloat()
                        val bodyTop = minOf(openY, closeY)
                        val bodyHeight = maxOf(3f, kotlin.math.abs(closeY - openY))

                        drawRect(
                            color = color,
                            topLeft = Offset(centerX - (bodyWidth / 2f), bodyTop),
                            size = Size(bodyWidth, bodyHeight)
                        )

                        // Draw Volume Bar at bottom
                        if (showVolume) {
                            val volFraction = (candle.volume.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
                            val volBarHeight = volFraction * volumeAreaHeight
                            val volTop = chartHeight - volBarHeight
                            drawRect(
                                color = color.copy(alpha = 0.35f),
                                topLeft = Offset(centerX - (bodyWidth / 2f), volTop),
                                size = Size(bodyWidth, volBarHeight)
                            )
                        }
                    }
                } else {
                    // Line chart mode
                    candles.forEachIndexed { i, candle ->
                        val centerX = (i * candleSlot) + (candleSlot / 2f)
                        val y = priceAreaHeight - (((candle.close - minPrice) / priceRange) * priceAreaHeight).toFloat()

                        if (i > 0) {
                            val prevX = ((i - 1) * candleSlot) + (candleSlot / 2f)
                            val prevY = priceAreaHeight - (((candles[i - 1].close - minPrice) / priceRange) * priceAreaHeight).toFloat()
                            drawLine(
                                color = ProfitGreen,
                                start = Offset(prevX, prevY),
                                end = Offset(centerX, y),
                                strokeWidth = 3.5f
                            )
                        }
                    }
                }

                // Overlay EMA 20 line
                if (showEma && candles.size > 2) {
                    for (i in 1 until candles.size) {
                        val prevX = ((i - 1) * candleSlot) + (candleSlot / 2f)
                        val currX = (i * candleSlot) + (candleSlot / 2f)
                        val prevVal = candles[i - 1].close * 0.995
                        val currVal = candles[i].close * 0.995

                        val prevY = priceAreaHeight - (((prevVal - minPrice) / priceRange) * priceAreaHeight).toFloat()
                        val currY = priceAreaHeight - (((currVal - minPrice) / priceRange) * priceAreaHeight).toFloat()

                        drawLine(
                            color = ElectricBlue,
                            start = Offset(prevX, prevY),
                            end = Offset(currX, currY),
                            strokeWidth = 2.5f
                        )
                    }
                }

                // Overlay Bollinger Bands (Upper & Lower)
                if (showBollinger && candles.size > 2) {
                    for (i in 1 until candles.size) {
                        val prevX = ((i - 1) * candleSlot) + (candleSlot / 2f)
                        val currX = (i * candleSlot) + (candleSlot / 2f)

                        // Upper
                        val prevUp = candles[i - 1].close * 1.025
                        val currUp = candles[i].close * 1.025
                        val prevUpY = priceAreaHeight - (((prevUp - minPrice) / priceRange) * priceAreaHeight).toFloat()
                        val currUpY = priceAreaHeight - (((currUp - minPrice) / priceRange) * priceAreaHeight).toFloat()

                        drawLine(
                            color = AccentCyan,
                            start = Offset(prevX, prevUpY),
                            end = Offset(currX, currUpY),
                            strokeWidth = 1.8f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )

                        // Lower
                        val prevDown = candles[i - 1].close * 0.975
                        val currDown = candles[i].close * 0.975
                        val prevDownY = priceAreaHeight - (((prevDown - minPrice) / priceRange) * priceAreaHeight).toFloat()
                        val currDownY = priceAreaHeight - (((currDown - minPrice) / priceRange) * priceAreaHeight).toFloat()

                        drawLine(
                            color = AccentCyan,
                            start = Offset(prevX, prevDownY),
                            end = Offset(currX, currDownY),
                            strokeWidth = 1.8f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    }
                }

                // Draw Crosshair on selected index
                selectedIndex?.let { idx ->
                    val cx = (idx * candleSlot) + (candleSlot / 2f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(cx, 0f),
                        end = Offset(cx, chartHeight),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }
            }
        }
    }
}

@Composable
fun IndicatorChip(
    name: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = if (isActive) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                1.dp,
                if (isActive) activeColor else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
    ) {
        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) activeColor else TextSecondary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
