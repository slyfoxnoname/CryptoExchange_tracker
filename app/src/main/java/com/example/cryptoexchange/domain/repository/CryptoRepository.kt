package com.example.cryptoexchange.domain.repository

import com.example.cryptoexchange.domain.model.CryptoCurrency
import com.example.cryptoexchange.domain.model.SyncResult
import kotlinx.coroutines.flow.Flow

/**
 * Контракт репозиторію предметної області.
 * UI спостерігає виключно за локальним кешем (Room), а синхронізація оновлює сховище атомарно.
 */
interface CryptoRepository {
    fun observeTopCryptos(): Flow<List<CryptoCurrency>>
    fun observeLastSyncTime(): Flow<Long?>
    suspend fun syncTopCryptos(): SyncResult
    suspend fun toggleFavorite(assetId: String)
}
