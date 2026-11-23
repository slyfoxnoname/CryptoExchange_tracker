package com.example.cryptoexchange.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.cryptoexchange.data.local.entity.CryptoCurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoDao {

    @Query("SELECT * FROM crypto_currencies ORDER BY rank ASC")
    fun observeAllOrderedByRank(): Flow<List<CryptoCurrencyEntity>>

    @Query("SELECT MAX(updated_at_millis) FROM crypto_currencies")
    fun observeLastUpdatedAt(): Flow<Long?>

    @Query("SELECT id FROM crypto_currencies WHERE is_favorite = 1")
    suspend fun getFavoriteIds(): List<String>

    @Query("UPDATE crypto_currencies SET is_favorite = NOT is_favorite WHERE id = :assetId")
    suspend fun toggleFavorite(assetId: String)

    @Upsert
    suspend fun upsertAll(items: List<CryptoCurrencyEntity>)

    @Query("DELETE FROM crypto_currencies WHERE id NOT IN (:activeIds)")
    suspend fun deleteNotIn(activeIds: List<String>)

    /**
     * Атомарне оновлення рейтингу топ-30 в одній транзакції:
     * 1. Зберігає вибір користувача («Обране»).
     * 2. Видаляє валюти, які вибули з поточного топ-30.
     * 3. Оновлює/вставляє актуальні записи без дублікатів.
     */
    @Transaction
    suspend fun replaceTopListAtomically(newItems: List<CryptoCurrencyEntity>) {
        if (newItems.isEmpty()) return
        val favoriteIds = getFavoriteIds().toSet()
        val mergedItems = newItems.map { item ->
            if (item.id in favoriteIds) item.copy(isFavorite = true) else item
        }
        val activeIds = mergedItems.map { it.id }
        deleteNotIn(activeIds)
        upsertAll(mergedItems)
    }
}
