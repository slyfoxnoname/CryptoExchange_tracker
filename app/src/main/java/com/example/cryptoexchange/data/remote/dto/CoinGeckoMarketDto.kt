package com.example.cryptoexchange.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO елемента масиву з CoinGecko API:
 * GET https://api.coingecko.com/api/v3/coins/markets
 */
data class CoinGeckoMarketDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("symbol")
    val symbol: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("current_price")
    val currentPrice: Double? = null,
    @SerializedName("market_cap")
    val marketCap: Double? = null,
    @SerializedName("market_cap_rank")
    val marketCapRank: Int? = null,
    @SerializedName("total_volume")
    val totalVolume: Double? = null,
    @SerializedName("price_change_percentage_24h")
    val priceChangePercentage24h: Double? = null,
    @SerializedName("circulating_supply")
    val circulatingSupply: Double? = null
)
