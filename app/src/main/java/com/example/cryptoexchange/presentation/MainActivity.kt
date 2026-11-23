package com.example.cryptoexchange.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptoexchange.CryptoExchangeApp
import com.example.cryptoexchange.presentation.ui.CryptoExchangeRoute
import com.example.cryptoexchange.presentation.ui.theme.CryptoExchangeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as CryptoExchangeApp).appContainer

        setContent {
            CryptoExchangeTheme {
                val viewModel: CryptoViewModel = viewModel(
                    factory = CryptoViewModel.provideFactory(appContainer)
                )
                CryptoExchangeRoute(viewModel = viewModel)
            }
        }
    }
}
