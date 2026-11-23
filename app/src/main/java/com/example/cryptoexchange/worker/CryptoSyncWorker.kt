package com.example.cryptoexchange.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.cryptoexchange.CryptoExchangeApp
import com.example.cryptoexchange.domain.model.SyncResult

/**
 * Фоновий Worker для синхронізації топ-30 криптовалют.
 * Виконується без прив'язки до Activity або ViewModel, очікує завершення
 * мережевого запиту та транзакційного запису у Room.
 */
class CryptoSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as CryptoExchangeApp
        val syncUseCase = app.appContainer.syncCryptosUseCase

        return when (val syncResult = syncUseCase()) {
            is SyncResult.Success -> Result.success(
                workDataOf(
                    KEY_SYNCED_COUNT to syncResult.syncedCount,
                    KEY_SYNCED_AT to syncResult.syncedAtMillis
                )
            )

            is SyncResult.AuthError -> {
                // Помилки ключа/доступу не повторюємо нескінченно
                Result.failure(
                    workDataOf(
                        KEY_ERROR_MESSAGE to syncResult.message,
                        KEY_ERROR_TYPE to ERROR_TYPE_AUTH
                    )
                )
            }

            is SyncResult.InvalidDataError -> {
                Result.failure(
                    workDataOf(
                        KEY_ERROR_MESSAGE to syncResult.message,
                        KEY_ERROR_TYPE to ERROR_TYPE_DATA
                    )
                )
            }

            is SyncResult.RateLimitError -> {
                if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.failure(
                        workDataOf(
                            KEY_ERROR_MESSAGE to syncResult.message,
                            KEY_ERROR_TYPE to ERROR_TYPE_RATE_LIMIT
                        )
                    )
                }
            }

            is SyncResult.TransientError -> {
                if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.failure(
                        workDataOf(
                            KEY_ERROR_MESSAGE to syncResult.message,
                            KEY_ERROR_TYPE to ERROR_TYPE_TRANSIENT
                        )
                    )
                }
            }
        }
    }

    companion object {
        const val MAX_RETRY_ATTEMPTS = 3

        const val KEY_SYNCED_COUNT = "synced_count"
        const val KEY_SYNCED_AT = "synced_at"
        const val KEY_ERROR_MESSAGE = "error_message"
        const val KEY_ERROR_TYPE = "error_type"

        const val ERROR_TYPE_AUTH = "auth"
        const val ERROR_TYPE_RATE_LIMIT = "rate_limit"
        const val ERROR_TYPE_TRANSIENT = "transient"
        const val ERROR_TYPE_DATA = "data"
    }
}
