package com.mmutert.trackmydebt.data

import androidx.lifecycle.LiveData

class AppRepository(private val dao: AppDao) {

    val personAndTransactions: LiveData<List<PersonAndTransactions>> = dao.personAndTransactions
    val transactionAndPerson: LiveData<List<TransactionAndPerson>> = dao.transactionAndPerson
    val persons : LiveData<List<Person>> = dao.persons
    val transactions : LiveData<List<Transaction>> = dao.transactions
    val balance : LiveData<Long> = dao.balance

    fun getTransactions(p: Person): LiveData<List<Transaction>> {
        return dao.transactionsForPerson(p.id)
    }

    suspend fun addTransaction(t: Transaction) {
        dao.insertTransaction(t)
    }

    suspend fun removeTransaction(t: Transaction) {
        dao.deleteTransaction(t)
    }

    suspend fun removePerson(p : Person) {
        dao.deletePerson(p)
    }

    suspend fun addPerson(p : Person) {
        dao.insertPerson(p)
    }


}