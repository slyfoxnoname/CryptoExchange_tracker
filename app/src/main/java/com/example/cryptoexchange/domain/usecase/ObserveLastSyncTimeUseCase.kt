package com.example.cryptoexchange.domain.usecase

import com.example.cryptoexchange.domain.repository.CryptoRepository
import kotlinx.coroutines.flow.Flow

class ObserveLastSyncTimeUseCase(
    private val repository: CryptoRepository
) {
    operator fun invoke(): Flow<Long?> = repository.observeLastSyncTime()
}
