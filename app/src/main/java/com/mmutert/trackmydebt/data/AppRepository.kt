package com.mmutert.trackmydebt.data

import androidx.lifecycle.LiveData
import com.mmutert.trackmydebt.model.PersonModel

class AppRepository(private val dao: AppDao) {

    val personAndTransactions: LiveData<List<PersonAndTransactions>> = dao.personAndTransactions
    val persons : LiveData<List<Person>> = dao.persons
    val transactions : LiveData<List<Transaction>> = dao.transactions

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
        TODO("Remove person and all transactions for that person?")
    }

    suspend fun addPerson(p : Person) {
        dao.insertPerson(p)
    }

    val personModels : LiveData<List<PersonModel>> = dao.getPersonModels()
}