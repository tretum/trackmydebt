package com.mmutert.trackmydebt.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object FormatHelper {

    fun printAsCurrency(amount: BigDecimal, locale: Locale = Locale.getDefault()) : String {
        return NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 2
            currency = Currency.getInstance(locale)
        }.format(amount)
    }

    fun printAsFloat(amount: BigDecimal) : String {
        return NumberFormat.getNumberInstance(Locale.ROOT).apply {
            maximumFractionDigits = 2
            isGroupingUsed = false
            roundingMode = RoundingMode.HALF_UP
        }.format(amount)
    }

    fun parseNumber(input: String) : BigDecimal {
        val formatter = NumberFormat.getNumberInstance(Locale.ROOT).apply {

        }
        return if(formatter is DecimalFormat) {
            formatter.isParseBigDecimal = true
            formatter.parse(input) as BigDecimal
        } else {
            BigDecimal(0)
        }
    }
}