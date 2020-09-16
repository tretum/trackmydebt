package com.mmutert.trackmydebt.ui.recentactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.data.TransactionAndPerson
import kotlinx.coroutines.launch

class RecentActivityViewModel(private val repository: AppRepository) : ViewModel() {

    val transactions: LiveData<List<Transaction>> = repository.transactions
    val transactionAndPerson: LiveData<List<TransactionAndPerson>> = repository.transactionAndPerson
    val persons : LiveData<List<Person>> = repository.persons

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.removeTransaction(transaction)
        }
    }
}