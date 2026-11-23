package com.example.cryptoexchange.presentation

import com.example.cryptoexchange.domain.model.CryptoCurrency

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object WaitingForNetwork : SyncStatus
    data object Syncing : SyncStatus
    data class Error(
        val message: String,
        val isAuthError: Boolean = false
    ) : SyncStatus
}

data class CryptoUiState(
    val allCryptos: List<CryptoCurrency> = emptyList(),
    val visibleCryptos: List<CryptoCurrency> = emptyList(),
    val searchQuery: String = "",
    val showFavoritesOnly: Boolean = false,
    val lastUpdatedAtMillis: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.Idle,
    val selectedCrypto: CryptoCurrency? = null,
    val hasLoadedInitialCache: Boolean = false
) {
    val isSyncing: Boolean
        get() = syncStatus is SyncStatus.Syncing
}
