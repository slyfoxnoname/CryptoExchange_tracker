package com.example.cryptoexchange.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceFormatterTest {

    @Test
    fun `null or non-positive price returns no data label`() {
        assertEquals(PriceFormatter.NO_DATA, PriceFormatter.formatUsdPrice(null))
        assertEquals(PriceFormatter.NO_DATA, PriceFormatter.formatUsdPrice(0.0))
        assertEquals(PriceFormatter.NO_DATA, PriceFormatter.formatUsdPrice(-5.0))
    }

    @Test
    fun `small positive prices never round to zero`() {
        val formatted = PriceFormatter.formatUsdPrice(0.00001845)
        assertNotEquals("$0.00", formatted)
        assertTrue(formatted.startsWith("$0.0000184"))
    }

    @Test
    fun `standard prices format with dollar sign and commas`() {
        assertEquals("$95,420.50", PriceFormatter.formatUsdPrice(95420.5))
        assertEquals("$0.1234", PriceFormatter.formatUsdPrice(0.1234))
    }
}
