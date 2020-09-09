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

    /**
     * Shows whether the user received value or gave value to another person.
     */
    val received: Boolean,

    /**
     * The amount that got transferred in the transaction.
     * This value is always positive, as it represents the absolute value of the transaction.
     */
    val amount: BigDecimal,

    /**
     * The date and time of the transaction.
     */
    val date: LocalDateTime,

    /**
     * The action that occurred.
     */
    val action: TransactionAction,

    /**
     * The short reason or title of the transaction. Used to recognize transactions with a glance
     */
    val reason: String,

    /**
     * A long reason that is only visualized in detailed views of the transaction and can contain long notes.
     */
    @ColumnInfo(name = "reason_long")
    val reasonLong: String
)