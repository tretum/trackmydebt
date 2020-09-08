package com.mmutert.trackmydebt.data

import androidx.room.TypeConverter
import com.mmutert.trackmydebt.TransactionAction
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

    @TypeConverter
    fun fromTransactionAction(unit: TransactionAction): String {
        return unit.toString()
    }

    @TypeConverter
    fun toTransactionAction(unit: String): TransactionAction {
        return TransactionAction.valueOf(unit)
    }
}