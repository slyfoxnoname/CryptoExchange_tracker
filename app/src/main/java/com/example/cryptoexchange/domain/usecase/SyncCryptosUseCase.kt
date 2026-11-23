package com.example.cryptoexchange.domain.usecase

import com.example.cryptoexchange.domain.model.SyncResult
import com.example.cryptoexchange.domain.repository.CryptoRepository

class SyncCryptosUseCase(
    private val repository: CryptoRepository
) {
    suspend operator fun invoke(): SyncResult = repository.syncTopCryptos()
}
