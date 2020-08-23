package com.mmutert.trackmydebt.data

import androidx.lifecycle.LiveData

class AppRepository(private val dao: AppDao) {

    val personAndTransactions: LiveData<List<PersonAndTransactions>> = dao.personAndTransactions
    val persons : LiveData<List<Person>> = dao.persons
    val transactions : LiveData<List<Transaction>> = dao.transactions

    suspend fun addTransaction(t: Transaction) {
        dao.insertTransaction(t)
    }

    suspend fun removeTransaction(t: Transaction) {
        dao.deleteTransaction(t)
    }

    suspend fun removePerson(p : Person) {
        TODO("Remove person and all transactions for that person?")
    }

    suspend fun addPerson(p : Person) {
        dao.insertPerson(p)
    }
}