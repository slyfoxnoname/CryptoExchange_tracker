package com.example.cryptoexchange.presentation.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateFormatter {
    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    fun formatTimestamp(epochMillis: Long?): String {
        if (epochMillis == null || epochMillis <= 0L) return "Ще не оновлювалося"
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
}
