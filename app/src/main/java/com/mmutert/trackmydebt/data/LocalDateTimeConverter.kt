package com.mmutert.trackmydebt.data

import androidx.room.TypeConverter
import org.joda.time.LocalDateTime

class LocalDateTimeConverter() {
    @TypeConverter
    fun toDate(dateLong: String?): LocalDateTime? {
        return if (dateLong == null) null else LocalDateTime.parse(dateLong)
    }

    @TypeConverter
    fun fromDate(date: LocalDateTime?): String? {
        return date?.toString()
    }
}