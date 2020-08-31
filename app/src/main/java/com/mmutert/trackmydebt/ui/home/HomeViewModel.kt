package com.mmutert.trackmydebt.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.PersonAndTransactions
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.util.TimeHelper
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository =
        AppRepository(AppDatabase.getDatabase(application).dao())
    val balance: LiveData<Long> = repository.balance
    val persons: LiveData<List<PersonAndTransactions>> = repository.personAndTransactions

    fun addPerson(name: String) {
        viewModelScope.launch {
            // TODO Add paypal link
            repository.addPerson(Person(0, name, ""))
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    fun giveMoney(partner: Person, amount: Long, reason: String) {
        addTransaction(Transaction(0, partner.id, false, -amount, TimeHelper.currentDateTimeLocalized, reason))
    }

    fun receiveMoney(partner: Person, amount: Long, reason: String) {
        addTransaction(Transaction(0, partner.id, true, amount, TimeHelper.currentDateTimeLocalized, reason))
    }

}