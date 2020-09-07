package com.mmutert.trackmydebt.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppDao {

    @get:Query("Select * from persons")
    val persons : LiveData<List<Person>>

    @get:Query("Select * from transactions ORDER BY datetime(date) DESC")
    val transactions : LiveData<List<Transaction>>

    @Query("Select * from transactions where partner_id == :personId ORDER BY datetime(date) DESC")
    fun transactionsForPerson(personId: Long) : LiveData<List<Transaction>>

    @get:androidx.room.Transaction
    @get:Query("Select * from persons")
    val personAndTransactions: LiveData<List<PersonAndTransactions>>

    @get:androidx.room.Transaction
    @get:Query("Select * from transactions ORDER BY datetime(date) DESC")
    val transactionAndPerson: LiveData<List<TransactionAndPerson>>

    @Query("SELECT * FROM persons where id = :referringPersonId")
    fun getPerson(referringPersonId: Long) : LiveData<Person>

    @Query("SELECT * FROM transactions where id = :transactionId")
    fun getTransaction(transactionId: Long): LiveData<Transaction>

    @get:Query("Select -SUM(t.amount) FROM transactions t")
    val balance: LiveData<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(p: Person)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(t: Transaction)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTransaction(t: Transaction)

    @Delete
    suspend fun deletePerson(p: Person)

    @Delete
    suspend fun deleteTransaction(t: Transaction)
}