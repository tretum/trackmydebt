package com.mmutert.trackmydebt.ui.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.Event
import com.mmutert.trackmydebt.TransactionAction
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.Result
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.util.FormatHelper
import com.mmutert.trackmydebt.util.TimeHelper
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.Locale

class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository = AppRepository(AppDatabase.getDatabase(application).dao())

    val persons = repository.persons

    val dateFormatter: DateTimeFormatter = DateTimeFormat.longDate().withLocale(Locale.getDefault())
    val timeFormatter = DateTimeFormat.shortTime().withLocale(Locale.getDefault())

    private var id: Long = 0L
    private var referringPersonId : Long = 0L

    val reasonShort = MutableLiveData<String>()
    val reasonLong = MutableLiveData<String>()

    val selectedDate = MutableLiveData(TimeHelper.currentDateLocalized)
    var printedDate = Transformations.map(selectedDate) {
        dateFormatter.print(it)
    }

    var selectedTime = MutableLiveData(TimeHelper.currentTimeLocalized)
    var printedTime = Transformations.map(selectedTime) {
        timeFormatter.print(it)
    }

    var transactionAction = MutableLiveData(TransactionAction.MONEY_FROM_USER)
    val valueString = MutableLiveData("")

    private var isNewTransaction: Boolean = false

    var selectedPerson: MutableLiveData<Person> = MutableLiveData()


    private val _transactionUpdated = MutableLiveData<Event<Int>>()
    val transactionUpdated: LiveData<Event<Int>> = _transactionUpdated


    fun start(transactionId: Long?, referringPersonId: Long) {
        if (transactionId == null || transactionId <= 0L) {
            this.isNewTransaction = true
            return
        }

        // Load the transaction
        viewModelScope.launch {
            when (val result = repository.getTransaction(transactionId)) {
                is Result.Success -> onTransactionLoaded(result.data)
                is Result.Error -> TODO()
            }
        }

        this.referringPersonId = referringPersonId
    }

    private fun onTransactionLoaded(transaction: Transaction) {
        reasonShort.value = transaction.reason
        reasonLong.value = transaction.reasonLong
        valueString.value = FormatHelper.printAsFloat(transaction.amount)
        transactionAction.value = transaction.action
        selectedDate.value = transaction.date.toLocalDate()
        selectedTime.value = transaction.date.toLocalTime()
        id = transaction.id
        // TODO Selected person
    }

    fun save() {

        when {
            valueString.value == null -> {
                // TODO Error handling
                return
            }
            selectedDate.value == null -> {
                return
            }
            selectedTime.value == null -> {
                return
            }
            reasonShort.value == null -> {
                return
            }
            reasonLong.value == null -> {
                return
            }
            transactionAction.value == null -> {
                return
            }
            else -> {
                val value = FormatHelper.parseNumber(valueString.value!!)
                val datetime = selectedDate.value!!.toLocalDateTime(selectedTime.value!!)

                viewModelScope.launch {

                    val received = when (transactionAction.value!!) {
                        TransactionAction.MONEY_FROM_USER, TransactionAction.LENT_BY_USER -> false
                        TransactionAction.MONEY_TO_USER, TransactionAction.LENT_TO_USER -> true
                    }
                    val t = Transaction(
                        id, selectedPerson.value!!.id, received, value, datetime,
                        transactionAction.value!!, reasonShort.value!!, reasonLong.value!!
                    )
                    createTransaction(t)
                    _transactionUpdated.value = Event(0)
                }
            }
        }
    }

    private fun createTransaction(t: Transaction) = viewModelScope.launch {
        repository.addTransaction(t)
    }

    fun loadSelectedPerson() {
        if (referringPersonId > 0L) {
            viewModelScope.launch {
                when (val person = repository.getPerson(referringPersonId)) {
                    is Result.Success -> selectedPerson.value = person.data
                    is Result.Error -> TODO()
                }
            }
        }
    }
}