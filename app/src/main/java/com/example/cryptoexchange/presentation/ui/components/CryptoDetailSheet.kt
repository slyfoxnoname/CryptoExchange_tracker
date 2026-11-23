package com.example.cryptoexchange.presentation.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cryptoexchange.domain.model.CryptoCurrency
import com.example.cryptoexchange.presentation.ui.theme.FavoriteGold
import com.example.cryptoexchange.presentation.ui.theme.NegativeRed
import com.example.cryptoexchange.presentation.ui.theme.NegativeRedDark
import com.example.cryptoexchange.presentation.ui.theme.PositiveGreen
import com.example.cryptoexchange.presentation.ui.theme.PositiveGreenDark
import com.example.cryptoexchange.presentation.util.DateFormatter
import com.example.cryptoexchange.presentation.util.PriceFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoDetailSheet(
    crypto: CryptoCurrency,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val changeText = PriceFormatter.formatChangePercent(crypto.changePercent24h) ?: PriceFormatter.NO_DATA
    val changeColor = when {
        crypto.changePercent24h == null -> MaterialTheme.colorScheme.onSurfaceVariant
        crypto.changePercent24h >= 0 -> if (isDark) PositiveGreenDark else PositiveGreen
        else -> if (isDark) NegativeRedDark else NegativeRed
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CryptoLogo(
                    logoUrl = crypto.logoUrl,
                    symbol = crypto.symbol,
                    contentDescription = crypto.name,
                    modifier = Modifier.size(48.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = crypto.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${crypto.symbol}  •  Рейтинг #${crypto.rank}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (crypto.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = if (crypto.isFavorite) "Видалити з обраного" else "Додати до обраного",
                        tint = if (crypto.isFavorite) FavoriteGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            MetricRow(
                label = "Ціна (USD)",
                value = PriceFormatter.formatUsdPrice(crypto.priceUsd),
                isEmphasized = true
            )
            MetricRow(
                label = "Зміна за 24 год",
                value = changeText,
                valueColor = changeColor
            )
            MetricRow(
                label = "Ринкова капіталізація",
                value = PriceFormatter.formatCompactUsd(crypto.marketCapUsd)
            )
            MetricRow(
                label = "Обсяг торгів (24 год)",
                value = PriceFormatter.formatCompactUsd(crypto.volume24hUsd)
            )
            MetricRow(
                label = "В обігу",
                value = PriceFormatter.formatSupply(crypto.circulatingSupply, crypto.symbol)
            )
            MetricRow(
                label = "Збережено о",
                value = DateFormatter.formatTimestamp(crypto.updatedAtMillis)
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    isEmphasized: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isEmphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
