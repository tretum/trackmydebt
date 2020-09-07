package com.mmutert.trackmydebt.util

import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime

object TimeHelper {

    val currentTimeLocalized: LocalTime
        get() = LocalTime.now(DateTimeZone.getDefault())

    @JvmStatic
    val currentDateLocalized: LocalDate
        get() = LocalDate.now(DateTimeZone.getDefault())
    val currentDateTimeLocalized: LocalDateTime
        get() = LocalDateTime.now(DateTimeZone.getDefault())
}