package com.mmutert.trackmydebt

import com.mmutert.trackmydebt.util.FormatHelper
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class NumberFormatTest {

    @Test
    fun testFormat() {

        var parsed = FormatHelper.parseNumber("123,45")
        assertEquals(BigDecimal("123.45"), parsed)

        parsed = FormatHelper.parseNumber("123,00")
        assertEquals(BigDecimal("123.00"), parsed)

        parsed = FormatHelper.parseNumber("123")
        assertEquals(BigDecimal(123), parsed)


        val formatter = NumberFormat.getCurrencyInstance()
        formatter.currency = Currency.getInstance(Locale.GERMANY)

        val format = formatter.format(10020/100.0)
        println(format)

        val parse = formatter.parse(format)!!
        println(parse)
        println((parse.toDouble() * 100).toLong())
    }

    @Test
    fun testCurrencyFormat() {
        var printAsCurrency = FormatHelper.printAsCurrency(BigDecimal(100.00))
        assertEquals("100,00 €", printAsCurrency)

        printAsCurrency = FormatHelper.printAsCurrency(BigDecimal("123.45"))
        assertEquals("123,45 €", printAsCurrency)
    }

    @Test
    fun testFormat2() {
        val formatter = NumberFormat.getCurrencyInstance()

        val format = formatter.format(10020/100.0)
        println(format)

        val parse = formatter.parse(format)!!
        println(parse)
        println((parse.toDouble() * 100).toLong())

        val parse2 = formatter.parse("100")!!
        println(parse2)
        println((parse2.toDouble() * 100).toLong())
    }

    @Test
    fun testParse() {

    }
}
