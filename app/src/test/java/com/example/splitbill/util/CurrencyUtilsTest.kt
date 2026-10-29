package com.example.splitbill.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyUtilsTest {

    @Test
    fun testFormatVnd() {
        val result = CurrencyUtils.formatVnd(150000.0)
        assertTrue(result.contains("150") && result.contains("₫"))
    }

    @Test
    fun testFormatSignedVnd() {
        val positive = CurrencyUtils.formatSignedVnd(200000.0)
        assertTrue(positive.startsWith("+"))

        val negative = CurrencyUtils.formatSignedVnd(-100000.0)
        assertTrue(negative.startsWith("-"))

        val zero = CurrencyUtils.formatSignedVnd(0.0)
        assertEquals("0 ₫", zero)
    }

    @Test
    fun testParseAmount() {
        assertEquals(150000.0, CurrencyUtils.parseAmount("150.000"), 0.01)
        assertEquals(250000.0, CurrencyUtils.parseAmount("250000"), 0.01)
        assertEquals(1000000.0, CurrencyUtils.parseAmount("1,000,000"), 0.01)
    }
}
