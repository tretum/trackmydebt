package com.mmutert.trackmydebt.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TransactionAndPerson(
    @Embedded
    val transaction: Transaction,

    @Relation(parentColumn = "partner_id", entityColumn = "id")
    val person: Person,
)