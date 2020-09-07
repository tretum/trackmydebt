package com.mmutert.trackmydebt.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import java.math.BigDecimal

class AppRepository(private val dao: AppDao) {

    val personAndTransactions: LiveData<List<PersonAndTransactions>> = dao.personAndTransactions
    val transactionAndPerson: LiveData<List<TransactionAndPerson>> = dao.transactionAndPerson
    val persons : LiveData<List<Person>> = dao.persons
    val transactions : LiveData<List<Transaction>> = dao.transactions
    private val _balance : LiveData<Long> = dao.balance
    val balance = Transformations.map(_balance) {
        BigDecimal(it / 100.0)
    }

    fun getTransactions(p: Person): LiveData<List<Transaction>> {
        return dao.transactionsForPerson(p.id)
    }

    suspend fun addTransaction(t: Transaction) {
        dao.insertTransaction(t)
    }

    suspend fun updateTransaction(t: Transaction) {
        dao.updateTransaction(t)
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

    fun getPerson(referringPersonId: Long): LiveData<Person> {
        return dao.getPerson(referringPersonId)
    }

    fun getTransaction(transactionId: Long): LiveData<Transaction> {
        return dao.getTransaction(transactionId)
    }
}