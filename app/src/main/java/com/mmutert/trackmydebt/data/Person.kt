package com.mmutert.trackmydebt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val name: String,

    val paypalUserName : String,
)
