package com.mmutert.trackmydebt.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PersonAndTransactions(
    @Embedded
    val person: Person,

    @Relation(parentColumn = "id", entityColumn = "partner_id")
    val transactions: List<Transaction>
)