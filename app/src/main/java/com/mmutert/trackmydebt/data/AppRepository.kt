package com.mmutert.trackmydebt.data

import androidx.lifecycle.LiveData

class AppRepository(private val dao: AppDao) {

    val personAndTransactions: LiveData<List<PersonAndTransactions>> = dao.personAndTransactions
    val persons : LiveData<List<Person>> = dao.persons
    val transactions : LiveData<List<Transaction>> = dao.transactions

    fun addTransaction(t: Transaction) {
        TODO()
    }

    fun removeTransaction(t: Transaction) {
        TODO()
    }

    fun removePerson(p : Person) {
        TODO("Remove person and all transactions for that person?")
    }

    fun addPerson(p : Person) {
        TODO()
    }
}