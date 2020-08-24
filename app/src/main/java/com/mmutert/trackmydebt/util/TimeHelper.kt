package com.mmutert.trackmydebt.util

import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

object TimeHelper {

    @JvmStatic
    val currentDateLocalized: LocalDate
        get() = LocalDate.now(DateTimeZone.getDefault())
    val currentDateTimeLocalized: LocalDateTime
        get() = LocalDateTime.now(DateTimeZone.getDefault())
}