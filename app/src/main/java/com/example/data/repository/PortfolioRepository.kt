package com.example.data.repository

import com.example.data.local.AlertEntity
import com.example.data.local.AppDatabase
import com.example.data.local.HoldingEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class PortfolioRepository(private val database: AppDatabase) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Real-Time Alert Trigger Event Flow
    private val _triggeredAlertNotification = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val triggeredAlertNotification = _triggeredAlertNotification.asSharedFlow()

    // Live Stocks State
    private val _stocks = MutableStateFlow<List<StockQuote>>(emptyList())
    val stocks = _stocks.asStateFlow()

    // Benchmark Indices
    private val _indices = MutableStateFlow<List<MarketIndex>>(emptyList())
    val indices = _indices.asStateFlow()

    // IPOs
    private val _ipos = MutableStateFlow<List<IpoItem>>(emptyList())
    val ipos = _ipos.asStateFlow()

    // Mutual Funds
    private val _mutualFunds = MutableStateFlow<List<MutualFundItem>>(emptyList())
    val mutualFunds = _mutualFunds.asStateFlow()

    // Market Sentiment
    private val _marketSentiment = MutableStateFlow(MarketSentiment())
    val marketSentiment = _marketSentiment.asStateFlow()

    // Real-Time Simulation Heartbeat Toggle
    private val _isLiveSimulationEnabled = MutableStateFlow(true)
    val isLiveSimulationEnabled = _isLiveSimulationEnabled.asStateFlow()

    init {
        initializeInitialMarketData()
        seedDefaultHoldingsIfEmpty()
        startRealTimeSimulationLoop()
    }

    fun toggleLiveSimulation() {
        _isLiveSimulationEnabled.value = !_isLiveSimulationEnabled.value
    }

    private fun initializeInitialMarketData() {
        _stocks.value = listOf(
            StockQuote(
                symbol = "RELIANCE",
                name = "Reliance Industries Ltd",
                exchange = "NSE",
                sector = "Energy & Conglomerate",
                currentPrice = 2942.30,
                dayChange = 34.60,
                dayChangePercent = 1.19,
                open = 2912.00,
                high = 2955.00,
                low = 2908.40,
                prevClose = 2907.70,
                volume = "7.8M",
                marketCap = "₹19.9L Cr",
                peRatio = 27.8,
                pbRatio = 2.4,
                dividendYield = 0.35,
                week52High = 3217.90,
                week52Low = 2220.30,
                rsi = 62.4,
                technicalSignal = "Strong Buy",
                sparkline = listOf(2908.0, 2915.0, 2924.0, 2918.0, 2935.0, 2942.3)
            ),
            StockQuote(
                symbol = "TCS",
                name = "Tata Consultancy Services",
                exchange = "NSE",
                sector = "Information Technology",
                currentPrice = 3885.50,
                dayChange = -22.30,
                dayChangePercent = -0.57,
                open = 3915.00,
                high = 3924.00,
                low = 3870.00,
                prevClose = 3907.80,
                volume = "2.4M",
                marketCap = "₹14.1L Cr",
                peRatio = 31.2,
                pbRatio = 14.5,
                dividendYield = 1.25,
                week52High = 4585.00,
                week52Low = 3313.00,
                rsi = 48.7,
                technicalSignal = "Neutral",
                sparkline = listOf(3912.0, 3905.0, 3890.0, 3895.0, 3880.0, 3885.5)
            ),
            StockQuote(
                symbol = "HDFCBANK",
                name = "HDFC Bank Ltd",
                exchange = "NSE",
                sector = "Banking & Finance",
                currentPrice = 1648.20,
                dayChange = 18.45,
                dayChangePercent = 1.13,
                open = 1632.00,
                high = 1654.80,
                low = 1629.10,
                prevClose = 1629.75,
                volume = "14.2M",
                marketCap = "₹12.5L Cr",
                peRatio = 18.9,
                pbRatio = 2.7,
                dividendYield = 1.18,
                week52High = 1794.00,
                week52Low = 1363.55,
                rsi = 59.1,
                technicalSignal = "Buy",
                sparkline = listOf(1630.0, 1635.0, 1642.0, 1639.0, 1645.0, 1648.2)
            ),
            StockQuote(
                symbol = "INFY",
                name = "Infosys Limited",
                exchange = "NSE",
                sector = "Information Technology",
                currentPrice = 1824.75,
                dayChange = 28.30,
                dayChangePercent = 1.58,
                open = 1802.00,
                high = 1832.00,
                low = 1798.50,
                prevClose = 1796.45,
                volume = "6.1M",
                marketCap = "₹7.6L Cr",
                peRatio = 29.4,
                pbRatio = 8.6,
                dividendYield = 2.10,
                week52High = 1991.45,
                week52Low = 1358.35,
                rsi = 65.8,
                technicalSignal = "Strong Buy",
                sparkline = listOf(1798.0, 1805.0, 1814.0, 1810.0, 1820.0, 1824.75)
            ),
            StockQuote(
                symbol = "TATAMOTORS",
                name = "Tata Motors Ltd",
                exchange = "NSE",
                sector = "Automobile",
                currentPrice = 965.80,
                dayChange = 15.20,
                dayChangePercent = 1.60,
                open = 952.00,
                high = 972.40,
                low = 948.00,
                prevClose = 950.60,
                volume = "9.5M",
                marketCap = "₹3.5L Cr",
                peRatio = 11.2,
                pbRatio = 3.8,
                dividendYield = 0.65,
                week52High = 1179.05,
                week52Low = 621.75,
                rsi = 54.3,
                technicalSignal = "Buy",
                sparkline = listOf(951.0, 955.0, 962.0, 958.0, 964.0, 965.8)
            ),
            StockQuote(
                symbol = "ICICIBANK",
                name = "ICICI Bank Ltd",
                exchange = "NSE",
                sector = "Banking & Finance",
                currentPrice = 1242.10,
                dayChange = 14.80,
                dayChangePercent = 1.21,
                open = 1230.00,
                high = 1248.00,
                low = 1227.00,
                prevClose = 1227.30,
                volume = "11.2M",
                marketCap = "₹8.7L Cr",
                peRatio = 17.6,
                pbRatio = 3.1,
                dividendYield = 0.82,
                week52High = 1334.00,
                week52Low = 912.00,
                rsi = 61.2,
                technicalSignal = "Buy",
                sparkline = listOf(1228.0, 1232.0, 1238.0, 1235.0, 1240.0, 1242.1)
            ),
            StockQuote(
                symbol = "BHARTIARTL",
                name = "Bharti Airtel Ltd",
                exchange = "NSE",
                sector = "Telecom",
                currentPrice = 1680.40,
                dayChange = -11.60,
                dayChangePercent = -0.69,
                open = 1695.00,
                high = 1702.00,
                low = 1674.00,
                prevClose = 1692.00,
                volume = "4.3M",
                marketCap = "₹9.8L Cr",
                peRatio = 64.5,
                pbRatio = 9.2,
                dividendYield = 0.48,
                week52High = 1779.00,
                week52Low = 901.00,
                rsi = 52.1,
                technicalSignal = "Neutral",
                sparkline = listOf(1694.0, 1690.0, 1682.0, 1686.0, 1678.0, 1680.4)
            ),
            StockQuote(
                symbol = "ZOMATO",
                name = "Zomato Limited",
                exchange = "NSE",
                sector = "Consumer Internet",
                currentPrice = 258.90,
                dayChange = 8.40,
                dayChangePercent = 3.35,
                open = 251.50,
                high = 262.00,
                low = 250.20,
                prevClose = 250.50,
                volume = "32.4M",
                marketCap = "₹2.3L Cr",
                peRatio = 94.0,
                pbRatio = 9.5,
                dividendYield = 0.0,
                week52High = 298.20,
                week52Low = 101.40,
                rsi = 71.4,
                technicalSignal = "Strong Buy",
                sparkline = listOf(251.0, 254.0, 257.0, 255.0, 258.0, 258.9)
            ),
            StockQuote(
                symbol = "NVDA",
                name = "NVIDIA Corporation",
                exchange = "NASDAQ",
                sector = "Semiconductors & AI",
                currentPrice = 138.25,
                dayChange = 4.15,
                dayChangePercent = 3.09,
                open = 134.80,
                high = 139.10,
                low = 134.20,
                prevClose = 134.10,
                volume = "48.2M",
                marketCap = "$3.38T",
                peRatio = 48.5,
                pbRatio = 38.2,
                dividendYield = 0.03,
                week52High = 144.42,
                week52Low = 45.60,
                rsi = 68.2,
                technicalSignal = "Strong Buy",
                sparkline = listOf(134.5, 135.8, 137.2, 136.5, 138.0, 138.25)
            ),
            StockQuote(
                symbol = "AAPL",
                name = "Apple Inc.",
                exchange = "NASDAQ",
                sector = "Consumer Tech",
                currentPrice = 232.50,
                dayChange = 1.80,
                dayChangePercent = 0.78,
                open = 231.00,
                high = 233.40,
                low = 230.60,
                prevClose = 230.70,
                volume = "39.1M",
                marketCap = "$3.52T",
                peRatio = 34.2,
                pbRatio = 49.1,
                dividendYield = 0.44,
                week52High = 237.23,
                week52Low = 164.08,
                rsi = 56.4,
                technicalSignal = "Buy",
                sparkline = listOf(231.0, 231.5, 232.2, 231.8, 232.4, 232.5)
            )
        )

        _indices.value = listOf(
            MarketIndex(
                name = "NIFTY 50",
                region = "India",
                category = "India Benchmark",
                value = 24857.30,
                change = 182.40,
                changePercent = 0.74,
                dayHigh = 24915.20,
                dayLow = 24680.10,
                sparkline = listOf(24680.0, 24710.0, 24790.0, 24820.0, 24857.3)
            ),
            MarketIndex(
                name = "SENSEX",
                region = "India",
                category = "India Benchmark",
                value = 81520.15,
                change = 560.80,
                changePercent = 0.69,
                dayHigh = 81680.40,
                dayLow = 80980.00,
                sparkline = listOf(80980.0, 81100.0, 81350.0, 81420.0, 81520.15)
            ),
            MarketIndex(
                name = "BANK NIFTY",
                region = "India",
                category = "Sectoral",
                value = 52310.40,
                change = 385.20,
                changePercent = 0.74,
                dayHigh = 52450.80,
                dayLow = 51950.00,
                sparkline = listOf(51950.0, 52040.0, 52180.0, 52240.0, 52310.4)
            ),
            MarketIndex(
                name = "NIFTY IT",
                region = "India",
                category = "Sectoral",
                value = 42890.75,
                change = 612.30,
                changePercent = 1.45,
                dayHigh = 43010.00,
                dayLow = 42200.50,
                sparkline = listOf(42200.0, 42350.0, 42600.0, 42720.0, 42890.75)
            ),
            MarketIndex(
                name = "NASDAQ 100",
                region = "USA",
                category = "US & Global",
                value = 20384.60,
                change = 210.50,
                changePercent = 1.04,
                dayHigh = 20420.00,
                dayLow = 20180.20,
                sparkline = listOf(20180.0, 20220.0, 20290.0, 20340.0, 20384.6)
            ),
            MarketIndex(
                name = "S&P 500",
                region = "USA",
                category = "US & Global",
                value = 5864.20,
                change = 32.10,
                changePercent = 0.55,
                dayHigh = 5878.50,
                dayLow = 5832.00,
                sparkline = listOf(5832.0, 5840.0, 5850.0, 5858.0, 5864.2)
            ),
            MarketIndex(
                name = "DOW JONES",
                region = "USA",
                category = "US & Global",
                value = 42863.85,
                change = -84.20,
                changePercent = -0.20,
                dayHigh = 42980.10,
                dayLow = 42710.00,
                sparkline = listOf(42950.0, 42920.0, 42880.0, 42840.0, 42863.85)
            )
        )

        _ipos.value = listOf(
            IpoItem(
                symbol = "HYUNDAI",
                companyName = "Hyundai Motor India Ltd",
                sector = "Automobiles",
                priceBand = "₹1,865 - ₹1,960",
                lotSize = 7,
                issueSize = "₹27,870 Cr",
                openDate = "Oct 15",
                closeDate = "Oct 17",
                listingDate = "Oct 22",
                status = IpoStatus.LISTED,
                gmpAmount = 65.0,
                gmpPercent = 3.3,
                totalSubscription = 2.37,
                retailSubscription = 0.50,
                qibSubscription = 6.97,
                rating = "Neutral",
                about = "India's second-largest passenger vehicle maker, offering a complete lineup from compact cars to premium SUVs."
            ),
            IpoItem(
                symbol = "SWIGGY",
                companyName = "Swiggy Limited",
                sector = "Quick Commerce & Food Delivery",
                priceBand = "₹371 - ₹390",
                lotSize = 38,
                issueSize = "₹11,327 Cr",
                openDate = "Nov 06",
                closeDate = "Nov 08",
                listingDate = "Nov 13",
                status = IpoStatus.OPEN,
                gmpAmount = 28.0,
                gmpPercent = 7.18,
                totalSubscription = 3.59,
                retailSubscription = 1.14,
                qibSubscription = 6.02,
                rating = "Apply for Long Term",
                about = "Pioneering hyper-local commerce platform operating food delivery, Instamart quick commerce, and dining solutions."
            ),
            IpoItem(
                symbol = "NTPCGREEN",
                companyName = "NTPC Green Energy Ltd",
                sector = "Renewable Energy",
                priceBand = "₹102 - ₹108",
                lotSize = 138,
                issueSize = "₹10,000 Cr",
                openDate = "Nov 19",
                closeDate = "Nov 22",
                listingDate = "Nov 27",
                status = IpoStatus.UPCOMING,
                gmpAmount = 18.0,
                gmpPercent = 16.67,
                totalSubscription = 0.0,
                retailSubscription = 0.0,
                qibSubscription = 0.0,
                rating = "Strong Apply",
                about = "Wholly owned subsidiary of Maharatna PSU NTPC, dedicated to solar, wind, and green hydrogen power capacity."
            ),
            IpoItem(
                symbol = "WAREE",
                companyName = "Waaree Energies Ltd",
                sector = "Solar Manufacturing",
                priceBand = "₹1,427 - ₹1,503",
                lotSize = 9,
                issueSize = "₹4,321 Cr",
                openDate = "Oct 21",
                closeDate = "Oct 23",
                listingDate = "Oct 28",
                status = IpoStatus.LISTED,
                gmpAmount = 1450.0,
                gmpPercent = 96.47,
                totalSubscription = 76.34,
                retailSubscription = 10.79,
                qibSubscription = 208.63,
                rating = "Super Bumper Listing",
                about = "India's largest solar PV module exporter with 12 GW installed module manufacturing capacity."
            )
        )

        _mutualFunds.value = listOf(
            MutualFundItem(
                id = "mf_1",
                name = "Parag Parikh Flexi Cap Fund - Direct Growth",
                amc = "PPFAS Mutual Fund",
                category = "Flexi Cap",
                nav = 78.45,
                dayChangePercent = 0.82,
                returns1Y = 38.6,
                returns3Y = 22.4,
                returns5Y = 24.8,
                aum = "₹78,450 Cr",
                expenseRatio = 0.62,
                fundManager = "Rajeev Thakkar",
                riskometer = "Very High",
                minSip = 500.0,
                ratingStars = 5
            ),
            MutualFundItem(
                id = "mf_2",
                name = "Mirae Asset Large Cap Fund - Direct Growth",
                amc = "Mirae Asset Mutual Fund",
                category = "Large Cap",
                nav = 118.20,
                dayChangePercent = 0.65,
                returns1Y = 28.4,
                returns3Y = 16.8,
                returns5Y = 17.5,
                aum = "₹39,800 Cr",
                expenseRatio = 0.54,
                fundManager = "Gaurav Misra",
                riskometer = "Very High",
                minSip = 1000.0,
                ratingStars = 5
            ),
            MutualFundItem(
                id = "mf_3",
                name = "Quant Small Cap Fund - Direct Growth",
                amc = "Quant Mutual Fund",
                category = "Small Cap",
                nav = 246.80,
                dayChangePercent = 1.45,
                returns1Y = 51.2,
                returns3Y = 32.1,
                returns5Y = 38.9,
                aum = "₹24,100 Cr",
                expenseRatio = 0.77,
                fundManager = "Sandeep Tandon",
                riskometer = "Very High",
                minSip = 1000.0,
                ratingStars = 5
            ),
            MutualFundItem(
                id = "mf_4",
                name = "UTI Nifty 50 Index Fund - Direct Growth",
                amc = "UTI Mutual Fund",
                category = "Index",
                nav = 162.30,
                dayChangePercent = 0.74,
                returns1Y = 27.2,
                returns3Y = 15.6,
                returns5Y = 17.1,
                aum = "₹18,900 Cr",
                expenseRatio = 0.18,
                fundManager = "Sharwan Kumar Goyal",
                riskometer = "High",
                minSip = 500.0,
                ratingStars = 4
            ),
            MutualFundItem(
                id = "mf_5",
                name = "HDFC Mid-Cap Opportunities Fund",
                amc = "HDFC Mutual Fund",
                category = "Mid Cap",
                nav = 194.50,
                dayChangePercent = 1.12,
                returns1Y = 44.8,
                returns3Y = 26.9,
                returns5Y = 25.2,
                aum = "₹68,200 Cr",
                expenseRatio = 0.78,
                fundManager = "Chirag Setalvad",
                riskometer = "Very High",
                minSip = 500.0,
                ratingStars = 5
            ),
            MutualFundItem(
                id = "mf_6",
                name = "Mirae Asset ELSS Tax Saver Fund",
                amc = "Mirae Asset Mutual Fund",
                category = "ELSS (Tax Saver)",
                nav = 45.60,
                dayChangePercent = 0.90,
                returns1Y = 33.1,
                returns3Y = 18.7,
                returns5Y = 20.4,
                aum = "₹22,400 Cr",
                expenseRatio = 0.58,
                fundManager = "Neelesh Surana",
                riskometer = "Very High",
                minSip = 500.0,
                ratingStars = 5
            )
        )
    }

    private fun seedDefaultHoldingsIfEmpty() {
        coroutineScope.launch {
            if (database.holdingDao().getHoldingsCount() == 0) {
                val defaultHoldings = listOf(
                    HoldingEntity(
                        symbol = "RELIANCE",
                        name = "Reliance Industries Ltd",
                        assetType = AssetType.STOCKS.name,
                        quantity = 25.0,
                        buyPrice = 2720.00,
                        purchaseDate = "2024-02-14"
                    ),
                    HoldingEntity(
                        symbol = "TCS",
                        name = "Tata Consultancy Services",
                        assetType = AssetType.STOCKS.name,
                        quantity = 15.0,
                        buyPrice = 3650.00,
                        purchaseDate = "2024-01-20"
                    ),
                    HoldingEntity(
                        symbol = "HDFCBANK",
                        name = "HDFC Bank Ltd",
                        assetType = AssetType.STOCKS.name,
                        quantity = 40.0,
                        buyPrice = 1540.00,
                        purchaseDate = "2024-03-10"
                    ),
                    HoldingEntity(
                        symbol = "PARAG_PARIKH",
                        name = "Parag Parikh Flexi Cap Fund",
                        assetType = AssetType.MUTUAL_FUNDS.name,
                        quantity = 950.0,
                        buyPrice = 64.50,
                        purchaseDate = "2023-11-05"
                    ),
                    HoldingEntity(
                        symbol = "WAREE",
                        name = "Waaree Energies Allotment",
                        assetType = AssetType.IPOS.name,
                        quantity = 9.0,
                        buyPrice = 1503.00,
                        purchaseDate = "2024-10-28"
                    ),
                    HoldingEntity(
                        symbol = "SGB_2029",
                        name = "Sovereign Gold Bond Series V",
                        assetType = AssetType.GOLD_BONDS.name,
                        quantity = 10.0,
                        buyPrice = 6200.00,
                        purchaseDate = "2023-09-18"
                    )
                )
                defaultHoldings.forEach { database.holdingDao().insertHolding(it) }

                // Seed initial transactions
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                defaultHoldings.forEach {
                    database.transactionDao().insertTransaction(
                        TransactionEntity(
                            symbol = it.symbol,
                            name = it.name,
                            type = "BUY",
                            assetType = it.assetType,
                            quantity = it.quantity,
                            price = it.buyPrice,
                            date = it.purchaseDate
                        )
                    )
                }

                // Seed initial watchlist
                database.watchlistDao().addToWatchlist(WatchlistEntity("RELIANCE"))
                database.watchlistDao().addToWatchlist(WatchlistEntity("INFY"))
                database.watchlistDao().addToWatchlist(WatchlistEntity("ZOMATO"))

                // Seed initial real-time price alerts
                database.alertDao().insertAlert(
                    AlertEntity(
                        symbol = "RELIANCE",
                        targetPrice = 2955.00,
                        condition = "ABOVE",
                        createdPrice = 2942.30,
                        createdDate = "Today 09:30",
                        note = "Breakout alert above key resistance"
                    )
                )
                database.alertDao().insertAlert(
                    AlertEntity(
                        symbol = "TCS",
                        targetPrice = 3870.00,
                        condition = "BELOW",
                        createdPrice = 3885.50,
                        createdDate = "Today 09:30",
                        note = "Stop loss warning level"
                    )
                )
                database.alertDao().insertAlert(
                    AlertEntity(
                        symbol = "INFY",
                        targetPrice = 1835.00,
                        condition = "ABOVE",
                        createdPrice = 1824.75,
                        createdDate = "Today 10:15",
                        note = "Quarterly earnings breakout target"
                    )
                )
            }
        }
    }

    private fun startRealTimeSimulationLoop() {
        coroutineScope.launch {
            while (true) {
                delay(2500)
                if (_isLiveSimulationEnabled.value) {
                    simulateMarketPulse()
                }
            }
        }
    }

    private fun simulateMarketPulse() {
        // Micro fluctuation for stocks
        _stocks.value = _stocks.value.map { stock ->
            val deltaPercent = (Random.nextDouble(-0.35, 0.40))
            val priceFactor = 1.0 + (deltaPercent / 100.0)
            val newPrice = Math.round(stock.currentPrice * priceFactor * 100.0) / 100.0
            val newDayChange = Math.round((newPrice - stock.prevClose) * 100.0) / 100.0
            val newChangePercent = Math.round((newDayChange / stock.prevClose) * 10000.0) / 100.0
            val updatedSparkline = (stock.sparkline.drop(1) + newPrice)
            stock.copy(
                currentPrice = newPrice,
                dayChange = newDayChange,
                dayChangePercent = newChangePercent,
                sparkline = updatedSparkline,
                high = maxOf(stock.high, newPrice),
                low = minOf(stock.low, newPrice)
            )
        }

        // Micro fluctuation for indices
        _indices.value = _indices.value.map { index ->
            val delta = Random.nextDouble(-0.15, 0.20)
            val newValue = Math.round((index.value * (1.0 + delta / 100.0)) * 100.0) / 100.0
            val newChange = Math.round((index.change + (newValue - index.value)) * 100.0) / 100.0
            val newChangePercent = Math.round((newChange / (newValue - newChange)) * 10000.0) / 100.0
            index.copy(
                value = newValue,
                change = newChange,
                changePercent = newChangePercent,
                dayHigh = maxOf(index.dayHigh, newValue),
                dayLow = minOf(index.dayLow, newValue),
                sparkline = (index.sparkline.drop(1) + newValue)
            )
        }

        // Real-time evaluation of active price alerts
        coroutineScope.launch {
            val activeAlerts = database.alertDao().getActiveAlerts()
            if (activeAlerts.isNotEmpty()) {
                val stockMap = _stocks.value.associateBy { it.symbol }
                val timeNow = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                activeAlerts.forEach { alert ->
                    val stock = stockMap[alert.symbol]
                    if (stock != null) {
                        val isTriggered = when (alert.condition) {
                            "ABOVE" -> stock.currentPrice >= alert.targetPrice
                            "BELOW" -> stock.currentPrice <= alert.targetPrice
                            else -> false
                        }
                        if (isTriggered) {
                            database.alertDao().markAlertTriggered(alert.id, timeNow)
                            val conditionText = if (alert.condition == "ABOVE") "crossed ABOVE" else "dropped BELOW"
                            _triggeredAlertNotification.emit(
                                "🚨 ALERT: ${alert.symbol} $conditionText target ₹${String.format(Locale.US, "%.2f", alert.targetPrice)} (LTP: ₹${String.format(Locale.US, "%.2f", stock.currentPrice)})"
                            )
                        }
                    }
                }
            }
        }
    }

    // Connect Room Holdings with live current prices
    fun getPortfolioHoldings(): Flow<List<PortfolioHolding>> {
        return database.holdingDao().getAllHoldings().map { entities ->
            val stockMap = _stocks.value.associateBy { it.symbol }
            val mfMap = _mutualFunds.value.associateBy { it.id }
            val ipoMap = _ipos.value.associateBy { it.symbol }

            entities.map { entity ->
                val currentPrice = when {
                    stockMap.containsKey(entity.symbol) -> stockMap[entity.symbol]!!.currentPrice
                    entity.symbol == "PARAG_PARIKH" -> 78.45
                    entity.symbol == "WAREE" -> 2950.00 // post-listing price
                    entity.symbol == "SGB_2029" -> 7180.00
                    else -> entity.buyPrice * 1.15
                }
                val assetType = try {
                    AssetType.valueOf(entity.assetType)
                } catch (e: Exception) {
                    AssetType.STOCKS
                }

                PortfolioHolding(
                    id = entity.id,
                    symbol = entity.symbol,
                    name = entity.name,
                    assetType = assetType,
                    quantity = entity.quantity,
                    buyPrice = entity.buyPrice,
                    currentPrice = currentPrice,
                    purchaseDate = entity.purchaseDate
                )
            }
        }
    }

    suspend fun addHolding(
        symbol: String,
        name: String,
        assetType: AssetType,
        quantity: Double,
        buyPrice: Double,
        date: String
    ) {
        val entity = HoldingEntity(
            symbol = symbol.uppercase().trim(),
            name = name.trim(),
            assetType = assetType.name,
            quantity = quantity,
            buyPrice = buyPrice,
            purchaseDate = date
        )
        database.holdingDao().insertHolding(entity)
        database.transactionDao().insertTransaction(
            TransactionEntity(
                symbol = symbol.uppercase().trim(),
                name = name.trim(),
                type = "BUY",
                assetType = assetType.name,
                quantity = quantity,
                price = buyPrice,
                date = date
            )
        )
    }

    suspend fun deleteHolding(id: Long) {
        database.holdingDao().deleteById(id)
    }

    fun getTransactions(): Flow<List<PortfolioTransaction>> {
        return database.transactionDao().getAllTransactions().map { list ->
            list.map {
                val assetType = try {
                    AssetType.valueOf(it.assetType)
                } catch (e: Exception) {
                    AssetType.STOCKS
                }
                PortfolioTransaction(
                    id = it.id,
                    symbol = it.symbol,
                    name = it.name,
                    type = it.type,
                    assetType = assetType,
                    quantity = it.quantity,
                    price = it.price,
                    date = it.date
                )
            }
        }
    }

    fun getWatchlist(): Flow<List<String>> {
        return database.watchlistDao().getAllWatchlist().map { list -> list.map { it.symbol } }
    }

    suspend fun toggleWatchlist(symbol: String) {
        val exists = database.watchlistDao().isWatchlisted(symbol)
        if (exists) {
            database.watchlistDao().removeFromWatchlist(symbol)
        } else {
            database.watchlistDao().addToWatchlist(WatchlistEntity(symbol))
        }
    }

    // Real-Time Price Alerts
    fun getAlerts(): Flow<List<RealTimeAlert>> {
        return database.alertDao().getAllAlerts().map { list ->
            list.map {
                RealTimeAlert(
                    id = it.id,
                    symbol = it.symbol,
                    targetPrice = it.targetPrice,
                    condition = it.condition,
                    createdPrice = it.createdPrice,
                    createdDate = it.createdDate,
                    isTriggered = it.isTriggered,
                    triggeredDate = it.triggeredDate,
                    note = it.note
                )
            }
        }
    }

    suspend fun createAlert(
        symbol: String,
        targetPrice: Double,
        condition: String,
        currentPrice: Double,
        note: String
    ) {
        val now = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
        database.alertDao().insertAlert(
            AlertEntity(
                symbol = symbol.uppercase().trim(),
                targetPrice = targetPrice,
                condition = condition,
                createdPrice = currentPrice,
                createdDate = now,
                note = note.trim()
            )
        )
    }

    suspend fun deleteAlert(id: Long) {
        database.alertDao().deleteAlert(id)
    }

    // High-Resolution Candlestick generator for technical charting
    fun getCandlesForStock(stock: StockQuote, timeframe: String): List<CandleStick> {
        val base = stock.currentPrice
        val count = when (timeframe) {
            "1D" -> 16 // Intraday 15-min bars
            "1W" -> 20 // 1-hour bars
            "1M" -> 22 // Daily bars
            "1Y" -> 24 // Weekly bars
            else -> 30 // Monthly bars
        }

        val timestamps = when (timeframe) {
            "1D" -> listOf("09:30", "09:45", "10:00", "10:15", "10:30", "10:45", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30")
            "1W" -> listOf("Mon 10am", "Mon 2pm", "Tue 10am", "Tue 2pm", "Wed 10am", "Wed 2pm", "Thu 10am", "Thu 2pm", "Fri 10am", "Fri 2pm")
            "1M" -> (1..22).map { "Day $it" }
            "1Y" -> listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            else -> listOf("2021", "2022", "2023", "2024", "2025")
        }

        var runningPrice = when (timeframe) {
            "1D" -> stock.open
            "1W" -> stock.currentPrice * 0.97
            "1M" -> stock.currentPrice * 0.94
            "1Y" -> stock.week52Low
            else -> stock.week52Low * 0.65
        }

        return timestamps.take(count).mapIndexed { idx, time ->
            val volatility = base * 0.008
            val isLast = idx == timestamps.take(count).size - 1
            val open = runningPrice
            val close = if (isLast) base else (open + Random.nextDouble(-volatility, volatility * 1.15))
            val high = maxOf(open, close) + Random.nextDouble(1.0, volatility * 0.8)
            val low = minOf(open, close) - Random.nextDouble(1.0, volatility * 0.8)
            val volume = Random.nextLong(250000, 1500000)
            runningPrice = close
            CandleStick(
                time = time,
                open = Math.round(open * 100.0) / 100.0,
                high = Math.round(high * 100.0) / 100.0,
                low = Math.round(low * 100.0) / 100.0,
                close = Math.round(close * 100.0) / 100.0,
                volume = volume
            )
        }
    }

    // Institutional Technical Indicators used by trading desks
    fun getTechnicalIndicators(stock: StockQuote): TechnicalIndicators {
        val p = stock.currentPrice
        val pivot = Math.round(((stock.high + stock.low + stock.currentPrice) / 3.0) * 100.0) / 100.0
        val r1 = Math.round((2 * pivot - stock.low) * 100.0) / 100.0
        val r2 = Math.round((pivot + (stock.high - stock.low)) * 100.0) / 100.0
        val s1 = Math.round((2 * pivot - stock.high) * 100.0) / 100.0
        val s2 = Math.round((pivot - (stock.high - stock.low)) * 100.0) / 100.0

        val rsi = stock.rsi
        val ema20 = Math.round(p * 0.993 * 100.0) / 100.0
        val dma50 = Math.round(p * 0.978 * 100.0) / 100.0
        val dma200 = Math.round(p * 0.925 * 100.0) / 100.0
        val vwap = Math.round(p * 0.998 * 100.0) / 100.0

        // Bollinger Bands (20, 2)
        val bollingerMid = ema20
        val bollingerUpper = Math.round(p * 1.032 * 100.0) / 100.0
        val bollingerLower = Math.round(p * 0.968 * 100.0) / 100.0

        // MACD (12, 26, 9)
        val macdLine = Math.round(p * 0.0085 * 100.0) / 100.0
        val signalLine = Math.round(macdLine * 0.72 * 100.0) / 100.0
        val macdHist = Math.round((macdLine - signalLine) * 100.0) / 100.0

        return TechnicalIndicators(
            rsi = rsi,
            macdLine = macdLine,
            signalLine = signalLine,
            macdHist = macdHist,
            bollingerUpper = bollingerUpper,
            bollingerMiddle = bollingerMid,
            bollingerLower = bollingerLower,
            ema20 = ema20,
            dma50 = dma50,
            dma200 = dma200,
            vwap = vwap,
            pivot = pivot,
            r1 = r1,
            r2 = r2,
            s1 = s1,
            s2 = s2,
            bullishCount = if (rsi > 50) 14 else 6,
            bearishCount = if (rsi > 50) 2 else 9,
            neutralCount = 4,
            overallSignal = if (rsi > 60) "Strong Buy" else if (rsi > 45) "Buy" else "Neutral"
        )
    }

    // F&O (Futures & Options) Derivatives Engine
    fun getFoDetails(stock: StockQuote): FoDetails {
        val p = stock.currentPrice
        val step = when {
            p > 3000 -> 50.0
            p > 1000 -> 20.0
            p > 400 -> 10.0
            else -> 5.0
        }
        val atmStrike = Math.round(p / step) * step
        val lotSize = when (stock.symbol) {
            "RELIANCE" -> 250
            "TCS" -> 175
            "INFY" -> 400
            "HDFCBANK" -> 550
            "TATAMOTORS" -> 1425
            "ZOMATO" -> 2200
            else -> 300
        }

        val strikes = (-3..3).map { offset ->
            val strike = atmStrike + (offset * step)
            val isAtm = offset == 0
            val distance = strike - p

            // Call pricing: higher when strike < price (ITM)
            val callLtp = if (strike <= p) (p - strike) + (step * 0.45) else maxOf(2.5, (step * 0.7) - (distance * 0.15))
            val putLtp = if (strike >= p) (strike - p) + (step * 0.45) else maxOf(2.5, (step * 0.7) + (distance * 0.15))

            val callOi = Random.nextLong(1200000, 4800000)
            val putOi = Random.nextLong(900000, 5200000)
            val callIv = 18.2 + (offset * 0.6)
            val putIv = 19.5 - (offset * 0.5)

            FoOptionStrike(
                strikePrice = strike,
                callLtp = Math.round(callLtp * 100.0) / 100.0,
                callChangePercent = Math.round(Random.nextDouble(-12.0, 18.0) * 10.0) / 10.0,
                callOi = callOi,
                callIv = Math.round(callIv * 10.0) / 10.0,
                putLtp = Math.round(putLtp * 100.0) / 100.0,
                putChangePercent = Math.round(Random.nextDouble(-15.0, 14.0) * 10.0) / 10.0,
                putOi = putOi,
                putIv = Math.round(putIv * 10.0) / 10.0,
                isAtm = isAtm
            )
        }

        val futurePrice = Math.round((p * 1.004) * 100.0) / 100.0
        val basis = Math.round((futurePrice - p) * 100.0) / 100.0

        return FoDetails(
            futurePrice = futurePrice,
            basis = basis,
            rolloverPercent = 78.4,
            lotSize = lotSize,
            pcr = 1.18,
            pcrInterpretation = "Moderately Bullish (Call Buying / Put Writing)",
            maxPain = atmStrike,
            atmStrike = atmStrike,
            strikes = strikes
        )
    }

    fun getGuideChapters(): List<GuideChapter> {
        return listOf(
            GuideChapter(
                id = 1,
                stepNumber = "Step 01",
                title = "Getting Started: Demat, KYC & Broker Setup",
                subtitle = "The essential foundation for entering stock and mutual fund markets",
                durationMinutes = 6,
                keyConcepts = listOf("PAN Card & Bank Account", "Demat vs Trading Account", "Discount vs Full-Service Brokers", "KYC Verification"),
                sections = listOf(
                    GuideSection(
                        title = "What is a Demat & Trading Account?",
                        content = "To buy shares in companies, you need two accounts: A Trading Account (which places buy/sell orders on the exchange) and a Demat Account (Dematerialized account where your purchased shares and mutual fund units are safely held electronically by depositories like NSDL or CDSL).",
                        tip = "Always check depository SMS/Email alerts for every trade confirmation to ensure your securities are safely credited."
                    ),
                    GuideSection(
                        title = "Choosing the Right Broker",
                        content = "Discount brokers charge flat brokerage fees per order (often ₹20 or $0 per trade) and provide fast modern mobile apps. Full-service brokers charge percentage commissions and offer dedicated relationship managers and advisory calls.",
                        tip = "For beginners and active DIY investors, discount brokers offer maximum savings on brokerage and transaction costs."
                    )
                )
            ),
            GuideChapter(
                id = 2,
                stepNumber = "Step 02",
                title = "Order Types & Market Mechanics",
                subtitle = "Understanding Market, Limit, Stop-Loss, and settlement cycles",
                durationMinutes = 8,
                keyConcepts = listOf("Market Order vs Limit Order", "Stop Loss (SL & SL-M)", "T+1 Settlement Cycle", "Market Timings (Pre-open & Regular)"),
                sections = listOf(
                    GuideSection(
                        title = "Market vs Limit Orders",
                        content = "A Market Order executes instantly at whatever best price is available right now in the order book. A Limit Order specifies the exact maximum price you are willing to pay (or minimum price you want to sell at).",
                        tip = "Always use Limit Orders during market open or during high volatility to protect yourself from slippage."
                    ),
                    GuideSection(
                        title = "Stop-Loss (Risk Management Rule #1)",
                        content = "A Stop-Loss order automatically triggers a sell order when the stock price falls to a pre-set threshold. It limits your downside and prevents emotional decision-making when the trade turns against you.",
                        tip = "Never enter a short-term or swing trade without defining your maximum acceptable loss (e.g., 2% to 5% risk per trade)."
                    )
                )
            ),
            GuideChapter(
                id = 3,
                stepNumber = "Step 03",
                title = "Fundamental Analysis: Picking Great Businesses",
                subtitle = "How to evaluate company health, valuation ratios, and moats",
                durationMinutes = 10,
                keyConcepts = listOf("P/E & P/B Ratios", "Return on Equity (ROE)", "Debt-to-Equity & Free Cash Flow", "Competitive Moats"),
                sections = listOf(
                    GuideSection(
                        title = "P/E Ratio (Price-to-Earnings)",
                        content = "The P/E ratio tells you how much money the market is paying for ₹1 (or $1) of company earnings. Compare a stock's P/E to its sector peers and its own historical 5-year average rather than in isolation.",
                        tip = "A low P/E isn't always cheap (could be a value trap), and a high P/E isn't always expensive if earnings are growing at 30%+ yearly."
                    ),
                    GuideSection(
                        title = "Debt & Balance Sheet Strength",
                        content = "Companies with high debt struggle during interest rate hikes and economic downturns. Look for Debt-to-Equity < 1.0, steady operating margins, and consistent positive Free Cash Flow.",
                        tip = "Consistent Return on Equity (ROE > 15%) is a great hallmark of a management team that allocates capital efficiently."
                    )
                )
            ),
            GuideChapter(
                id = 4,
                stepNumber = "Step 04",
                title = "Technical Analysis & Chart Reading",
                subtitle = "Price action, Candlesticks, RSI, and Trendlines",
                durationMinutes = 9,
                keyConcepts = listOf("Candlestick Anatomy", "Support & Resistance", "RSI (Relative Strength Index)", "Moving Averages (50 & 200 DMA)"),
                sections = listOf(
                    GuideSection(
                        title = "Reading Candlesticks",
                        content = "Each candle shows Open, High, Low, and Close for a period. Green candles indicate buyers dominated, pushing close above open. Long wicks at the bottom show strong buying rejection of lower prices.",
                        tip = "Look for candle patterns at key support or resistance levels, not randomly in the middle of a consolidation range."
                    ),
                    GuideSection(
                        title = "RSI (Momentum Oscillator)",
                        content = "RSI measures price momentum on a scale from 0 to 100. RSI > 70 indicates overbought conditions (potential pullback), while RSI < 30 indicates oversold conditions (potential bounce).",
                        tip = "In strong uptrends, RSI can stay overbought for long periods. Use RSI divergence for higher probability reversal signals."
                    )
                )
            ),
            GuideChapter(
                id = 5,
                stepNumber = "Step 05",
                title = "Demystifying IPOs & Grey Market Premium (GMP)",
                subtitle = "How initial public offerings work, bidding bands, and allotment odds",
                durationMinutes = 7,
                keyConcepts = listOf("IPO Price Band & Lot Size", "Grey Market Premium (GMP)", "Subscription Multiples (QIB, Retail)", "Listing Day Strategy"),
                sections = listOf(
                    GuideSection(
                        title = "Understanding GMP (Grey Market Premium)",
                        content = "GMP is the unofficial premium at which an IPO's shares trade in the unorganized market before official exchange listing. If issue price is ₹500 and GMP is ₹150, estimated listing price is ₹650 (+30%).",
                        tip = "GMP is volatile and informal. Never apply solely for high GMP without checking the company's financial fundamentals and QIB demand."
                    ),
                    GuideSection(
                        title = "Increasing Allotment Chances",
                        content = "Retail allotment is done via lottery when oversubscribed. Applying for multiple lots under the same PAN does NOT increase your lottery odds in retail! Apply with 1 lot across different family members' PANs instead.",
                        tip = "Always bid at the 'Cut-off' price (the ceiling of the price band) to ensure your bid isn't rejected if finalized at the top band."
                    )
                )
            ),
            GuideChapter(
                id = 6,
                stepNumber = "Step 06",
                title = "Mutual Funds, SIP & Compounding Wealth",
                subtitle = "Building a diversified, stress-free long-term wealth engine",
                durationMinutes = 8,
                keyConcepts = listOf("Active vs Passive Index Funds", "SIP (Systematic Investment Plan)", "Expense Ratio & Direct Plans", "Asset Allocation"),
                sections = listOf(
                    GuideSection(
                        title = "SIP & Rupee Cost Averaging",
                        content = "A SIP automatically invests a fixed amount on a set day every month. When markets fall, your SIP buys more units at cheaper NAV; when markets rise, you enjoy capital appreciation.",
                        tip = "Never stop your SIP during market crashes—that is precisely when you accumulate the most units at deep discounts."
                    ),
                    GuideSection(
                        title = "Direct vs Regular Plans",
                        content = "Direct plans eliminate distributor commissions, saving ~0.7% to 1.2% every single year. Over 15-20 years of compounding, that 1% difference can mean ₹20-50 Lakhs extra in your pocket!",
                        tip = "Always choose 'Direct - Growth' mutual fund schemes for maximum compounding efficiency."
                    )
                )
            )
        )
    }
}
