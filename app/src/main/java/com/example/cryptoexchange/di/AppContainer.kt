package com.example.cryptoexchange.di

import android.content.Context
import com.example.cryptoexchange.BuildConfig
import com.example.cryptoexchange.data.local.db.CryptoDatabase
import com.example.cryptoexchange.data.remote.AuthInterceptor
import com.example.cryptoexchange.data.remote.api.CoinGeckoApi
import com.example.cryptoexchange.data.repository.CryptoRepositoryImpl
import com.example.cryptoexchange.domain.repository.CryptoRepository
import com.example.cryptoexchange.domain.usecase.ObserveCryptosUseCase
import com.example.cryptoexchange.domain.usecase.ObserveLastSyncTimeUseCase
import com.example.cryptoexchange.domain.usecase.SyncCryptosUseCase
import com.example.cryptoexchange.domain.usecase.ToggleFavoriteUseCase
import com.example.cryptoexchange.worker.CryptoWorkScheduler
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Ручний контейнер залежностей застосунку.
 */
class AppContainer(context: Context) {

    private val apiKeyProvider: () -> String = { BuildConfig.COINGECKO_API_KEY }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader(AuthInterceptor.HEADER_DEMO_API_KEY)
        }

        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(apiKeyProvider))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val coinGeckoApi: CoinGeckoApi by lazy {
        Retrofit.Builder()
            .baseUrl(CoinGeckoApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinGeckoApi::class.java)
    }

    private val database: CryptoDatabase by lazy {
        CryptoDatabase.create(context)
    }

    val cryptoRepository: CryptoRepository by lazy {
        CryptoRepositoryImpl(
            api = coinGeckoApi,
            dao = database.cryptoDao()
        )
    }

    val observeCryptosUseCase: ObserveCryptosUseCase by lazy {
        ObserveCryptosUseCase(cryptoRepository)
    }

    val observeLastSyncTimeUseCase: ObserveLastSyncTimeUseCase by lazy {
        ObserveLastSyncTimeUseCase(cryptoRepository)
    }

    val syncCryptosUseCase: SyncCryptosUseCase by lazy {
        SyncCryptosUseCase(cryptoRepository)
    }

    val toggleFavoriteUseCase: ToggleFavoriteUseCase by lazy {
        ToggleFavoriteUseCase(cryptoRepository)
    }

    val workScheduler: CryptoWorkScheduler by lazy {
        CryptoWorkScheduler(context)
    }
}
