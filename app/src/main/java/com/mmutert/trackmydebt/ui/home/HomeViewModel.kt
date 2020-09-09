package com.mmutert.trackmydebt.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.TransactionAction
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.PersonAndTransactions
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.util.FormatHelper
import com.mmutert.trackmydebt.util.TimeHelper
import kotlinx.coroutines.launch
import java.math.BigDecimal

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository =
        AppRepository(AppDatabase.getDatabase(application).dao())

    private val _balance: LiveData<BigDecimal> = repository.balance
    val balanceFormatted : LiveData<String> = Transformations.map(_balance) {
        FormatHelper.printAsCurrency(it)
    }

    val persons: LiveData<List<PersonAndTransactions>> = repository.personAndTransactions

    fun addPerson(name: String, paypalUsername: String?) {
        viewModelScope.launch {
            repository.addPerson(Person(0, name, paypalUsername))
        }
    }
}