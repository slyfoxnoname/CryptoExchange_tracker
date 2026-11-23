package com.example.cryptoexchange.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.example.cryptoexchange.domain.model.CryptoCurrency
import com.example.cryptoexchange.presentation.ui.theme.FavoriteGold
import com.example.cryptoexchange.presentation.ui.theme.NegativeRed
import com.example.cryptoexchange.presentation.ui.theme.NegativeRedDark
import com.example.cryptoexchange.presentation.ui.theme.PositiveGreen
import com.example.cryptoexchange.presentation.ui.theme.PositiveGreenDark
import com.example.cryptoexchange.presentation.util.PriceFormatter

@Composable
fun CryptoListItem(
    crypto: CryptoCurrency,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val changeText = PriceFormatter.formatChangePercent(crypto.changePercent24h)
    val changeColor = when {
        crypto.changePercent24h == null -> MaterialTheme.colorScheme.onSurfaceVariant
        crypto.changePercent24h >= 0 -> if (isDark) PositiveGreenDark else PositiveGreen
        else -> if (isDark) NegativeRedDark else NegativeRed
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Місце в рейтингу
            Text(
                text = "#${crypto.rank}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(min = 28.dp)
            )

            // Логотип криптовалюти через Coil 3 із заглушкою під час завантаження та офлайн
            CryptoLogo(
                logoUrl = crypto.logoUrl,
                symbol = crypto.symbol,
                contentDescription = crypto.name,
                modifier = Modifier.size(40.dp)
            )

            // Назва та тикер
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = crypto.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = crypto.symbol,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Ціна в USD та зміна за 24 год
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = PriceFormatter.formatUsdPrice(crypto.priceUsd),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (changeText != null) {
                    Text(
                        text = changeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = changeColor,
                        maxLines = 1
                    )
                }
            }

            // Кнопка «Обране»
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (crypto.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = if (crypto.isFavorite) "Видалити з обраного" else "Додати до обраного",
                    tint = if (crypto.isFavorite) FavoriteGold else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CryptoLogo(
    logoUrl: String?,
    symbol: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    if (logoUrl.isNullOrBlank()) {
        CryptoLogoPlaceholder(symbol = symbol, modifier = modifier)
    } else {
        SubcomposeAsyncImage(
            model = logoUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape),
            loading = {
                CryptoLogoPlaceholder(symbol = symbol, modifier = Modifier.fillMaxSize())
            },
            error = {
                CryptoLogoPlaceholder(symbol = symbol, modifier = Modifier.fillMaxSize())
            }
        )
    }
}

@Composable
private fun CryptoLogoPlaceholder(
    symbol: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol.take(3).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1
        )
    }
}
