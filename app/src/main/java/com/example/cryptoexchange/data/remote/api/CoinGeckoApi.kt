package com.example.cryptoexchange.data.remote.api

import com.example.cryptoexchange.data.remote.dto.CoinGeckoMarketDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {

    @GET("coins/markets")
    suspend fun getTopMarkets(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = TOP_LIMIT,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): Response<List<CoinGeckoMarketDto>>

    companion object {
        const val BASE_URL = "https://api.coingecko.com/api/v3/"
        const val TOP_LIMIT = 30
    }
}
