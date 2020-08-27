package com.mmutert.trackmydebt.util

import java.text.NumberFormat

object FormatHelper {

    private val currencyFormatter : NumberFormat = NumberFormat.getInstance()

    fun printAsCurrency(amountInCents: Long) : String {
        return currencyFormatter.format(amountInCents / 100.0)
    }

    init {
        with(currencyFormatter) {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
}