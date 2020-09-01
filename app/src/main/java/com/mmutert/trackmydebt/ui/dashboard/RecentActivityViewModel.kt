package com.mmutert.trackmydebt.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.data.TransactionAndPerson
import kotlinx.coroutines.launch

class RecentActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(AppDatabase.getDatabase(application).dao())

    val transactions: LiveData<List<Transaction>> = repository.transactions
    val transactionAndPerson: LiveData<List<TransactionAndPerson>> = repository.transactionAndPerson
    val persons : LiveData<List<Person>> = repository.persons

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.removeTransaction(transaction)
        }
    }
}