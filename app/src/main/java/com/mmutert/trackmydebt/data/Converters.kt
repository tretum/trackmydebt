package com.mmutert.trackmydebt.data

import androidx.room.TypeConverter
import org.joda.time.LocalDateTime
import java.math.BigDecimal

class Converters() {
    @TypeConverter
    fun toDate(dateLong: String): LocalDateTime? {
        return LocalDateTime.parse(dateLong)
    }

    @TypeConverter
    fun fromDate(date: LocalDateTime): String? {
        return date.toString()
    }

    @TypeConverter
    fun fromLong(value: Long): BigDecimal {
        return BigDecimal(value).divide(BigDecimal(100))
    }

    @TypeConverter
    fun toLong(bigDecimal: BigDecimal): Long {
        return bigDecimal.multiply(BigDecimal(100)).toLong()
    }
}