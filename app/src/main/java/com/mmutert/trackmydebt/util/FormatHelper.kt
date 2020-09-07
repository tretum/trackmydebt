package com.mmutert.trackmydebt.util

import java.lang.NumberFormatException
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object FormatHelper {

    private val currencyFormatter : NumberFormat = NumberFormat.getCurrencyInstance()

    fun printAsCurrency(amount: BigDecimal) : String {
        return currencyFormatter.format(amount)
    }

    fun printAsFloat(amount: BigDecimal) : String {
        return NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 2
        }.format(amount)
    }

    fun parseNumber(input: String) : BigDecimal {
        val formatter = NumberFormat.getNumberInstance().apply {

        }
        return if(formatter is DecimalFormat) {
            formatter.isParseBigDecimal = true
            formatter.parse(input) as BigDecimal
        } else {
            BigDecimal(0)
        }
    }

    init {
        with(currencyFormatter) {
            maximumFractionDigits = 2
            currency = Currency.getInstance(Locale.getDefault())
        }
    }
}