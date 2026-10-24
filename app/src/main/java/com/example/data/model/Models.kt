package com.example.data.model

enum class AssetType(val label: String) {
    STOCKS("Stocks"),
    MUTUAL_FUNDS("Mutual Funds"),
    IPOS("IPOs"),
    GOLD_BONDS("Gold & Bonds")
}

data class StockQuote(
    val symbol: String,
    val name: String,
    val exchange: String = "NSE",
    val sector: String,
    val currentPrice: Double,
    val dayChange: Double,
    val dayChangePercent: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val prevClose: Double,
    val volume: String,
    val marketCap: String,
    val peRatio: Double,
    val pbRatio: Double,
    val dividendYield: Double,
    val week52High: Double,
    val week52Low: Double,
    val rsi: Double,
    val technicalSignal: String,
    val sparkline: List<Double>,
    val isWatchlisted: Boolean = false
) {
    val isPositive: Boolean get() = dayChange >= 0.0
}

data class MarketIndex(
    val name: String,
    val region: String,
    val category: String = "Benchmark",
    val value: Double,
    val change: Double,
    val changePercent: Double,
    val dayHigh: Double = value * 1.004,
    val dayLow: Double = value * 0.994,
    val sparkline: List<Double>
) {
    val isPositive: Boolean get() = change >= 0.0
}

enum class IpoStatus(val label: String) {
    UPCOMING("Upcoming"),
    OPEN("Bidding Open"),
    CLOSED("Closed / Allotment"),
    LISTED("Recently Listed")
}

data class IpoItem(
    val symbol: String,
    val companyName: String,
    val sector: String,
    val priceBand: String,
    val lotSize: Int,
    val issueSize: String,
    val openDate: String,
    val closeDate: String,
    val listingDate: String,
    val status: IpoStatus,
    val gmpAmount: Double,
    val gmpPercent: Double,
    val totalSubscription: Double,
    val retailSubscription: Double,
    val qibSubscription: Double,
    val rating: String,
    val about: String
)

data class MutualFundItem(
    val id: String,
    val name: String,
    val amc: String,
    val category: String,
    val nav: Double,
    val dayChangePercent: Double,
    val returns1Y: Double,
    val returns3Y: Double,
    val returns5Y: Double,
    val aum: String,
    val expenseRatio: Double,
    val fundManager: String,
    val riskometer: String,
    val minSip: Double,
    val ratingStars: Int = 5
)

data class PortfolioHolding(
    val id: Long = 0,
    val symbol: String,
    val name: String,
    val assetType: AssetType,
    val quantity: Double,
    val buyPrice: Double,
    val currentPrice: Double,
    val purchaseDate: String = "2024-03-15"
) {
    val investedValue: Double get() = quantity * buyPrice
    val currentValue: Double get() = quantity * currentPrice
    val totalProfitLoss: Double get() = currentValue - investedValue
    val profitLossPercent: Double get() = if (investedValue > 0) (totalProfitLoss / investedValue) * 100 else 0.0
    val isProfitable: Boolean get() = totalProfitLoss >= 0
}

data class PortfolioTransaction(
    val id: Long = 0,
    val symbol: String,
    val name: String,
    val type: String, // "BUY" or "SELL"
    val assetType: AssetType,
    val quantity: Double,
    val price: Double,
    val date: String
)

data class MarketSentiment(
    val fearGreedIndex: Int = 68,
    val sentiment: String = "Greed",
    val advanceCount: Int = 1420,
    val declineCount: Int = 890,
    val vixValue: Double = 13.4,
    val marketStatus: String = "Live Trading Session"
)

data class GuideSection(
    val title: String,
    val content: String,
    val tip: String
)

data class GuideChapter(
    val id: Int,
    val stepNumber: String,
    val title: String,
    val subtitle: String,
    val durationMinutes: Int,
    val keyConcepts: List<String>,
    val sections: List<GuideSection>
)

data class CandleStick(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
) {
    val isBullish: Boolean get() = close >= open
}

data class TechnicalIndicators(
    val rsi: Double,
    val macdLine: Double,
    val signalLine: Double,
    val macdHist: Double,
    val bollingerUpper: Double,
    val bollingerMiddle: Double,
    val bollingerLower: Double,
    val ema20: Double,
    val dma50: Double,
    val dma200: Double,
    val vwap: Double,
    val pivot: Double,
    val r1: Double,
    val r2: Double,
    val s1: Double,
    val s2: Double,
    val bullishCount: Int = 14,
    val bearishCount: Int = 2,
    val neutralCount: Int = 4,
    val overallSignal: String = "Strong Buy"
)

data class FoOptionStrike(
    val strikePrice: Double,
    val callLtp: Double,
    val callChangePercent: Double,
    val callOi: Long,
    val callIv: Double,
    val putLtp: Double,
    val putChangePercent: Double,
    val putOi: Long,
    val putIv: Double,
    val isAtm: Boolean = false
)

data class FoDetails(
    val futurePrice: Double,
    val basis: Double,
    val rolloverPercent: Double,
    val lotSize: Int,
    val pcr: Double,
    val pcrInterpretation: String,
    val maxPain: Double,
    val atmStrike: Double,
    val strikes: List<FoOptionStrike>
)

data class RealTimeAlert(
    val id: Long = 0,
    val symbol: String,
    val targetPrice: Double,
    val condition: String, // "ABOVE" or "BELOW"
    val createdPrice: Double,
    val createdDate: String,
    val isTriggered: Boolean = false,
    val triggeredDate: String? = null,
    val note: String = ""
)
