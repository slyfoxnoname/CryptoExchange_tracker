package com.example.cryptoexchange.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Додає необов'язковий заголовок `x-cg-demo-api-key`, якщо ключ задано в local.properties.
 * Без ключа запити виконуються через публічний безкоштовний CoinGecko API.
 */
class AuthInterceptor(
    private val apiKeyProvider: () -> String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = apiKeyProvider().trim()
        val requestBuilder = chain.request().newBuilder()
            .header("Accept", "application/json")
        if (apiKey.isNotEmpty()) {
            requestBuilder.header(HEADER_DEMO_API_KEY, apiKey)
        }
        return chain.proceed(requestBuilder.build())
    }

    companion object {
        const val HEADER_DEMO_API_KEY = "x-cg-demo-api-key"
    }
}
