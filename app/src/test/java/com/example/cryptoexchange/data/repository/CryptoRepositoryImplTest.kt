package com.example.cryptoexchange.data.repository

import com.example.cryptoexchange.data.local.dao.CryptoDao
import com.example.cryptoexchange.data.local.entity.CryptoCurrencyEntity
import com.example.cryptoexchange.data.remote.api.CoinGeckoApi
import com.example.cryptoexchange.data.remote.dto.CoinGeckoMarketDto
import com.example.cryptoexchange.domain.model.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class CryptoRepositoryImplTest {

    @Test
    fun `failed sync preserves existing cached records and timestamp`() = runTest {
        val fakeDao = InMemoryCryptoDao()
        val fakeApi = FakeCoinGeckoApi(
            responses = ArrayDeque(
                listOf(
                    {
                        Response.success(
                            listOf(
                                CoinGeckoMarketDto(id = "bitcoin", symbol = "btc", name = "Bitcoin", currentPrice = 90000.0, marketCapRank = 1),
                                CoinGeckoMarketDto(id = "ethereum", symbol = "eth", name = "Ethereum", currentPrice = 3200.0, marketCapRank = 2)
                            )
                        )
                    },
                    { throw IOException("No internet") }
                )
            )
        )
        var currentTime = 1_000L
        val repository = CryptoRepositoryImpl(
            api = fakeApi,
            dao = fakeDao,
            clock = { currentTime }
        )

        val first = repository.syncTopCryptos()
        assertTrue(first is SyncResult.Success)
        assertEquals(2, repository.observeTopCryptos().first().size)
        assertEquals(1_000L, repository.observeLastSyncTime().first())

        currentTime = 2_000L
        val second = repository.syncTopCryptos()
        assertTrue(second is SyncResult.TransientError)

        val cachedAfterFailure = repository.observeTopCryptos().first()
        assertEquals(2, cachedAfterFailure.size)
        assertEquals(1_000L, repository.observeLastSyncTime().first())
    }

    @Test
    fun `repeated sync updates prices without creating duplicates`() = runTest {
        val fakeDao = InMemoryCryptoDao()
        val fakeApi = FakeCoinGeckoApi(
            responses = ArrayDeque(
                listOf(
                    {
                        Response.success(
                            listOf(CoinGeckoMarketDto(id = "bitcoin", symbol = "btc", name = "Bitcoin", currentPrice = 90000.0, marketCapRank = 1))
                        )
                    },
                    {
                        Response.success(
                            listOf(CoinGeckoMarketDto(id = "bitcoin", symbol = "btc", name = "Bitcoin", currentPrice = 95500.0, marketCapRank = 1))
                        )
                    }
                )
            )
        )
        val repository = CryptoRepositoryImpl(fakeApi, fakeDao, { 5_000L })

        repository.syncTopCryptos()
        repository.syncTopCryptos()

        val list = repository.observeTopCryptos().first()
        assertEquals(1, list.size)
        assertEquals("BTC", list.first().symbol)
        assertEquals(95500.0, list.first().priceUsd!!, 0.001)
    }

    @Test
    fun `new top list replaces old list atomically and preserves favorites`() = runTest {
        val fakeDao = InMemoryCryptoDao()
        val fakeApi = FakeCoinGeckoApi(
            responses = ArrayDeque(
                listOf(
                    {
                        Response.success(
                            listOf(
                                CoinGeckoMarketDto(id = "bitcoin", symbol = "btc", name = "Bitcoin", currentPrice = 90000.0, marketCapRank = 1),
                                CoinGeckoMarketDto(id = "ethereum", symbol = "eth", name = "Ethereum", currentPrice = 3000.0, marketCapRank = 2)
                            )
                        )
                    },
                    {
                        Response.success(
                            listOf(
                                CoinGeckoMarketDto(id = "bitcoin", symbol = "btc", name = "Bitcoin", currentPrice = 91000.0, marketCapRank = 1),
                                CoinGeckoMarketDto(id = "solana", symbol = "sol", name = "Solana", currentPrice = 180.0, marketCapRank = 2)
                            )
                        )
                    }
                )
            )
        )
        val repository = CryptoRepositoryImpl(fakeApi, fakeDao, { 10_000L })

        repository.syncTopCryptos()
        repository.toggleFavorite("bitcoin")
        repository.syncTopCryptos()

        val list = repository.observeTopCryptos().first()
        assertEquals(listOf("bitcoin", "solana"), list.map { it.id })
        assertTrue(list.first { it.id == "bitcoin" }.isFavorite)
    }

    @Test
    fun `missing price and logo in dto map to null instead of zero`() = runTest {
        val fakeDao = InMemoryCryptoDao()
        val fakeApi = FakeCoinGeckoApi(
            responses = ArrayDeque(
                listOf(
                    {
                        Response.success(
                            listOf(
                                CoinGeckoMarketDto(
                                    id = "new-coin",
                                    symbol = "new",
                                    name = "NewCoin",
                                    image = "",
                                    currentPrice = null,
                                    marketCapRank = 30
                                )
                            )
                        )
                    }
                )
            )
        )
        val repository = CryptoRepositoryImpl(fakeApi, fakeDao, { 10_000L })

        val result = repository.syncTopCryptos()
        assertTrue(result is SyncResult.Success)
        val item = repository.observeTopCryptos().first().single()
        assertNull(item.priceUsd)
        assertNull(item.logoUrl)
    }

    @Test
    fun `http 401 returns AuthError and 429 returns RateLimitError`() = runTest {
        val fakeDao = InMemoryCryptoDao()
        val errorBody = "{}".toResponseBody("application/json".toMediaType())
        val fakeApi = FakeCoinGeckoApi(
            responses = ArrayDeque(
                listOf(
                    { Response.error(401, errorBody) },
                    { Response.error(429, errorBody) }
                )
            )
        )
        val repository = CryptoRepositoryImpl(fakeApi, fakeDao)

        assertTrue(repository.syncTopCryptos() is SyncResult.AuthError)
        assertTrue(repository.syncTopCryptos() is SyncResult.RateLimitError)
    }
}

private class FakeCoinGeckoApi(
    private val responses: ArrayDeque<() -> Response<List<CoinGeckoMarketDto>>>
) : CoinGeckoApi {
    override suspend fun getTopMarkets(
        vsCurrency: String,
        order: String,
        perPage: Int,
        page: Int,
        sparkline: Boolean
    ): Response<List<CoinGeckoMarketDto>> = responses.removeFirst().invoke()
}

private class InMemoryCryptoDao : CryptoDao {
    private val store = MutableStateFlow<Map<String, CryptoCurrencyEntity>>(emptyMap())

    override fun observeAllOrderedByRank(): Flow<List<CryptoCurrencyEntity>> =
        store.map { map -> map.values.sortedBy { it.rank } }

    override fun observeLastUpdatedAt(): Flow<Long?> =
        store.map { map -> map.values.maxOfOrNull { it.updatedAtMillis } }

    override suspend fun getFavoriteIds(): List<String> =
        store.value.values.filter { it.isFavorite }.map { it.id }

    override suspend fun toggleFavorite(assetId: String) {
        val current = store.value.toMutableMap()
        val existing = current[assetId] ?: return
        current[assetId] = existing.copy(isFavorite = !existing.isFavorite)
        store.value = current
    }

    override suspend fun upsertAll(items: List<CryptoCurrencyEntity>) {
        val current = store.value.toMutableMap()
        items.forEach { current[it.id] = it }
        store.value = current
    }

    override suspend fun deleteNotIn(activeIds: List<String>) {
        val activeSet = activeIds.toSet()
        store.value = store.value.filterKeys { it in activeSet }
    }
}
