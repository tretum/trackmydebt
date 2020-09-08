package com.mmutert.trackmydebt

import com.mmutert.trackmydebt.util.FormatHelper
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class NumberFormatTest {

    @Test
    fun testFormat() {

        var parsed = FormatHelper.parseNumber("123.45")
        assertEquals(BigDecimal("123.45"), parsed)

        parsed = FormatHelper.parseNumber("123.00")
        assertEquals(BigDecimal("123.00"), parsed)

        parsed = FormatHelper.parseNumber("123")
        assertEquals(BigDecimal(123), parsed)

        val printAsFloat = FormatHelper.printAsFloat(BigDecimal(23410.21))
        assertEquals("23410.21", printAsFloat)
    }

    @Test
    fun testCurrencyFormat() {
        var printAsCurrency = FormatHelper.printAsCurrency(BigDecimal(100.00), Locale.GERMANY)
        assertEquals("100,00 €", printAsCurrency)

        printAsCurrency = FormatHelper.printAsCurrency(BigDecimal("123.45"), Locale.GERMANY)
        assertEquals("123,45 €", printAsCurrency)
    }
}
