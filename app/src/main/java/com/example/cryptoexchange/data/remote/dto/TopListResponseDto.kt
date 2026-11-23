package com.example.cryptoexchange.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Коренева відповідь ендпоінту CoinDesk Data API:
 * GET https://data-api.coindesk.com/asset/v1/top/list
 */
data class TopListResponseDto(
    @SerializedName("Data")
    val data: TopListDataDto? = null,
    @SerializedName("Err")
    val err: ApiErrorDto? = null
)

data class TopListDataDto(
    @SerializedName("STATS")
    val stats: TopListStatsDto? = null,
    @SerializedName("LIST")
    val list: List<CryptoAssetDto>? = null
)

data class TopListStatsDto(
    @SerializedName("PAGE")
    val page: Int? = null,
    @SerializedName("PAGE_SIZE")
    val pageSize: Int? = null,
    @SerializedName("TOTAL_ASSETS")
    val totalAssets: Int? = null
)

data class CryptoAssetDto(
    @SerializedName("ID")
    val id: Long? = null,
    @SerializedName("SYMBOL")
    val symbol: String? = null,
    @SerializedName("NAME")
    val name: String? = null,
    @SerializedName("LOGO_URL")
    val logoUrl: String? = null,
    @SerializedName("PRICE_USD")
    val priceUsd: Double? = null,
    @SerializedName("CIRCULATING_MKT_CAP_USD")
    val circulatingMarketCapUsd: Double? = null,
    @SerializedName("SPOT_MOVING_24_HOUR_CHANGE_PERCENTAGE_USD")
    val changePercent24hUsd: Double? = null,
    @SerializedName("SPOT_MOVING_24_HOUR_QUOTE_VOLUME_USD")
    val volume24hUsd: Double? = null,
    @SerializedName("SUPPLY_CIRCULATING")
    val circulatingSupply: Double? = null
)

data class ApiErrorDto(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("type")
    val type: Int? = null
)
