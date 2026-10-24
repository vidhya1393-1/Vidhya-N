package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetType
import com.example.data.model.StockQuote
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun Sparkline(
    points: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 38.dp,
    strokeWidth: Float = 4f,
    showGradientFill: Boolean = true
) {
    if (points.isEmpty()) return

    val lineColor = if (isPositive) ProfitGreen else LossRed
    val minVal = points.minOrNull() ?: 0.0
    val maxVal = points.maxOrNull() ?: 1.0
    val range = if (maxVal == minVal) 1.0 else maxVal - minVal

    Canvas(modifier = modifier.height(height)) {
        val width = size.width
        val canvasHeight = size.height
        val stepX = width / (points.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { i, value ->
            val x = i * stepX
            val normalizedY = ((value - minVal) / range).toFloat()
            val y = canvasHeight - (normalizedY * (canvasHeight - 8f)) - 4f

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, canvasHeight)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        if (showGradientFill) {
            fillPath.lineTo(width, canvasHeight)
            fillPath.close()
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.28f),
                        lineColor.copy(alpha = 0.0f)
                    )
                )
            )
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun PriceChangeBadge(
    change: Double,
    changePercent: Double,
    modifier: Modifier = Modifier,
    prefix: String = "₹",
    isLarge: Boolean = false
) {
    val isPos = change >= 0
    val color = if (isPos) ProfitGreen else LossRed
    val bgColor = if (isPos) ProfitGreenBg else LossRedBg

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (isLarge) 10.dp else 7.dp, vertical = if (isLarge) 5.dp else 3.dp)
        ) {
            Icon(
                imageVector = if (isPos) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(if (isLarge) 16.dp else 13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            val sign = if (isPos) "+" else ""
            Text(
                text = "$sign${String.format(Locale.US, "%.2f", changePercent)}%",
                color = color,
                fontSize = if (isLarge) 13.sp else 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StockCard(
    stock: StockQuote,
    onClick: () -> Unit,
    onWatchlistToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .testTag("stock_item_${stock.symbol}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Symbol, Name, Sector
            Column(modifier = Modifier.weight(1.3f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stock.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = stock.exchange,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stock.sector,
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• RSI: ${String.format(Locale.US, "%.0f", stock.rsi)}",
                        fontSize = 11.sp,
                        color = if (stock.rsi > 70) AmberGold else if (stock.rsi < 30) ElectricBlue else TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Center: Mini Sparkline
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .padding(horizontal = 8.dp)
            ) {
                Sparkline(
                    points = stock.sparkline,
                    isPositive = stock.isPositive,
                    modifier = Modifier.fillMaxWidth(),
                    height = 34.dp
                )
            }

            // Right: Price, Change & Watchlist Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1.1f)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    val curr = if (stock.exchange == "NASDAQ") "$" else "₹"
                    Text(
                        text = "$curr${String.format(Locale.US, "%,.2f", stock.currentPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    PriceChangeBadge(
                        change = stock.dayChange,
                        changePercent = stock.dayChangePercent,
                        prefix = curr
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onWatchlistToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("watchlist_btn_${stock.symbol}")
                ) {
                    Icon(
                        imageVector = if (stock.isWatchlisted) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Watchlist",
                        tint = if (stock.isWatchlisted) AmberGold else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AssetAllocationBar(
    breakdown: Map<AssetType, Double>,
    modifier: Modifier = Modifier
) {
    val total = breakdown.values.sum().coerceAtLeast(1.0)

    val colorMap = mapOf(
        AssetType.STOCKS to ElectricBlue,
        AssetType.MUTUAL_FUNDS to ProfitGreen,
        AssetType.IPOS to AmberGold,
        AssetType.GOLD_BONDS to PurpleAccent
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Multi-segment horizontal bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(DarkSurfaceVariant)
        ) {
            AssetType.values().forEach { type ->
                val amount = breakdown[type] ?: 0.0
                val fraction = (amount / total).toFloat().coerceIn(0f, 1f)
                if (fraction > 0.01f) {
                    Box(
                        modifier = Modifier
                            .weight(fraction)
                            .fillMaxHeight()
                            .background(colorMap[type] ?: ElectricBlue)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AssetType.values().forEach { type ->
                val amount = breakdown[type] ?: 0.0
                val pct = (amount / total) * 100
                if (pct > 0.5) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colorMap[type] ?: ElectricBlue)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${type.label}: ${String.format(Locale.US, "%.0f", pct)}%",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
