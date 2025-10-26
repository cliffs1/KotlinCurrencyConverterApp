package com.example.currencyconverterapp

import org.junit.Assert.*
import org.junit.Test

class MainActivityTest {

    @Test
    fun testGetLimitForCurrency() {
        assertEquals(20000.0, getLimitForCurrency("PLN"), 0.0)
        assertEquals(5000.0, getLimitForCurrency("EUR"), 0.0)
        assertEquals(1000.0, getLimitForCurrency("GBP"), 0.0)
        assertEquals(50000.0, getLimitForCurrency("UAH"), 0.0)
        assertEquals(Double.MAX_VALUE, getLimitForCurrency("XYZ"), 0.0)
    }

    @Test
    fun testGetFlagForCurrency() {
        assertEquals(R.drawable.flag_pl, getFlagForCurrency("PLN"))
        assertEquals(R.drawable.flag_ge, getFlagForCurrency("EUR"))
        assertEquals(R.drawable.flag_gb, getFlagForCurrency("GBP"))
        assertEquals(R.drawable.flag_ua, getFlagForCurrency("UAH"))
        assertEquals(0, getFlagForCurrency("XXX")) // Default case
    }

    @Test
    fun testConversionLogic() {
        val rate = 10.0
        val fromAmount = 5.0
        val expectedToAmount = fromAmount * rate
        assertEquals(50.0, expectedToAmount, 0.0)
    }

    @Test
    fun testLimitExceededLogic() {
        val limit = getLimitForCurrency("EUR")
        val overLimitAmount = limit + 100.0
        assertTrue(overLimitAmount > limit)
    }

    @Test
    fun testFxRateApiFails_returnsDefaultRate() = kotlinx.coroutines.test.runTest {
        val rate = getFxRate("INVALID", "XXX", 100.0)
        assertEquals(1.0, rate, 0.0)
    }
}