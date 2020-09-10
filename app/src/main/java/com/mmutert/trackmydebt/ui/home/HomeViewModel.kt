package com.mmutert.trackmydebt.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.PersonAndTransactions
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.util.FormatHelper
import com.mmutert.trackmydebt.util.balance
import kotlinx.coroutines.launch
import java.math.BigDecimal

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository =
        AppRepository(AppDatabase.getDatabase(application).dao())

    val persons: LiveData<List<PersonAndTransactions>> = repository.personAndTransactions
    private val transactions: LiveData<List<Transaction>> = repository.transactions

    val balanceFormatted: LiveData<String> = Transformations.map(repository.balance) {
        FormatHelper.printAsCurrency(it)
    }

    private val _sumDebt: LiveData<BigDecimal> = Transformations.map(persons) {
        it.filter { p ->
            p.transactions.balance() < BigDecimal.ZERO
        }.sumOf { pat ->
            pat.transactions.balance()
        }
        // TODO Optimize by saving balance and using 0 when filter condition not fulfilled
    }
    val sumDebtFormatted: LiveData<String> = Transformations.map(_sumDebt) {
        FormatHelper.printAsCurrency(it.abs())
    }

    private val _sumCredit: LiveData<BigDecimal> = Transformations.map(persons) {
        it.filter { p ->
            p.transactions.balance() > BigDecimal.ZERO
        }.sumOf { pat ->
            pat.transactions.balance()
        }
        // TODO Optimize by saving balance and using 0 when filter condition not fulfilled
    }
    val sumCreditFormatted: LiveData<String> = Transformations.map(_sumCredit) {
        FormatHelper.printAsCurrency(it.abs())
    }

    /**
     * Indicates whether the list of persons is empty.
     */
    val empty : LiveData<Boolean> = Transformations.map(persons) {
        it.isEmpty()
    }


    fun addPerson(name: String, paypalUsername: String?) {
        viewModelScope.launch {
            repository.addPerson(Person(0, name, paypalUsername))
        }
    }
}