package com.example.cryptoexchange.data.remote.api

import com.example.cryptoexchange.data.remote.dto.TopListResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinDeskApi {

    @GET("asset/v1/top/list")
    suspend fun getTopList(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = TOP_LIMIT,
        @Query("sort_by") sortBy: String = "CIRCULATING_MKT_CAP_USD",
        @Query("sort_direction") sortDirection: String = "DESC",
        @Query("toplist_quote_asset") quoteAsset: String = "USD",
        @Query("groups") groups: String = DEFAULT_GROUPS
    ): Response<TopListResponseDto>

    companion object {
        const val BASE_URL = "https://data-api.coindesk.com/"
        const val TOP_LIMIT = 30
        const val DEFAULT_GROUPS = "ID,BASIC,SUPPLY,PRICE,MKT_CAP,VOLUME,CHANGE,TOPLIST_RANK"
    }
}
