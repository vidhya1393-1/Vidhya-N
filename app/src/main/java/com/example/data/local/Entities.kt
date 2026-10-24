package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_holdings")
data class HoldingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val name: String,
    val assetType: String,
    val quantity: Double,
    val buyPrice: Double,
    val purchaseDate: String
)

@Entity(tableName = "portfolio_transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val name: String,
    val type: String,
    val assetType: String,
    val quantity: Double,
    val price: Double,
    val date: String
)

@Entity(tableName = "watchlist_items")
data class WatchlistEntity(
    @PrimaryKey val symbol: String
)

@Entity(tableName = "price_alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val targetPrice: Double,
    val condition: String, // "ABOVE" or "BELOW"
    val createdPrice: Double,
    val createdDate: String,
    val isTriggered: Boolean = false,
    val triggeredDate: String? = null,
    val note: String = ""
)
