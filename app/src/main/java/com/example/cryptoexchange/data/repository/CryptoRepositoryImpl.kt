package com.example.cryptoexchange.data.repository

import com.example.cryptoexchange.data.local.dao.CryptoDao
import com.example.cryptoexchange.data.mapper.toDomain
import com.example.cryptoexchange.data.mapper.toEntity
import com.example.cryptoexchange.data.remote.api.CoinGeckoApi
import com.example.cryptoexchange.domain.model.CryptoCurrency
import com.example.cryptoexchange.domain.model.SyncResult
import com.example.cryptoexchange.domain.repository.CryptoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Реалізація репозиторію на базі CoinGecko API та Room:
 * - Єдине джерело даних для UI — локальна БД Room (`observeTopCryptos`).
 * - Під час помилки або некоректної відповіді попередні записи у Room не очищуються.
 * - Час оновлення оновлюється виключно разом із транзакційним записом нових даних.
 */
class CryptoRepositoryImpl(
    private val api: CoinGeckoApi,
    private val dao: CryptoDao,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : CryptoRepository {

    override fun observeTopCryptos(): Flow<List<CryptoCurrency>> =
        dao.observeAllOrderedByRank().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeLastSyncTime(): Flow<Long?> =
        dao.observeLastUpdatedAt()

    override suspend fun toggleFavorite(assetId: String) {
        dao.toggleFavorite(assetId)
    }

    override suspend fun syncTopCryptos(): SyncResult {
        val response = try {
            api.getTopMarkets(perPage = CoinGeckoApi.TOP_LIMIT, page = 1)
        } catch (e: IOException) {
            return SyncResult.TransientError(
                message = "Немає з'єднання з сервером. Перевірте Інтернет і спробуйте пізніше."
            )
        } catch (e: Exception) {
            return SyncResult.TransientError(
                message = "Помилка мережевого запиту: ${e.localizedMessage ?: "невідома помилка"}"
            )
        }

        if (!response.isSuccessful) {
            val retryAfter = response.headers()["Retry-After"]?.toLongOrNull()
            return when (response.code()) {
                401, 403 -> SyncResult.AuthError(
                    message = "Помилка доступу до CoinGecko API (HTTP ${response.code()}). Перевірте COINGECKO_API_KEY у local.properties."
                )
                429 -> SyncResult.RateLimitError(
                    message = "Перевищено ліміт запитів CoinGecko (HTTP 429). Зачекайте хвилину або додайте безкоштовний Demo API key.",
                    retryAfterSeconds = retryAfter
                )
                in 500..599 -> SyncResult.TransientError(
                    message = "Сервер CoinGecko тимчасово недоступний (HTTP ${response.code()})."
                )
                else -> SyncResult.InvalidDataError(
                    message = "Несподівана відповідь сервера (HTTP ${response.code()})."
                )
            }
        }

        val rawList = response.body()
        if (rawList.isNullOrEmpty()) {
            return SyncResult.InvalidDataError(
                message = "Сервер повернув порожній список криптовалют."
            )
        }

        val syncedAt = clock()
        val entities = rawList
            .take(CoinGeckoApi.TOP_LIMIT)
            .mapIndexedNotNull { index, dto ->
                dto.toEntity(fallbackRank = index + 1, syncedAtMillis = syncedAt)
            }
            .distinctBy { it.id }

        if (entities.isEmpty()) {
            return SyncResult.InvalidDataError(
                message = "Не вдалося розпізнати жодного валідного активу з відповіді API."
            )
        }

        dao.replaceTopListAtomically(entities)
        return SyncResult.Success(
            syncedCount = entities.size,
            syncedAtMillis = syncedAt
        )
    }
}
