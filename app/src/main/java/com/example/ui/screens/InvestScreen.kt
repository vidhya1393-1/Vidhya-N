package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
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
import com.example.data.model.IpoItem
import com.example.data.model.IpoStatus
import com.example.data.model.MutualFundItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradePulseViewModel
import java.util.Locale

@Composable
fun InvestScreen(
    viewModel: TradePulseViewModel,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0 = IPOs, 1 = Mutual Funds

    val ipos by viewModel.ipos.collectAsState()
    val mutualFunds by viewModel.mutualFunds.collectAsState()
    val ipoFilter by viewModel.ipoFilterStatus.collectAsState()
    val fundFilter by viewModel.fundCategoryFilter.collectAsState()
    val sipState by viewModel.sipState.collectAsState()

    val selectedIpo by viewModel.selectedIpo.collectAsState()
    val selectedFund by viewModel.selectedFund.collectAsState()

    var showSipCalculator by remember { mutableStateOf(false) }

    val filteredIpos = remember(ipos, ipoFilter) {
        if (ipoFilter == null) ipos else ipos.filter { it.status == ipoFilter }
    }

    val filteredFunds = remember(mutualFunds, fundFilter) {
        if (fundFilter == "All") mutualFunds else mutualFunds.filter { it.category.contains(fundFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Primary Tab Switcher: IPOs vs Mutual Funds
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeSubTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeSubTab = 0 }
                        .padding(vertical = 10.dp)
                        .testTag("tab_ipos"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IPO Central",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (activeSubTab == 0) DarkBackground else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeSubTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeSubTab = 1 }
                        .padding(vertical = 10.dp)
                        .testTag("tab_mutual_funds"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mutual Funds & SIP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (activeSubTab == 1) DarkBackground else TextSecondary
                    )
                }
            }
        }

        if (activeSubTab == 0) {
            // IPO Section
            item {
                // Filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterPill(
                            label = "All IPOs",
                            isSelected = ipoFilter == null,
                            onClick = { viewModel.setIpoFilter(null) }
                        )
                    }
                    items(IpoStatus.values()) { status ->
                        FilterPill(
                            label = status.label,
                            isSelected = ipoFilter == status,
                            onClick = { viewModel.setIpoFilter(status) }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active & Upcoming IPOs (${filteredIpos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text("GMP Live", fontSize = 11.sp, color = AmberGold, fontWeight = FontWeight.Bold)
                }
            }

            items(filteredIpos, key = { it.symbol }) { ipo ->
                IpoCard(
                    ipo = ipo,
                    onClick = { viewModel.openIpoDetail(ipo) }
                )
            }
        } else {
            // Mutual Funds Section
            item {
                // SIP Calculator Launcher Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .clickable { showSipCalculator = !showSipCalculator }
                        .testTag("sip_calculator_card")
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Interactive SIP Planner", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Calculate wealth compounding over time", fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            Text(
                                text = if (showSipCalculator) "Hide" else "Open",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Expandable SIP Calculator
                        AnimatedVisibility(visible = showSipCalculator) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                var monthlySlider by remember { mutableStateOf(sipState.monthlyInvestment.toFloat()) }
                                var rateSlider by remember { mutableStateOf(sipState.returnRatePercent.toFloat()) }
                                var yearsSlider by remember { mutableStateOf(sipState.years.toFloat()) }

                                Text("Monthly Investment: ₹${monthlySlider.toInt()}", fontSize = 12.sp, color = TextSecondary)
                                Slider(
                                    value = monthlySlider,
                                    onValueChange = {
                                        monthlySlider = it
                                        viewModel.updateSipCalculator(monthlySlider.toDouble(), rateSlider.toDouble(), yearsSlider.toInt())
                                    },
                                    valueRange = 1000f..50000f,
                                    steps = 49
                                )

                                Text("Expected Return: ${String.format(Locale.US, "%.1f", rateSlider)}% p.a.", fontSize = 12.sp, color = TextSecondary)
                                Slider(
                                    value = rateSlider,
                                    onValueChange = {
                                        rateSlider = it
                                        viewModel.updateSipCalculator(monthlySlider.toDouble(), rateSlider.toDouble(), yearsSlider.toInt())
                                    },
                                    valueRange = 8f..25f
                                )

                                Text("Time Horizon: ${yearsSlider.toInt()} Years", fontSize = 12.sp, color = TextSecondary)
                                Slider(
                                    value = yearsSlider,
                                    onValueChange = {
                                        yearsSlider = it
                                        viewModel.updateSipCalculator(monthlySlider.toDouble(), rateSlider.toDouble(), yearsSlider.toInt())
                                    },
                                    valueRange = 1f..30f,
                                    steps = 29
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Invested: ₹${String.format(Locale.US, "%,.0f", sipState.totalInvested)}", fontSize = 11.sp, color = TextSecondary)
                                            Text("Est. Gain: ₹${String.format(Locale.US, "%,.0f", sipState.estimatedReturns)}", fontSize = 11.sp, color = ProfitGreen, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Maturity Value", fontSize = 11.sp, color = TextSecondary)
                                            Text("₹${String.format(Locale.US, "%,.0f", sipState.totalFutureValue)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category filter chips
            item {
                val fundCategories = listOf("All", "Flexi Cap", "Large Cap", "Small Cap", "Mid Cap", "Index", "ELSS")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(fundCategories) { cat ->
                        FilterPill(
                            label = cat,
                            isSelected = fundFilter == cat,
                            onClick = { viewModel.setFundCategoryFilter(cat) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Top Performing Funds (${filteredFunds.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(filteredFunds, key = { it.id }) { fund ->
                MutualFundCard(
                    fund = fund,
                    onClick = { viewModel.openFundDetail(fund) }
                )
            }
        }
    }

    // IPO Detail Dialog
    selectedIpo?.let { ipo ->
        AlertDialog(
            onDismissRequest = { viewModel.closeIpoDetail() },
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(ipo.companyName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    Text("${ipo.sector} • ${ipo.status.label}", fontSize = 12.sp, color = TextSecondary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(ipo.about, fontSize = 12.5.sp, color = TextPrimary)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Price Band:", color = TextSecondary, fontSize = 12.sp)
                        Text(ipo.priceBand, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Lot Size:", color = TextSecondary, fontSize = 12.sp)
                        Text("${ipo.lotSize} Shares", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Issue Size:", color = TextSecondary, fontSize = 12.sp)
                        Text(ipo.issueSize, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subscription Multiple:", color = TextSecondary, fontSize = 12.sp)
                        Text("${ipo.totalSubscription}x (Retail: ${ipo.retailSubscription}x)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ProfitGreen)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grey Market Premium (GMP):", color = TextSecondary, fontSize = 12.sp)
                        Text("+₹${ipo.gmpAmount} (+${ipo.gmpPercent}%)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AmberGold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dates (Open - Close):", color = TextSecondary, fontSize = 12.sp)
                        Text("${ipo.openDate} - ${ipo.closeDate}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.closeIpoDetail() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Close", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Mutual Fund Detail Dialog
    selectedFund?.let { fund ->
        AlertDialog(
            onDismissRequest = { viewModel.closeFundDetail() },
            title = {
                Column {
                    Text(fund.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${fund.amc} • ${fund.category}", fontSize = 12.sp, color = TextSecondary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current NAV:", color = TextSecondary, fontSize = 12.sp)
                        Text("₹${fund.nav}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("3-Year CAGR Returns:", color = TextSecondary, fontSize = 12.sp)
                        Text("${fund.returns3Y}%", fontWeight = FontWeight.Bold, color = ProfitGreen, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("5-Year CAGR Returns:", color = TextSecondary, fontSize = 12.sp)
                        Text("${fund.returns5Y}%", fontWeight = FontWeight.Bold, color = ProfitGreen, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("AUM (Fund Size):", color = TextSecondary, fontSize = 12.sp)
                        Text(fund.aum, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Expense Ratio:", color = TextSecondary, fontSize = 12.sp)
                        Text("${fund.expenseRatio}%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fund Manager:", color = TextSecondary, fontSize = 12.sp)
                        Text(fund.fundManager, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Riskometer:", color = TextSecondary, fontSize = 12.sp)
                        Text(fund.riskometer, fontWeight = FontWeight.Bold, color = AmberGold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.closeFundDetail() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Done", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun IpoCard(
    ipo: IpoItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .testTag("ipo_card_${ipo.symbol}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ipo.companyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "${ipo.sector} • ${ipo.issueSize}", fontSize = 11.5.sp, color = TextSecondary)
                }

                Surface(
                    color = when (ipo.status) {
                        IpoStatus.OPEN -> ProfitGreenBg
                        IpoStatus.UPCOMING -> ElectricBlueBg
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = ipo.status.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (ipo.status) {
                            IpoStatus.OPEN -> ProfitGreen
                            IpoStatus.UPCOMING -> ElectricBlue
                            else -> TextSecondary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Price Band", fontSize = 11.sp, color = TextSecondary)
                    Text(ipo.priceBand, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lot Size", fontSize = 11.sp, color = TextSecondary)
                    Text("${ipo.lotSize} shares", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Est. GMP", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "+₹${ipo.gmpAmount} (+${ipo.gmpPercent}%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                }
            }
        }
    }
}

@Composable
fun MutualFundCard(
    fund: MutualFundItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .testTag("fund_card_${fund.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fund.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "${fund.amc} • ${fund.category}", fontSize = 11.5.sp, color = TextSecondary)
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "NAV: ₹${fund.nav}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("1Y Return", fontSize = 11.sp, color = TextSecondary)
                    Text("${fund.returns1Y}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("3Y CAGR", fontSize = 11.sp, color = TextSecondary)
                    Text("${fund.returns3Y}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("5Y CAGR", fontSize = 11.sp, color = TextSecondary)
                    Text("${fund.returns5Y}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                }
            }
        }
    }
}
