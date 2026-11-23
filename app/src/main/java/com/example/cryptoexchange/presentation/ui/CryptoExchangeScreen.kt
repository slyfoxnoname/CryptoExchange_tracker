package com.example.cryptoexchange.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cryptoexchange.presentation.CryptoUiState
import com.example.cryptoexchange.presentation.CryptoViewModel
import com.example.cryptoexchange.presentation.SyncStatus
import com.example.cryptoexchange.presentation.ui.components.CryptoDetailSheet
import com.example.cryptoexchange.presentation.ui.components.CryptoListItem
import com.example.cryptoexchange.presentation.ui.components.SyncStatusBanner

@Composable
fun CryptoExchangeRoute(
    viewModel: CryptoViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CryptoExchangeScreen(
        uiState = uiState,
        onRefreshClick = viewModel::onRefreshClick,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onToggleFavoritesFilter = viewModel::onToggleFavoritesFilter,
        onToggleFavorite = viewModel::onToggleFavorite,
        onSelectCrypto = viewModel::onSelectCrypto,
        onDismissDetail = viewModel::onDismissDetail,
        onDismissError = { viewModel.onDismissError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoExchangeScreen(
    uiState: CryptoUiState,
    onRefreshClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleFavoritesFilter: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSelectCrypto: (String) -> Unit,
    onDismissDetail: () -> Unit,
    onDismissError: () -> Unit
) {
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Біржа криптовалют",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Топ-30 за капіталізацією (USD)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefreshClick,
                        enabled = !uiState.isSyncing
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Оновити"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SyncStatusBanner(
                lastUpdatedAtMillis = uiState.lastUpdatedAtMillis,
                syncStatus = uiState.syncStatus,
                onDismissError = onDismissError,
                modifier = Modifier.padding(top = 12.dp)
            )

            if (uiState.allCryptos.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Пошук (BTC, Bitcoin…)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Очистити пошук"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = uiState.showFavoritesOnly,
                        onClick = onToggleFavoritesFilter,
                        label = { Text("Обране") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            when {
                uiState.allCryptos.isEmpty() -> {
                    EmptyInitialState(
                        syncStatus = uiState.syncStatus,
                        onRefreshClick = onRefreshClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                uiState.visibleCryptos.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.showFavoritesOnly) {
                                "У списку обраного немає валют за вашим запитом."
                            } else {
                                "За запитом «${uiState.searchQuery}» нічого не знайдено."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(top = 2.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.visibleCryptos,
                            key = { crypto -> crypto.id }
                        ) { crypto ->
                            CryptoListItem(
                                crypto = crypto,
                                onClick = { onSelectCrypto(crypto.id) },
                                onToggleFavorite = { onToggleFavorite(crypto.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    uiState.selectedCrypto?.let { selected ->
        CryptoDetailSheet(
            crypto = selected,
            onToggleFavorite = { onToggleFavorite(selected.id) },
            onDismiss = onDismissDetail
        )
    }
}

@Composable
private fun EmptyInitialState(
    syncStatus: SyncStatus,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (syncStatus) {
                SyncStatus.Syncing -> {
                    CircularProgressIndicator()
                    Text(
                        text = "Завантажуємо топ-30 криптовалют…",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }

                SyncStatus.WaitingForNetwork -> {
                    Text(
                        text = "Дані ще не завантажено",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Підключіть пристрій до Інтернету — перша синхронізація розпочнеться автоматично, після чого список буде доступний офлайн.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                else -> {
                    Text(
                        text = "Локальний кеш порожній",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Натисніть «Оновити», щоб завантажити актуальні курси 30 криптовалют у USD.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onRefreshClick) {
                        Text("Оновити")
                    }
                }
            }
        }
    }
}
