package com.example.cryptoexchange.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

/**
 * Інкапсулює планування та спостереження за задачами WorkManager,
 * ізолюючи їх від Composable-компонентів.
 */
class CryptoWorkScheduler(context: Context) {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * Реєструє єдине періодичне фонове оновлення раз на 1 годину.
     * Політика KEEP гарантує відсутність дублікатів при повторному відкритті чи повороті.
     */
    fun schedulePeriodicSync() {
        val periodicRequest = PeriodicWorkRequestBuilder<CryptoSyncWorker>(
            REPEAT_INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setConstraints(networkConstraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_SECONDS,
                TimeUnit.SECONDS
            )
            .addTag(TAG_PERIODIC_SYNC)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    /**
     * Запускає одноразове ручне оновлення.
     * Політика KEEP запобігає дублюванню, якщо оновлення вже виконується або чекає на мережу.
     */
    fun enqueueManualSync() {
        val manualRequest = OneTimeWorkRequestBuilder<CryptoSyncWorker>()
            .setConstraints(networkConstraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_SECONDS,
                TimeUnit.SECONDS
            )
            .addTag(TAG_MANUAL_SYNC)
            .build()

        workManager.enqueueUniqueWork(
            MANUAL_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            manualRequest
        )
    }

    fun observeManualSyncWorkInfo(): Flow<WorkInfo?> =
        workManager.getWorkInfosForUniqueWorkFlow(MANUAL_WORK_NAME)
            .map { infos -> infos.firstOrNull() }

    fun observePeriodicSyncWorkInfo(): Flow<WorkInfo?> =
        workManager.getWorkInfosForUniqueWorkFlow(PERIODIC_WORK_NAME)
            .map { infos -> infos.firstOrNull() }

    companion object {
        const val PERIODIC_WORK_NAME = "crypto_periodic_sync_work"
        const val MANUAL_WORK_NAME = "crypto_manual_sync_work"
        const val TAG_PERIODIC_SYNC = "tag_crypto_periodic_sync"
        const val TAG_MANUAL_SYNC = "tag_crypto_manual_sync"
        const val REPEAT_INTERVAL_HOURS = 1L
        const val BACKOFF_DELAY_SECONDS = 30L
    }
}
