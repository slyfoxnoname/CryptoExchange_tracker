package com.example.cryptoexchange

import android.app.Application
import com.example.cryptoexchange.di.AppContainer

class CryptoExchangeApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        // Реєструємо періодичну синхронізацію (1 год) один раз на рівні Application
        appContainer.workScheduler.schedulePeriodicSync()
    }
}
