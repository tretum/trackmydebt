package com.mmutert.trackmydebt.data

interface DataSource {

    suspend fun saveTransaction(t: Transaction)
}