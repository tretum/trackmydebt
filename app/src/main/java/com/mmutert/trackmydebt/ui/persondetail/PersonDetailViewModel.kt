package com.mmutert.trackmydebt.ui.persondetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.Transaction
import kotlinx.coroutines.launch
import java.math.BigDecimal

class PersonDetailViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var transactionToDelete: Transaction

    private val repository: AppRepository =
        AppRepository(AppDatabase.getDatabase(application).dao())

    val selection: MutableLiveData<Person> = MutableLiveData()
    lateinit var person: Person

    fun selectPerson(p: Person) {
        selection.value = p
        person = p
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

    val transactions: LiveData<List<Transaction>> =
        Transformations.switchMap(selection) { person -> repository.getTransactions(person) }

    val sum: LiveData<BigDecimal> = Transformations.map(transactions) {
        BigDecimal(-1) * it.sumOf { t -> t.amount }
    }
}
