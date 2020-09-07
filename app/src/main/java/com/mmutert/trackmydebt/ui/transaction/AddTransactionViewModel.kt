package com.mmutert.trackmydebt.ui.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.TransactionAction
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.util.TimeHelper
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import java.math.BigDecimal

class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {

    val repository: AppRepository = AppRepository(AppDatabase.getDatabase(application).dao())
    val persons = repository.persons

    var selectedPartnerId: Long = 0L
    var selectedDate: LocalDate = TimeHelper.currentDateLocalized
    var selectedTime: LocalTime = TimeHelper.currentTimeLocalized
    var selectedPersonPosition: Int = 1
    var selectedReasonPosition: Int = 1

    var transaction: Transaction =
        Transaction(
            0,
            0,
            false,
            BigDecimal.ZERO,
            TimeHelper.currentDateTimeLocalized,
            TransactionAction.MONEY_FROM_USER,
            "",
            ""
        )

    // TODO: Way to get a new Transaction

    // TODO Way to load an existing transaction

    fun save(value: BigDecimal) {

        viewModelScope.launch {
            // TODO Set missing values in the copy
            val copy = transaction.copy(partnerId = selectedPartnerId, amount = value)

            if (copy.id == 0L) {
                repository.addTransaction(copy)
            } else {
                repository.updateTransaction(copy)
            }
        }
    }

    fun loadPerson(referringPersonId: Long): LiveData<Person> {
        return repository.getPerson(referringPersonId)
    }

    fun loadTransaction(transactionId: Long): LiveData<Transaction> {
        return repository.getTransaction(transactionId)
    }
}