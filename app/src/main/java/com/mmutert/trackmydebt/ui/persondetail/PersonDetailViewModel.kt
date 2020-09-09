package com.mmutert.trackmydebt.ui.persondetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.util.FormatHelper
import com.mmutert.trackmydebt.util.balance
import kotlinx.coroutines.launch
import java.math.BigDecimal

class PersonDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository =
        AppRepository(AppDatabase.getDatabase(application).dao())

    val selection: MutableLiveData<Person> = MutableLiveData()
    lateinit var person: Person

    fun selectPerson(p: Person) {
        selection.value = p
        person = p
    }


    val transactions: LiveData<List<Transaction>> =
        Transformations.switchMap(selection) { person -> repository.getTransactions(person) }

    val sum: LiveData<BigDecimal> = Transformations.map(transactions) {
        it.balance()
    }
    val formattedSum = Transformations.map(sum) {
        FormatHelper.printAsCurrency(it)
    }

    val empty : LiveData<Boolean> = Transformations.map(transactions) {
        it.isEmpty()
    }

    fun removeSelectedPerson() {
        viewModelScope.launch {
            repository.removePerson(person)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.removeTransaction(transaction)
        }
    }
}
