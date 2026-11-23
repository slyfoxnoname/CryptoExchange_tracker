package com.example.cryptoexchange.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Локальна сутність Room для офлайн-збереження криптовалют.
 * Первинний ключ [id] — стабільний ідентифікатор активу CoinGecko (напр. "bitcoin").
 */
@Entity(tableName = "crypto_currencies")
data class CryptoCurrencyEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "rank")
    val rank: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "symbol")
    val symbol: String,
    @ColumnInfo(name = "logo_url")
    val logoUrl: String?,
    @ColumnInfo(name = "price_usd")
    val priceUsd: Double?,
    @ColumnInfo(name = "change_percent_24h")
    val changePercent24h: Double?,
    @ColumnInfo(name = "market_cap_usd")
    val marketCapUsd: Double?,
    @ColumnInfo(name = "volume_24h_usd")
    val volume24hUsd: Double?,
    @ColumnInfo(name = "circulating_supply")
    val circulatingSupply: Double?,
    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)
