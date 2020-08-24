package com.mmutert.trackmydebt.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mmutert.trackmydebt.model.PersonModel

@Dao
interface AppDao {

    @get:Query("Select * from persons")
    val persons : LiveData<List<Person>>

    @get:Query("Select * from transactions")
    val transactions : LiveData<List<Transaction>>

    @Query("Select * from transactions where partner_id == :personId")
    fun transactionsForPerson(personId: Long) : LiveData<List<Transaction>>

    @Query("Select p.id, p.name as name, -SUM(t.amount) as sum from persons p LEFT OUTER JOIN transactions t on p.id = t.partner_id GROUP BY p.id")
    fun getPersonModels() : LiveData<List<PersonModel>>

    @get:androidx.room.Transaction
    @get:Query("Select * from persons")
    val personAndTransactions: LiveData<List<PersonAndTransactions>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(p: Person)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(t: Transaction)

    @Delete
    suspend fun deletePerson(p: Person)

    @Delete
    suspend fun deleteTransaction(t: Transaction)
}