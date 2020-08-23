package com.mmutert.trackmydebt.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.joda.time.LocalDateTime

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["partner_id"]
        )]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "partner_id")
    val partnerId: Long,

    val received: Boolean,

    val amount: Long,

    val date: LocalDateTime,

    val reason: String
) {
}