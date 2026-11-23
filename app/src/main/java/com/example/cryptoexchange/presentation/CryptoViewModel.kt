package com.example.cryptoexchange.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.cryptoexchange.di.AppContainer
import com.example.cryptoexchange.domain.model.CryptoCurrency
import com.example.cryptoexchange.domain.usecase.ObserveCryptosUseCase
import com.example.cryptoexchange.domain.usecase.ObserveLastSyncTimeUseCase
import com.example.cryptoexchange.domain.usecase.ToggleFavoriteUseCase
import com.example.cryptoexchange.worker.CryptoSyncWorker
import com.example.cryptoexchange.worker.CryptoWorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CryptoViewModel(
    private val observeCryptosUseCase: ObserveCryptosUseCase,
    private val observeLastSyncTimeUseCase: ObserveLastSyncTimeUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val workScheduler: CryptoWorkScheduler
) : ViewModel() {

    private val searchQueryFlow = MutableStateFlow("")
    private val showFavoritesOnlyFlow = MutableStateFlow(false)
    private val selectedCryptoIdFlow = MutableStateFlow<String?>(null)
    private val dismissedErrorWorkIdFlow = MutableStateFlow<String?>(null)

    private data class FilterState(
        val query: String,
        val favoritesOnly: Boolean,
        val selectedId: String?,
        val dismissedErrorId: String?
    )

    private val filterStateFlow = combine(
        searchQueryFlow,
        showFavoritesOnlyFlow,
        selectedCryptoIdFlow,
        dismissedErrorWorkIdFlow
    ) { query, favoritesOnly, selectedId, dismissedErrorId ->
        FilterState(query, favoritesOnly, selectedId, dismissedErrorId)
    }

    val uiState: StateFlow<CryptoUiState> = combine(
        observeCryptosUseCase(),
        observeLastSyncTimeUseCase(),
        workScheduler.observeManualSyncWorkInfo(),
        workScheduler.observePeriodicSyncWorkInfo(),
        filterStateFlow
    ) { cryptos, lastSyncTime, manualWorkInfo, periodicWorkInfo, filter ->
        val filtered = filterCryptos(
            cryptos = cryptos,
            query = filter.query,
            favoritesOnly = filter.favoritesOnly
        )
        val syncStatus = resolveSyncStatus(
            manualWorkInfo = manualWorkInfo,
            periodicWorkInfo = periodicWorkInfo,
            dismissedErrorId = filter.dismissedErrorId
        )
        val selected = filter.selectedId?.let { id -> cryptos.firstOrNull { it.id == id } }

        CryptoUiState(
            allCryptos = cryptos,
            visibleCryptos = filtered,
            searchQuery = filter.query,
            showFavoritesOnly = filter.favoritesOnly,
            lastUpdatedAtMillis = lastSyncTime,
            syncStatus = syncStatus,
            selectedCrypto = selected,
            hasLoadedInitialCache = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CryptoUiState()
    )

    init {
        triggerInitialSyncIfCacheEmpty()
    }

    private fun triggerInitialSyncIfCacheEmpty() {
        viewModelScope.launch {
            val cached = observeCryptosUseCase().first()
            if (cached.isEmpty()) {
                workScheduler.enqueueManualSync()
            }
        }
    }

    fun onRefreshClick() {
        dismissedErrorWorkIdFlow.value = null
        workScheduler.enqueueManualSync()
    }

    fun onSearchQueryChange(query: String) {
        searchQueryFlow.value = query
    }

    fun onToggleFavoritesFilter() {
        showFavoritesOnlyFlow.value = !showFavoritesOnlyFlow.value
    }

    fun onToggleFavorite(assetId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(assetId)
        }
    }

    fun onSelectCrypto(assetId: String) {
        selectedCryptoIdFlow.value = assetId
    }

    fun onDismissDetail() {
        selectedCryptoIdFlow.value = null
    }

    fun onDismissError(workId: String? = null) {
        dismissedErrorWorkIdFlow.value = workId ?: "dismissed"
    }

    private fun filterCryptos(
        cryptos: List<CryptoCurrency>,
        query: String,
        favoritesOnly: Boolean
    ): List<CryptoCurrency> {
        val normalizedQuery = query.trim().lowercase()
        return cryptos.filter { crypto ->
            val matchesFavorite = !favoritesOnly || crypto.isFavorite
            val matchesQuery = normalizedQuery.isEmpty() ||
                crypto.name.lowercase().contains(normalizedQuery) ||
                crypto.symbol.lowercase().contains(normalizedQuery)
            matchesFavorite && matchesQuery
        }
    }

    private fun resolveSyncStatus(
        manualWorkInfo: WorkInfo?,
        periodicWorkInfo: WorkInfo?,
        dismissedErrorId: String?
    ): SyncStatus {
        if (manualWorkInfo?.state == WorkInfo.State.RUNNING ||
            periodicWorkInfo?.state == WorkInfo.State.RUNNING
        ) {
            return SyncStatus.Syncing
        }

        if (manualWorkInfo?.state == WorkInfo.State.ENQUEUED ||
            manualWorkInfo?.state == WorkInfo.State.BLOCKED
        ) {
            return SyncStatus.WaitingForNetwork
        }

        if (manualWorkInfo?.state == WorkInfo.State.FAILED) {
            val workId = manualWorkInfo.id.toString()
            if (dismissedErrorId != workId && dismissedErrorId != "dismissed") {
                val message = manualWorkInfo.outputData
                    .getString(CryptoSyncWorker.KEY_ERROR_MESSAGE)
                    ?: "Не вдалося оновити дані. Перевірте підключення до мережі."
                val isAuth = manualWorkInfo.outputData
                    .getString(CryptoSyncWorker.KEY_ERROR_TYPE) == CryptoSyncWorker.ERROR_TYPE_AUTH
                return SyncStatus.Error(message = message, isAuthError = isAuth)
            }
        }

        return SyncStatus.Idle
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CryptoViewModel(
                        observeCryptosUseCase = container.observeCryptosUseCase,
                        observeLastSyncTimeUseCase = container.observeLastSyncTimeUseCase,
                        toggleFavoriteUseCase = container.toggleFavoriteUseCase,
                        workScheduler = container.workScheduler
                    ) as T
                }
            }
    }
}
