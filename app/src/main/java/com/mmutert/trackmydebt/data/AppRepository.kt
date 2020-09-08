package com.mmutert.trackmydebt.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class AppRepository(
    private val dao: AppDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val personAndTransactions: LiveData<List<PersonAndTransactions>> = dao.personAndTransactions
    val transactionAndPerson: LiveData<List<TransactionAndPerson>> = dao.transactionAndPerson
    val persons: LiveData<List<Person>> = dao.persons
    val transactions: LiveData<List<Transaction>> = dao.transactions
    private val _balance: LiveData<Long> = dao.balance
    val balance = Transformations.map(_balance) {
        if (it != null) {
            BigDecimal(it / 100.0)
        } else {
            // Fixes crash on initialization
            BigDecimal.ZERO
        }
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

    suspend fun removePerson(p: Person) {
        dao.deletePerson(p)
    }

    suspend fun addPerson(p: Person) {
        dao.insertPerson(p)
    }

    suspend fun getPerson(referringPersonId: Long): Result<Person> =
        withContext(ioDispatcher) {
        try {
            val person = dao.getPerson(referringPersonId)
                ?: return@withContext Result.Error(Exception("Transaction not found!"))
            return@withContext Result.Success(person)
        } catch (e: java.lang.Exception) {
            return@withContext Result.Error(e)
        }
    }

    suspend fun getTransaction(transactionId: Long): Result<Transaction> =
        withContext(ioDispatcher) {
            try {
                val transaction = dao.getTransaction(transactionId)
                    ?: return@withContext Result.Error(Exception("Transaction not found!"))
                return@withContext Result.Success(transaction)
            } catch (e: java.lang.Exception) {
                return@withContext Result.Error(e)
            }
        }
}