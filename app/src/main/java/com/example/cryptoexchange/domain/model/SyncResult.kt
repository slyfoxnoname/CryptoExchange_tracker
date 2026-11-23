package com.example.cryptoexchange.domain.model

/**
 * Результат спроби синхронізації курсу криптовалют.
 * Дозволяє WorkManager розрізняти тимчасові мережеві збої, ліміти API та помилки ключа доступу.
 */
sealed interface SyncResult {
    data class Success(
        val syncedCount: Int,
        val syncedAtMillis: Long
    ) : SyncResult

    data class AuthError(
        val message: String
    ) : SyncResult

    data class RateLimitError(
        val message: String,
        val retryAfterSeconds: Long? = null
    ) : SyncResult

    data class TransientError(
        val message: String
    ) : SyncResult

    data class InvalidDataError(
        val message: String
    ) : SyncResult
}
