package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.RealTimeAlert
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.NavTab
import com.example.ui.viewmodel.TradePulseViewModel
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TradePulseTheme {
                val viewModel: TradePulseViewModel = viewModel()
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: TradePulseViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedStock by viewModel.selectedStock.collectAsState()
    val orderSuccessMsg by viewModel.orderSuccessMessage.collectAsState()
    val isSimulationLive by viewModel.isLiveSimulationEnabled.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val showAlertsModal by viewModel.showAlertsModal.collectAsState()

    val activeAlertsCount = remember(alerts) { alerts.count { !it.isTriggered } }

    var latestAlertPopup by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen to real-time price alert trigger events from the simulation engine
    LaunchedEffect(Unit) {
        viewModel.triggeredAlertNotification.collect { notificationText ->
            latestAlertPopup = notificationText
            snackbarHostState.showSnackbar(
                message = notificationText,
                duration = SnackbarDuration.Long
            )
            delay(5000)
            latestAlertPopup = null
        }
    }

    LaunchedEffect(orderSuccessMsg) {
        orderSuccessMsg?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearOrderSuccessMessage()
        }
    }

    // Detail screens handle their own Scaffold
    if (selectedStock != null) {
        StockDetailScreen(
            stock = selectedStock!!,
            viewModel = viewModel
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ProfitGreenBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = "TradePulse Logo",
                                tint = ProfitGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TradePulse",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isSimulationLive) ProfitGreen else TextTertiary)
                                )
                            }
                            Text(
                                text = "Real-Time Markets & Portfolio",
                                fontSize = 10.5.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Real-Time Alerts Center Button with Badge
                    IconButton(
                        onClick = { viewModel.setShowAlertsModal(true) },
                        modifier = Modifier.testTag("open_alerts_center_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (activeAlertsCount > 0) {
                                    Badge(
                                        containerColor = AmberGold,
                                        contentColor = DarkBackground
                                    ) {
                                        Text("$activeAlertsCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = "Price Alerts",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Market Live Heartbeat Status
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (isSimulationLive) "LIVE" else "PAUSED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSimulationLive) ProfitGreen else TextTertiary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationItem(
                    selected = currentTab == NavTab.MARKETS,
                    onClick = { viewModel.selectTab(NavTab.MARKETS) },
                    label = "Markets",
                    icon = Icons.Outlined.BarChart,
                    selectedIcon = Icons.Filled.BarChart,
                    testTag = "nav_tab_markets"
                )
                NavigationItem(
                    selected = currentTab == NavTab.STOCKS,
                    onClick = { viewModel.selectTab(NavTab.STOCKS) },
                    label = "Stocks",
                    icon = Icons.AutoMirrored.Outlined.TrendingUp,
                    selectedIcon = Icons.AutoMirrored.Filled.TrendingUp,
                    testTag = "nav_tab_stocks"
                )
                NavigationItem(
                    selected = currentTab == NavTab.PORTFOLIO,
                    onClick = { viewModel.selectTab(NavTab.PORTFOLIO) },
                    label = "Portfolio",
                    icon = Icons.Outlined.AccountBalanceWallet,
                    selectedIcon = Icons.Filled.AccountBalanceWallet,
                    testTag = "nav_tab_portfolio"
                )
                NavigationItem(
                    selected = currentTab == NavTab.INVEST,
                    onClick = { viewModel.selectTab(NavTab.INVEST) },
                    label = "IPOs & Funds",
                    icon = Icons.Outlined.RocketLaunch,
                    selectedIcon = Icons.Filled.RocketLaunch,
                    testTag = "nav_tab_invest"
                )
                NavigationItem(
                    selected = currentTab == NavTab.GUIDE,
                    onClick = { viewModel.selectTab(NavTab.GUIDE) },
                    label = "Learn",
                    icon = Icons.Outlined.School,
                    selectedIcon = Icons.Filled.School,
                    testTag = "nav_tab_guide"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.MARKETS -> MarketsScreen(viewModel = viewModel)
                NavTab.STOCKS -> StocksScreen(viewModel = viewModel)
                NavTab.PORTFOLIO -> PortfolioScreen(viewModel = viewModel)
                NavTab.INVEST -> InvestScreen(viewModel = viewModel)
                NavTab.GUIDE -> GuideScreen(viewModel = viewModel)
            }

            // Real-Time Alert Floating Popup Banner at Top
            AnimatedVisibility(
                visible = latestAlertPopup != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                latestAlertPopup?.let { text ->
                    Surface(
                        color = AmberGoldBg,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, AmberGold, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AmberGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { latestAlertPopup = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Real-Time Price Alerts Center Modal
    if (showAlertsModal) {
        AlertsCenterBottomSheet(
            alerts = alerts,
            onDismiss = { viewModel.setShowAlertsModal(false) },
            onDeleteAlert = { viewModel.deleteAlert(it) },
            onCreateAlert = { sym, target, cond, note ->
                viewModel.createAlert(sym, target, cond, target, note)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsCenterBottomSheet(
    alerts: List<RealTimeAlert>,
    onDismiss: () -> Unit,
    onDeleteAlert: (Long) -> Unit,
    onCreateAlert: (symbol: String, targetPrice: Double, condition: String, note: String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AmberGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-Time Price Alerts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("create_new_alert_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Alert", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Text(
                text = "Get notified instantly when stock prices cross your trigger thresholds",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (alerts.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No price alerts set", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Tap 'New Alert' or open any stock to set alerts", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(alerts, key = { it.id }) { alert ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (alert.isTriggered) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (alert.isTriggered) AmberGold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(14.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = alert.symbol,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = if (alert.isTriggered) AmberGoldBg else if (alert.condition == "ABOVE") ProfitGreenBg else LossRedBg,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (alert.isTriggered) "TRIGGERED" else if (alert.condition == "ABOVE") "CROSSES ABOVE" else "DROPS BELOW",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (alert.isTriggered) AmberGold else if (alert.condition == "ABOVE") ProfitGreen else LossRed,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Target Price: ₹${String.format(Locale.US, "%,.2f", alert.targetPrice)}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (alert.note.isNotBlank()) {
                                        Text(text = alert.note, fontSize = 11.sp, color = TextSecondary)
                                    }

                                    if (alert.isTriggered) {
                                        Text(
                                            text = "Triggered at ${alert.triggeredDate ?: "Session"}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AmberGold
                                        )
                                    }
                                }

                                IconButton(onClick = { onDeleteAlert(alert.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Alert", tint = TextTertiary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCustomAlertModal(
            onDismiss = { showCreateDialog = false },
            onCreate = { sym, target, cond, note ->
                onCreateAlert(sym, target, cond, note)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateCustomAlertModal(
    onDismiss: () -> Unit,
    onCreate: (symbol: String, targetPrice: Double, condition: String, note: String) -> Unit
) {
    var symbol by remember { mutableStateOf("RELIANCE") }
    var targetText by remember { mutableStateOf("2960.00") }
    var condition by remember { mutableStateOf("ABOVE") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Price Alert", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it.uppercase() },
                    label = { Text("Stock Symbol (e.g. INFY, TCS)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("modal_alert_symbol_input")
                )

                // Condition Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (condition == "ABOVE") ProfitGreen else Color.Transparent)
                            .clickable { condition = "ABOVE" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Above (≥)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (condition == "ABOVE") DarkBackground else TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (condition == "BELOW") LossRed else Color.Transparent)
                            .clickable { condition = "BELOW" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Below (≤)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (condition == "BELOW") Color.White else TextSecondary)
                    }
                }

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("modal_alert_price_input")
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (e.g. Breakout)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = targetText.toDoubleOrNull()
                    if (symbol.isNotBlank() && p != null && p > 0) {
                        onCreate(symbol, p, condition, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
            ) {
                Text("Set Alert", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun RowScope.NavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    testTag: String
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) selectedIcon else icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
        ),
        modifier = Modifier.testTag(testTag)
    )
}
