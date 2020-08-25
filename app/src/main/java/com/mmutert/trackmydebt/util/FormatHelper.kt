package com.mmutert.trackmydebt.util

import java.text.NumberFormat

object FormatHelper {

    val currencyFormatter : NumberFormat = NumberFormat.getInstance()

    fun printAsCurrency(amountInCents: Long) : String {
        return currencyFormatter.format(amountInCents / 100.0)
    }

    init {
        with(currencyFormatter) {
            maximumIntegerDigits = 2
            minimumFractionDigits = 2
        }
    }
}