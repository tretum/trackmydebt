package com.mmutert.trackmydebt.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mmutert.trackmydebt.TransactionAction
import org.joda.time.LocalDateTime
import java.math.BigDecimal

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["partner_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )],
    indices = [Index(name = "PartnerIdIndex", value = ["partner_id"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "partner_id")
    val partnerId: Long,

    val received: Boolean,

    val amount: BigDecimal,

    val date: LocalDateTime,

    val action: TransactionAction,

    val reason: String,

    @ColumnInfo(name = "reason_long")
    val reasonLong: String
)