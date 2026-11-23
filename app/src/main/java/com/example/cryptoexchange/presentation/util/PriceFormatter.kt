package com.example.cryptoexchange.presentation.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Форматування цін у USD з адаптивною точністю:
 * - null -> «Немає даних» (ніколи не 0 чи $0.00)
 * - Малі ціни (наприклад, $0.00001845) зберігають значущі розряди й не округлюються до $0.00.
 */
object PriceFormatter {

    const val NO_DATA = "Немає даних"

    private val usSymbols = DecimalFormatSymbols(Locale.US)

    fun formatUsdPrice(price: Double?): String {
        if (price == null || !price.isFinite() || price <= 0.0) {
            return NO_DATA
        }

        return when {
            price >= 1_000.0 -> {
                val df = DecimalFormat("$#,##0.00", usSymbols)
                df.format(price)
            }
            price >= 1.0 -> {
                val df = DecimalFormat("$#,##0.00", usSymbols)
                df.format(price)
            }
            price >= 0.01 -> {
                val df = DecimalFormat("$0.0000", usSymbols)
                df.format(price)
            }
            price >= 0.0001 -> {
                val df = DecimalFormat("$0.000000", usSymbols)
                df.format(price)
            }
            else -> {
                // Для надмалих токенів показуємо до 4 значущих цифр без наукової нотації
                val bd = BigDecimal.valueOf(price)
                val significantScale = (-bd.scale() + bd.precision() - 1).let { exp ->
                    (-exp + 3).coerceIn(6, 12)
                }
                val rounded = bd.setScale(significantScale, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
                "\$$rounded"
            }
        }
    }

    fun formatChangePercent(change: Double?): String? {
        if (change == null || !change.isFinite()) return null
        val sign = if (change > 0) "+" else ""
        val df = DecimalFormat("0.00", usSymbols)
        return "$sign${df.format(change)}%"
    }

    fun formatCompactUsd(amount: Double?): String {
        if (amount == null || !amount.isFinite() || amount <= 0.0) return NO_DATA
        val df = DecimalFormat("#,##0.00", usSymbols)
        return when {
            amount >= 1_000_000_000_000.0 -> "$${df.format(amount / 1_000_000_000_000.0)} T"
            amount >= 1_000_000_000.0 -> "$${df.format(amount / 1_000_000_000.0)} B"
            amount >= 1_000_000.0 -> "$${df.format(amount / 1_000_000.0)} M"
            amount >= 1_000.0 -> "$${df.format(amount / 1_000.0)} K"
            else -> "$${df.format(amount)}"
        }
    }

    fun formatSupply(supply: Double?, symbol: String): String {
        if (supply == null || !supply.isFinite() || supply <= 0.0) return NO_DATA
        val df = DecimalFormat("#,##0.##", usSymbols)
        val formatted = when {
            supply >= 1_000_000_000.0 -> "${df.format(supply / 1_000_000_000.0)} B"
            supply >= 1_000_000.0 -> "${df.format(supply / 1_000_000.0)} M"
            else -> df.format(supply)
        }
        return "$formatted $symbol"
    }
}
