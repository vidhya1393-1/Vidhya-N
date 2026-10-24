package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

enum class NavTab(val title: String) {
    MARKETS("Markets"),
    STOCKS("Stocks"),
    PORTFOLIO("Portfolio"),
    INVEST("IPOs & Funds"),
    GUIDE("Learn")
}

data class SipCalculationResult(
    val monthlyInvestment: Double = 10000.0,
    val returnRatePercent: Double = 13.5,
    val years: Int = 10,
    val totalInvested: Double = 1200000.0,
    val estimatedReturns: Double = 1384000.0,
    val totalFutureValue: Double = 2584000.0
)

class TradePulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PortfolioRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PortfolioRepository(database)
    }

    // Active Tab Navigation
    private val _currentTab = MutableStateFlow(NavTab.MARKETS)
    val currentTab = _currentTab.asStateFlow()

    // Navigation Stack for sub-screens / details
    private val _selectedStock = MutableStateFlow<StockQuote?>(null)
    val selectedStock = _selectedStock.asStateFlow()

    private val _selectedIpo = MutableStateFlow<IpoItem?>(null)
    val selectedIpo = _selectedIpo.asStateFlow()

    private val _selectedFund = MutableStateFlow<MutualFundItem?>(null)
    val selectedFund = _selectedFund.asStateFlow()

    private val _selectedGuideChapter = MutableStateFlow<GuideChapter?>(null)
    val selectedGuideChapter = _selectedGuideChapter.asStateFlow()

    // UI Dialogs
    private val _showAddHoldingDialog = MutableStateFlow(false)
    val showAddHoldingDialog = _showAddHoldingDialog.asStateFlow()

    private val _showOrderSheet = MutableStateFlow(false)
    val showOrderSheet = _showOrderSheet.asStateFlow()

    private val _orderSuccessMessage = MutableStateFlow<String?>(null)
    val orderSuccessMessage = _orderSuccessMessage.asStateFlow()

    // Live Repository Streams
    val stocks = repository.stocks
    val indices = repository.indices
    val ipos = repository.ipos
    val mutualFunds = repository.mutualFunds
    val marketSentiment = repository.marketSentiment
    val isLiveSimulationEnabled = repository.isLiveSimulationEnabled
    val holdings = repository.getPortfolioHoldings().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val transactions = repository.getTransactions().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val watchlistSymbols = repository.getWatchlist().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val alerts = repository.getAlerts().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val triggeredAlertNotification = repository.triggeredAlertNotification

    private val _showAlertsModal = MutableStateFlow(false)
    val showAlertsModal = _showAlertsModal.asStateFlow()

    private val _showCreateAlertDialog = MutableStateFlow(false)
    val showCreateAlertDialog = _showCreateAlertDialog.asStateFlow()

    // Stock Filters
    private val _stockSearchQuery = MutableStateFlow("")
    val stockSearchQuery = _stockSearchQuery.asStateFlow()

    private val _selectedSectorFilter = MutableStateFlow("All")
    val selectedSectorFilter = _selectedSectorFilter.asStateFlow()

    // IPO Filter
    private val _ipoFilterStatus = MutableStateFlow<IpoStatus?>(null)
    val ipoFilterStatus = _ipoFilterStatus.asStateFlow()

    // Mutual Fund Filter
    private val _fundCategoryFilter = MutableStateFlow("All")
    val fundCategoryFilter = _fundCategoryFilter.asStateFlow()

    // SIP Calculator State
    private val _sipState = MutableStateFlow(SipCalculationResult())
    val sipState = _sipState.asStateFlow()

    val guideChapters = repository.getGuideChapters()

    // Filtered Stocks
    val filteredStocks = combine(stocks, stockSearchQuery, selectedSectorFilter, watchlistSymbols) { list, query, sector, watchlist ->
        list.map { stock ->
            stock.copy(isWatchlisted = watchlist.contains(stock.symbol))
        }.filter { stock ->
            val matchesQuery = query.isBlank() ||
                    stock.symbol.contains(query, ignoreCase = true) ||
                    stock.name.contains(query, ignoreCase = true) ||
                    stock.sector.contains(query, ignoreCase = true)
            val matchesSector = sector == "All" ||
                    (sector == "Watchlist" && stock.isWatchlisted) ||
                    stock.sector.contains(sector, ignoreCase = true)
            matchesQuery && matchesSector
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Portfolio Totals derived from holdings
    val portfolioSummary = holdings.map { list ->
        val invested = list.sumOf { it.investedValue }
        val current = list.sumOf { it.currentValue }
        val profitLoss = current - invested
        val profitPercent = if (invested > 0) (profitLoss / invested) * 100.0 else 0.0
        val todayProfitLoss = list.sumOf { it.quantity * (it.currentPrice * 0.009) } // estimated day movement

        val breakdown = list.groupBy { it.assetType }
            .mapValues { entry -> entry.value.sumOf { it.currentValue } }

        PortfolioStats(
            totalInvested = invested,
            totalCurrent = current,
            totalProfitLoss = profitLoss,
            profitLossPercent = profitPercent,
            todayProfitLoss = todayProfitLoss,
            assetBreakdown = breakdown
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PortfolioStats())

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
        // Clear sub-screen selections when switching tabs
        _selectedStock.value = null
        _selectedIpo.value = null
        _selectedFund.value = null
        _selectedGuideChapter.value = null
    }

    fun openStockDetail(stock: StockQuote) {
        _selectedStock.value = stock
    }

    fun closeStockDetail() {
        _selectedStock.value = null
        _showOrderSheet.value = false
    }

    fun openIpoDetail(ipo: IpoItem) {
        _selectedIpo.value = ipo
    }

    fun closeIpoDetail() {
        _selectedIpo.value = null
    }

    fun openFundDetail(fund: MutualFundItem) {
        _selectedFund.value = fund
    }

    fun closeFundDetail() {
        _selectedFund.value = null
    }

    fun openGuideChapter(chapter: GuideChapter) {
        _selectedGuideChapter.value = chapter
    }

    fun closeGuideChapter() {
        _selectedGuideChapter.value = null
    }

    fun toggleLiveSimulation() {
        repository.toggleLiveSimulation()
    }

    fun setStockSearchQuery(query: String) {
        _stockSearchQuery.value = query
    }

    fun setSectorFilter(sector: String) {
        _selectedSectorFilter.value = sector
    }

    fun setIpoFilter(status: IpoStatus?) {
        _ipoFilterStatus.value = status
    }

    fun setFundCategoryFilter(cat: String) {
        _fundCategoryFilter.value = cat
    }

    fun toggleWatchlist(symbol: String) {
        viewModelScope.launch {
            repository.toggleWatchlist(symbol)
        }
    }

    fun setShowAddHoldingDialog(show: Boolean) {
        _showAddHoldingDialog.value = show
    }

    fun setShowOrderSheet(show: Boolean) {
        _showOrderSheet.value = show
    }

    fun clearOrderSuccessMessage() {
        _orderSuccessMessage.value = null
    }

    fun addHolding(
        symbol: String,
        name: String,
        assetType: AssetType,
        quantity: Double,
        price: Double
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.addHolding(symbol, name, assetType, quantity, price, dateStr)
            _showAddHoldingDialog.value = false
            _orderSuccessMessage.value = "Added $quantity units of $symbol to portfolio!"
        }
    }

    fun deleteHolding(id: Long) {
        viewModelScope.launch {
            repository.deleteHolding(id)
        }
    }

    fun executePaperTrade(
        stock: StockQuote,
        isBuy: Boolean,
        quantity: Double,
        orderType: String // "MARKET" or "LIMIT"
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            if (isBuy) {
                repository.addHolding(
                    stock.symbol,
                    stock.name,
                    AssetType.STOCKS,
                    quantity,
                    stock.currentPrice,
                    dateStr
                )
                _orderSuccessMessage.value = "Market Order Executed: Bought $quantity shares of ${stock.symbol} at ₹${stock.currentPrice}"
            } else {
                _orderSuccessMessage.value = "Sell Order Executed: Sold $quantity shares of ${stock.symbol} at ₹${stock.currentPrice}"
            }
            _showOrderSheet.value = false
        }
    }

    fun updateSipCalculator(monthlyAmount: Double, expectedReturns: Double, durationYears: Int) {
        val i = (expectedReturns / 100.0) / 12.0
        val n = durationYears * 12
        val totalInvested = monthlyAmount * n
        val futureValue = if (i > 0) {
            monthlyAmount * (((1 + i).pow(n.toDouble()) - 1) / i) * (1 + i)
        } else {
            totalInvested
        }
        val estimatedReturns = futureValue - totalInvested

        _sipState.value = SipCalculationResult(
            monthlyInvestment = monthlyAmount,
            returnRatePercent = expectedReturns,
            years = durationYears,
            totalInvested = totalInvested,
            estimatedReturns = estimatedReturns,
            totalFutureValue = futureValue
        )
    }

    fun setShowAlertsModal(show: Boolean) {
        _showAlertsModal.value = show
    }

    fun setShowCreateAlertDialog(show: Boolean) {
        _showCreateAlertDialog.value = show
    }

    fun createAlert(
        symbol: String,
        targetPrice: Double,
        condition: String,
        currentPrice: Double,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.createAlert(symbol, targetPrice, condition, currentPrice, note)
            _showCreateAlertDialog.value = false
            _orderSuccessMessage.value = "🔔 Real-time alert set for $symbol $condition ₹$targetPrice"
        }
    }

    fun deleteAlert(id: Long) {
        viewModelScope.launch {
            repository.deleteAlert(id)
        }
    }

    fun getCandlesForStock(stock: StockQuote, timeframe: String): List<CandleStick> {
        return repository.getCandlesForStock(stock, timeframe)
    }

    fun getTechnicalIndicators(stock: StockQuote): TechnicalIndicators {
        return repository.getTechnicalIndicators(stock)
    }

    fun getFoDetails(stock: StockQuote): FoDetails {
        return repository.getFoDetails(stock)
    }
}

data class PortfolioStats(
    val totalInvested: Double = 0.0,
    val totalCurrent: Double = 0.0,
    val totalProfitLoss: Double = 0.0,
    val profitLossPercent: Double = 0.0,
    val todayProfitLoss: Double = 0.0,
    val assetBreakdown: Map<AssetType, Double> = emptyMap()
)
