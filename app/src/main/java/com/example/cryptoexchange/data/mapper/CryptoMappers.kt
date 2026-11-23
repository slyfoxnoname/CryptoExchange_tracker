package com.example.cryptoexchange.data.mapper

import com.example.cryptoexchange.data.local.entity.CryptoCurrencyEntity
import com.example.cryptoexchange.data.remote.dto.CoinGeckoMarketDto
import com.example.cryptoexchange.domain.model.CryptoCurrency

/**
 * Перетворює DTO з CoinGecko API в сутність Room.
 * Відсутня ціна (null / NaN / <= 0) зберігається як null (UI покаже «Немає даних»).
 */
fun CoinGeckoMarketDto.toEntity(
    fallbackRank: Int,
    syncedAtMillis: Long
): CryptoCurrencyEntity? {
    val stableId = id?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val normalizedSymbol = symbol?.trim()?.uppercase().orEmpty()
    val normalizedName = name?.trim().orEmpty()
    if (normalizedSymbol.isEmpty() && normalizedName.isEmpty()) return null

    val sanitizedPrice = currentPrice?.takeIf { it.isFinite() && it > 0.0 }
    val sanitizedChange = priceChangePercentage24h?.takeIf { it.isFinite() }
    val sanitizedMarketCap = marketCap?.takeIf { it.isFinite() && it >= 0.0 }
    val sanitizedVolume = totalVolume?.takeIf { it.isFinite() && it >= 0.0 }
    val sanitizedSupply = circulatingSupply?.takeIf { it.isFinite() && it >= 0.0 }
    val sanitizedLogo = image?.trim()?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
    val effectiveRank = marketCapRank?.takeIf { it > 0 } ?: fallbackRank

    return CryptoCurrencyEntity(
        id = stableId,
        rank = effectiveRank,
        name = normalizedName.ifEmpty { normalizedSymbol },
        symbol = normalizedSymbol.ifEmpty { normalizedName },
        logoUrl = sanitizedLogo,
        priceUsd = sanitizedPrice,
        changePercent24h = sanitizedChange,
        marketCapUsd = sanitizedMarketCap,
        volume24hUsd = sanitizedVolume,
        circulatingSupply = sanitizedSupply,
        updatedAtMillis = syncedAtMillis,
        isFavorite = false
    )
}

fun CryptoCurrencyEntity.toDomain(): CryptoCurrency = CryptoCurrency(
    id = id,
    rank = rank,
    name = name,
    symbol = symbol,
    logoUrl = logoUrl,
    priceUsd = priceUsd,
    changePercent24h = changePercent24h,
    marketCapUsd = marketCapUsd,
    volume24hUsd = volume24hUsd,
    circulatingSupply = circulatingSupply,
    updatedAtMillis = updatedAtMillis,
    isFavorite = isFavorite
)
