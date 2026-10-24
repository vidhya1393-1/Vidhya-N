package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketIndex
import com.example.data.model.StockQuote
import com.example.ui.components.PriceChangeBadge
import com.example.ui.components.Sparkline
import com.example.ui.components.StockCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradePulseViewModel
import java.util.Locale

@Composable
fun MarketsScreen(
    viewModel: TradePulseViewModel,
    modifier: Modifier = Modifier
) {
    val indices by viewModel.indices.collectAsState()
    val stocks by viewModel.stocks.collectAsState()
    val sentiment by viewModel.marketSentiment.collectAsState()
    val isSimulationLive by viewModel.isLiveSimulationEnabled.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedIndexCategory by remember { mutableStateOf("All Indices") }
    var indicesViewMode by remember { mutableStateOf("Grid") } // "Grid" or "List"
    var selectedMoverTab by remember { mutableStateOf(0) } // 0 = Gainers, 1 = Losers

    val indexCategories = listOf("All Indices", "India Benchmark", "Sectoral", "US & Global")

    val quickSymbolSuggestions = listOf("RELIANCE", "TCS", "INFY", "HDFCBANK", "NVDA", "AAPL", "ZOMATO")

    // Filtered search results for stock symbols
    val searchStockResults = remember(searchQuery, stocks) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase()
            stocks.filter { stock ->
                stock.symbol.lowercase().contains(q) ||
                stock.name.lowercase().contains(q) ||
                stock.sector.lowercase().contains(q)
            }
        }
    }

    // Filtered indices list
    val filteredIndices = remember(selectedIndexCategory, indices) {
        if (selectedIndexCategory == "All Indices") {
            indices
        } else {
            indices.filter { it.category == selectedIndexCategory }
        }
    }

    val gainers = remember(stocks) {
        stocks.sortedByDescending { it.dayChangePercent }.take(5)
    }
    val losers = remember(stocks) {
        stocks.sortedBy { it.dayChangePercent }.take(5)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
    ) {
        // 1. Stock Symbol Search Input Bar
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search stock symbol (e.g. INFY, TCS, NVDA)...",
                            fontSize = 13.5.sp,
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search stock symbols",
                            tint = if (searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary else TextSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("clear_dashboard_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_stock_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick symbol suggestion chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(quickSymbolSuggestions) { symbol ->
                        val isCurrentQuery = searchQuery.equals(symbol, ignoreCase = true)
                        Surface(
                            color = if (isCurrentQuery) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clickable {
                                    searchQuery = if (isCurrentQuery) "" else symbol
                                }
                                .testTag("quick_symbol_chip_$symbol")
                        ) {
                            Text(
                                text = symbol,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentQuery) DarkBackground else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Real-Time Stock Search Results Section (Shown when user types a symbol)
        if (searchQuery.isNotBlank()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock Results for \"$searchQuery\" (${searchStockResults.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { searchQuery = "" }) {
                        Text("Clear", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
            }

            if (searchStockResults.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No stock found matching \"$searchQuery\"",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching RELIANCE, TCS, INFY, HDFCBANK, TATAMOTORS, NVDA, AAPL",
                                color = TextTertiary,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            } else {
                items(searchStockResults, key = { it.symbol }) { stock ->
                    StockCard(
                        stock = stock,
                        onClick = { viewModel.openStockDetail(stock) },
                        onWatchlistToggle = { viewModel.toggleWatchlist(stock.symbol) }
                    )
                }
            }
        }

        // 3. Market Session & Pulse Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isSimulationLive) ProfitGreen else TextTertiary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSimulationLive) "LIVE TRADING SESSION" else "MARKET PAUSED",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSimulationLive) ProfitGreen else TextSecondary,
                                letterSpacing = 1.1.sp
                            )
                        }

                        // Simulation toggle button
                        FilledTonalButton(
                            onClick = { viewModel.toggleLiveSimulation() },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSimulationLive) ProfitGreenBg else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("toggle_market_pulse_btn")
                        ) {
                            Text(
                                text = if (isSimulationLive) "Pulse: ON" else "Pulse: OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSimulationLive) ProfitGreen else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sentiment & Market Breadth Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Market Sentiment", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${sentiment.sentiment} (${sentiment.fearGreedIndex}/100)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Market Breadth (Adv/Dec)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${sentiment.advanceCount} Adv",
                                    color = ProfitGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(text = " : ", color = TextSecondary)
                                Text(
                                    text = "${sentiment.declineCount} Dec",
                                    color = LossRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Market Indices Section (Core Focus of Dashboard)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Market Indices",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${filteredIndices.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // View Mode Switcher (Grid vs List)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (indicesViewMode == "Grid") MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { indicesViewMode = "Grid" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("indices_view_mode_grid")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Grid View",
                                tint = if (indicesViewMode == "Grid") DarkBackground else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (indicesViewMode == "List") MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { indicesViewMode = "List" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("indices_view_mode_list")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewAgenda,
                                contentDescription = "List View",
                                tint = if (indicesViewMode == "List") DarkBackground else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Indices Category Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(indexCategories) { cat ->
                        val isSelected = selectedIndexCategory == cat
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedIndexCategory = cat }
                                .testTag("index_filter_$cat")
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Display Indices: Grid / Carousel vs Detailed Vertical List
        if (indicesViewMode == "Grid") {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(filteredIndices, key = { it.name }) { index ->
                        IndexCard(index = index)
                    }
                }
            }
        } else {
            // Detailed List Mode
            items(filteredIndices, key = { it.name }) { index ->
                DetailedIndexListItem(index = index)
            }
        }

        // 5. Sector Performance Heatmap
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
                        text = "Sector Performance",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectorChip(name = "IT & Tech", change = 1.82, modifier = Modifier.weight(1f))
                        SectorChip(name = "Banking", change = 1.15, modifier = Modifier.weight(1f))
                        SectorChip(name = "Auto", change = 1.60, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectorChip(name = "Energy", change = 1.19, modifier = Modifier.weight(1f))
                        SectorChip(name = "Pharma", change = 0.42, modifier = Modifier.weight(1f))
                        SectorChip(name = "FMCG", change = -0.38, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // 6. Market Movers: Top Gainers vs Losers Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Market Movers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp)
                ) {
                    TabPill(
                        text = "Top Gainers",
                        isSelected = selectedMoverTab == 0,
                        onClick = { selectedMoverTab = 0 }
                    )
                    TabPill(
                        text = "Top Losers",
                        isSelected = selectedMoverTab == 1,
                        onClick = { selectedMoverTab = 1 }
                    )
                }
            }
        }

        val displayMovers = if (selectedMoverTab == 0) gainers else losers
        items(displayMovers, key = { it.symbol }) { stock ->
            StockCard(
                stock = stock,
                onClick = { viewModel.openStockDetail(stock) },
                onWatchlistToggle = { viewModel.toggleWatchlist(stock.symbol) }
            )
        }
    }
}

@Composable
fun IndexCard(index: MarketIndex) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(185.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .testTag("index_card_${index.name.replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = index.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = index.region,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = String.format(Locale.US, "%,.2f", index.value),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            PriceChangeBadge(
                change = index.change,
                changePercent = index.changePercent,
                prefix = ""
            )

            Spacer(modifier = Modifier.height(10.dp))

            Sparkline(
                points = index.sparkline,
                isPositive = index.isPositive,
                modifier = Modifier.fillMaxWidth(),
                height = 36.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "L: ${String.format(Locale.US, "%,.0f", index.dayLow)}",
                    fontSize = 10.5.sp,
                    color = TextTertiary
                )
                Text(
                    text = "H: ${String.format(Locale.US, "%,.0f", index.dayHigh)}",
                    fontSize = 10.5.sp,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
fun DetailedIndexListItem(index: MarketIndex) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .testTag("index_list_item_${index.name.replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = index.name,
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
                                text = index.region,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(text = index.category, fontSize = 11.sp, color = TextTertiary)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "%,.2f", index.value),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    PriceChangeBadge(
                        change = index.change,
                        changePercent = index.changePercent,
                        prefix = ""
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sparkline & Day Range Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Day Low: ${String.format(Locale.US, "%,.1f", index.dayLow)}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Day High: ${String.format(Locale.US, "%,.1f", index.dayHigh)}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val range = (index.dayHigh - index.dayLow).coerceAtLeast(1.0)
                    val fraction = ((index.value - index.dayLow) / range).toFloat().coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .background(if (index.isPositive) ProfitGreen else LossRed)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(modifier = Modifier.width(80.dp)) {
                    Sparkline(
                        points = index.sparkline,
                        isPositive = index.isPositive,
                        modifier = Modifier.fillMaxWidth(),
                        height = 30.dp
                    )
                }
            }
        }
    }
}

@Composable
fun SectorChip(
    name: String,
    change: Double,
    modifier: Modifier = Modifier
) {
    val isPos = change >= 0
    val color = if (isPos) ProfitGreen else LossRed
    val bg = if (isPos) ProfitGreenBg else LossRedBg

    Surface(
        color = bg,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = name, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            val sign = if (isPos) "+" else ""
            Text(
                text = "$sign${String.format(Locale.US, "%.2f", change)}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun TabPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextSecondary
        )
    }
}
