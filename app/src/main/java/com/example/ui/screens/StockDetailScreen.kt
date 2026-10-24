package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CandleStick
import com.example.data.model.FoDetails
import com.example.data.model.StockQuote
import com.example.data.model.TechnicalIndicators
import com.example.ui.components.AdvancedTradingChart
import com.example.ui.components.PriceChangeBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradePulseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    stock: StockQuote,
    viewModel: TradePulseViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.closeStockDetail()
    }

    val watchlistSymbols by viewModel.watchlistSymbols.collectAsState()
    val isWatchlisted = watchlistSymbols.contains(stock.symbol)
    val showOrderSheet by viewModel.showOrderSheet.collectAsState()

    var selectedInterval by remember { mutableStateOf("1D") }
    val intervals = listOf("1D", "1W", "1M", "1Y", "5Y")

    // Active Tab in Stock Detail: 0 = Technicals & Chart, 1 = F&O Details, 2 = Fundamentals
    var detailTab by remember { mutableStateOf(0) }

    // Alert Creation Modal State
    var showSetAlertSheet by remember { mutableStateOf(false) }

    // Dynamic Candlestick generation based on interval
    val candles = remember(stock.currentPrice, selectedInterval) {
        viewModel.getCandlesForStock(stock, selectedInterval)
    }

    // Technical Indicators
    val technicals = remember(stock.currentPrice) {
        viewModel.getTechnicalIndicators(stock)
    }

    // F&O Details
    val foDetails = remember(stock.currentPrice) {
        viewModel.getFoDetails(stock)
    }

    var isBuyAction by remember { mutableStateOf(true) }
    var orderQuantityText by remember { mutableStateOf("10") }
    var orderType by remember { mutableStateOf("MARKET") } // MARKET or LIMIT

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
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
                                    fontSize = 10.sp,
                                    color = TextSecondary,
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
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.closeStockDetail() },
                        modifier = Modifier.testTag("stock_detail_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    // Set Real-Time Alert Button
                    IconButton(
                        onClick = { showSetAlertSheet = true },
                        modifier = Modifier.testTag("stock_detail_set_alert_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "Set Price Alert",
                            tint = AmberGold
                        )
                    }

                    // Watchlist Toggle
                    IconButton(
                        onClick = { viewModel.toggleWatchlist(stock.symbol) },
                        modifier = Modifier.testTag("detail_watchlist_toggle")
                    ) {
                        Icon(
                            imageVector = if (isWatchlisted) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Watchlist",
                            tint = if (isWatchlisted) AmberGold else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Trading & Alert Action Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { showSetAlertSheet = true },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = AmberGoldBg),
                        modifier = Modifier.testTag("set_alert_bottom_btn")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Alert", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            isBuyAction = true
                            viewModel.setShowOrderSheet(true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("paper_buy_btn")
                    ) {
                        Text("Paper Buy", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    }

                    Button(
                        onClick = {
                            isBuyAction = false
                            viewModel.setShowOrderSheet(true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("paper_sell_btn")
                    ) {
                        Text("Paper Sell", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            // Price & Day Change Banner
            item {
                val curr = if (stock.exchange == "NASDAQ") "$" else "₹"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$curr${String.format(Locale.US, "%,.2f", stock.currentPrice)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PriceChangeBadge(
                                change = stock.dayChange,
                                changePercent = stock.dayChangePercent,
                                prefix = curr,
                                isLarge = true
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "LTP (Real-time)",
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                        }
                    }

                    // Institutional Signal Badge
                    Surface(
                        color = when (technicals.overallSignal) {
                            "Strong Buy" -> ProfitGreenBg
                            "Buy" -> ProfitGreenBg
                            else -> AmberGoldBg
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = technicals.overallSignal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (technicals.overallSignal) {
                                    "Strong Buy" -> ProfitGreen
                                    "Buy" -> ProfitGreen
                                    else -> AmberGold
                                }
                            )
                            Text(
                                text = "${technicals.bullishCount} Bull / ${technicals.bearishCount} Bear",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Interactive Candlestick Chart View
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Timeframe Selector Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        intervals.forEach { interval ->
                            val isSelected = selectedInterval == interval
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                    .clickable { selectedInterval = interval }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = interval,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Advanced Candlestick Chart with Overlays & Volume
                    AdvancedTradingChart(
                        candles = candles,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Sub-Navigation Tabs: [Technicals & Indicators, F&O Details, Fundamentals]
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp)
                ) {
                    DetailSubTab(
                        title = "Technicals",
                        isSelected = detailTab == 0,
                        onClick = { detailTab = 0 },
                        modifier = Modifier.weight(1f),
                        testTag = "subtab_technicals"
                    )
                    DetailSubTab(
                        title = "F&O (Options)",
                        isSelected = detailTab == 1,
                        onClick = { detailTab = 1 },
                        modifier = Modifier.weight(1f),
                        testTag = "subtab_fo"
                    )
                    DetailSubTab(
                        title = "Fundamentals",
                        isSelected = detailTab == 2,
                        onClick = { detailTab = 2 },
                        modifier = Modifier.weight(1f),
                        testTag = "subtab_fundamentals"
                    )
                }
            }

            // TAB 0: MOST USED INSTITUTIONAL INDICATORS
            if (detailTab == 0) {
                // Moving Averages & Crosses
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Moving Averages & Crosses",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    color = ProfitGreenBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Golden Cross Active",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ProfitGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                IndicatorStat("EMA (20)", "₹${technicals.ema20}", "Bullish", ProfitGreen)
                                IndicatorStat("SMA (50 DMA)", "₹${technicals.dma50}", "Bullish", ProfitGreen)
                                IndicatorStat("SMA (200 DMA)", "₹${technicals.dma200}", "Strong Bull", ProfitGreen)
                            }
                        }
                    }
                }

                // Momentum & Oscillators: RSI + MACD
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Momentum Oscillators (RSI & MACD)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // RSI Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("RSI (14 Period)", fontSize = 12.sp, color = TextSecondary)
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", technicals.rsi)} (Momentum)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (technicals.rsi > 70) AmberGold else if (technicals.rsi < 30) ElectricBlue else ProfitGreen
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (technicals.rsi > 70) "Overbought" else if (technicals.rsi < 30) "Oversold" else "Healthy Uptrend",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // MACD Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("MACD (12, 26, 9)", fontSize = 12.sp, color = TextSecondary)
                                    Text(
                                        text = "Line: ${technicals.macdLine} | Signal: ${technicals.signalLine}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Histogram", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        text = "+${technicals.macdHist} (Bullish)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ProfitGreen
                                    )
                                }
                            }
                        }
                    }
                }

                // Volatility & Pivot Levels: Bollinger Bands, VWAP & Pivot S/R
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Institutional Volatility & Pivot Support/Resistance",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("VWAP", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${technicals.vwap}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                                }
                                Column {
                                    Text("BB Upper", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${technicals.bollingerUpper}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column {
                                    Text("BB Lower", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${technicals.bollingerLower}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Pivot", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${technicals.pivot}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                }
                                Column {
                                    Text("R1 / R2", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${technicals.r1} / ₹${technicals.r2}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LossRed)
                                }
                                Column {
                                    Text("S1 / S2", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${technicals.s1} / ₹${technicals.s2}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ProfitGreen)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: F&O (FUTURES & OPTIONS CHAIN)
            if (detailTab == 1) {
                // Futures & PCR Header
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Current Month Futures", fontSize = 12.sp, color = TextSecondary)
                                    Text("₹${foDetails.futurePrice}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Basis: +₹${foDetails.basis} (Premium)", fontSize = 11.sp, color = ProfitGreen)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Lot Size", fontSize = 12.sp, color = TextSecondary)
                                    Text("${foDetails.lotSize} shares", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Rollover: ${foDetails.rolloverPercent}%", fontSize = 11.sp, color = TextTertiary)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Put-Call Ratio (PCR)", fontSize = 11.sp, color = TextSecondary)
                                    Text("${foDetails.pcr}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                                    Text(foDetails.pcrInterpretation, fontSize = 10.5.sp, color = TextSecondary)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Max Pain Strike", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${foDetails.maxPain}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                                }
                            }
                        }
                    }
                }

                // Option Chain Table Header
                item {
                    Text(
                        text = "Option Chain (Calls vs Puts)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CALL LTP (OI)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = ProfitGreen, modifier = Modifier.weight(1.2f))
                            Text("STRIKE", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(0.8f))
                            Text("PUT LTP (OI)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = LossRed, modifier = Modifier.weight(1.2f))
                        }
                    }
                }

                // Option Chain Strike Rows
                items(foDetails.strikes) { strike ->
                    Surface(
                        color = if (strike.isAtm) AmberGoldBg else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (strike.isAtm) AmberGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Call Side
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = "₹${strike.callLtp}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.1fM", strike.callOi / 1000000.0)} OI • IV ${strike.callIv}%",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }

                            // Strike (Center)
                            Box(
                                modifier = Modifier.weight(0.8f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${strike.strikePrice.toInt()}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (strike.isAtm) AmberGold else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (strike.isAtm) {
                                        Text("ATM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                                    }
                                }
                            }

                            // Put Side
                            Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${strike.putLtp}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.1fM", strike.putOi / 1000000.0)} OI • IV ${strike.putIv}%",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // TAB 2: FUNDAMENTALS & VALUATION
            if (detailTab == 2) {
                // 52-Week Range Bar
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("52-Week Price Range", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(10.dp))

                            val range = (stock.week52High - stock.week52Low).coerceAtLeast(1.0)
                            val fraction = ((stock.currentPrice - stock.week52Low) / range).toFloat().coerceIn(0f, 1f)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Low: ₹${stock.week52Low}", fontSize = 11.5.sp, color = TextSecondary)
                                Text("High: ₹${stock.week52High}", fontSize = 11.5.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .fillMaxHeight()
                                        .background(ElectricBlue)
                                    )
                            }
                        }
                    }
                }

                // Key Financial Metrics & Ratios Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            MetricTile("Market Cap", stock.marketCap, modifier = Modifier.weight(1f))
                            MetricTile("P/E Ratio", "${stock.peRatio}", modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            MetricTile("P/B Ratio", "${stock.pbRatio}", modifier = Modifier.weight(1f))
                            MetricTile("Div Yield", "${stock.dividendYield}%", modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            MetricTile("Day High", "₹${stock.high}", modifier = Modifier.weight(1f))
                            MetricTile("Day Low", "₹${stock.low}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Real-Time Price Alert Configuration Modal
    if (showSetAlertSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSetAlertSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            var alertCondition by remember { mutableStateOf("ABOVE") }
            var targetPriceText by remember {
                mutableStateOf(String.format(Locale.US, "%.2f", stock.currentPrice * 1.02))
            }
            var alertNote by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set Real-Time Price Alert",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AmberGold)
                }

                Text(
                    text = "${stock.symbol} • Current Price: ₹${stock.currentPrice}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Alert Condition Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (alertCondition == "ABOVE") ProfitGreen else Color.Transparent)
                            .clickable { alertCondition = "ABOVE" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Crosses Above (≥)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (alertCondition == "ABOVE") DarkBackground else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (alertCondition == "BELOW") LossRed else Color.Transparent)
                            .clickable { alertCondition = "BELOW" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Drops Below (≤)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (alertCondition == "BELOW") Color.White else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick percentage offsets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1.0, 2.0, 5.0, -1.0, -2.0).forEach { pct ->
                        val calc = stock.currentPrice * (1.0 + (pct / 100.0))
                        val sign = if (pct > 0) "+$pct%" else "$pct%"
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    targetPriceText = String.format(Locale.US, "%.2f", calc)
                                    alertCondition = if (pct >= 0) "ABOVE" else "BELOW"
                                }
                        ) {
                            Text(
                                text = sign,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pct >= 0) ProfitGreen else LossRed,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = targetPriceText,
                    onValueChange = { targetPriceText = it },
                    label = { Text("Target Alert Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("alert_target_price_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = alertNote,
                    onValueChange = { alertNote = it },
                    label = { Text("Alert Note (Optional, e.g. Breakout point)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("alert_note_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val target = targetPriceText.toDoubleOrNull()
                        if (target != null && target > 0) {
                            viewModel.createAlert(
                                symbol = stock.symbol,
                                targetPrice = target,
                                condition = alertCondition,
                                currentPrice = stock.currentPrice,
                                note = alertNote
                            )
                            showSetAlertSheet = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_create_alert_btn")
                ) {
                    Text(
                        text = "Set Real-Time Alert",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkBackground
                    )
                }
            }
        }
    }

    // Paper Trading Modal Bottom Sheet
    if (showOrderSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setShowOrderSheet(false) },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = if (isBuyAction) "Simulated Buy Order" else "Simulated Sell Order",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isBuyAction) ProfitGreen else LossRed
                )
                Text(
                    text = "${stock.symbol} • ${stock.name}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Order Type Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (orderType == "MARKET") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { orderType = "MARKET" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Market Order",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (orderType == "MARKET") DarkBackground else TextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (orderType == "LIMIT") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { orderType = "LIMIT" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Limit Order",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (orderType == "LIMIT") DarkBackground else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Input
                OutlinedTextField(
                    value = orderQuantityText,
                    onValueChange = { orderQuantityText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Quantity (Shares)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_quantity_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Total Estimated Amount
                val qty = orderQuantityText.toDoubleOrNull() ?: 0.0
                val estTotal = qty * stock.currentPrice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estimated Value:", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = "₹${String.format(Locale.US, "%,.2f", estTotal)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (qty > 0) {
                            viewModel.executePaperTrade(stock, isBuyAction, qty, orderType)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBuyAction) ProfitGreen else LossRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_order_btn")
                ) {
                    Text(
                        text = if (isBuyAction) "Confirm Paper Buy" else "Confirm Paper Sell",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isBuyAction) DarkBackground else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DetailSubTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) DarkBackground else TextSecondary
        )
    }
}

@Composable
fun IndicatorStat(
    label: String,
    value: String,
    status: String,
    statusColor: Color
) {
    Column {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = status, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
