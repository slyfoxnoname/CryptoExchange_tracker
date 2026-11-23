package com.example.cryptoexchange.domain.model

/**
 * Модель предметної області для криптовалюти.
 * Не залежить від DTO мережевого API чи Entity бази даних Room.
 */
data class CryptoCurrency(
    val id: String,
    val rank: Int,
    val name: String,
    val symbol: String,
    val logoUrl: String?,
    val priceUsd: Double?,
    val changePercent24h: Double?,
    val marketCapUsd: Double?,
    val volume24hUsd: Double?,
    val circulatingSupply: Double?,
    val updatedAtMillis: Long,
    val isFavorite: Boolean = false
)
