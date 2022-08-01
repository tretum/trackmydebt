package com.mmutert.trackmydebt

import com.mmutert.trackmydebt.util.FormatHelper
import junit.framework.TestCase.assertEquals
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.MatcherAssert.assertThat
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
        assertThat(printAsCurrency).startsWith("100,00").endsWith("€")

        printAsCurrency = FormatHelper.printAsCurrency(BigDecimal("123.45"), Locale.GERMANY)
        assertThat(printAsCurrency).startsWith("123,45").endsWith("€")
    }
}
