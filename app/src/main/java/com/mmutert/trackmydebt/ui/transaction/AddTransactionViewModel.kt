package com.mmutert.trackmydebt.ui.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.Event
import com.mmutert.trackmydebt.TransactionAction
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.Result
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.util.FormatHelper
import com.mmutert.trackmydebt.util.TimeHelper
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.Locale

class AddTransactionViewModel(private val repository: AppRepository) : ViewModel() {

    val persons = repository.persons

    val dateFormatter: DateTimeFormatter = DateTimeFormat.longDate().withLocale(Locale.getDefault())
    val timeFormatter: DateTimeFormatter =
        DateTimeFormat.shortTime().withLocale(Locale.getDefault())

    private var id: Long = 0L

    val reasonShort = MutableLiveData<String>()
    val reasonLong = MutableLiveData<String>()

    private val _selectedDate = MutableLiveData(TimeHelper.currentDateLocalized)
    val selectedDate: LiveData<LocalDate> = _selectedDate

    val printedDate = Transformations.map(selectedDate) {
        dateFormatter.print(it)
    }

    private val _selectedTime = MutableLiveData(TimeHelper.currentTimeLocalized)
    val selectedTime: LiveData<LocalTime> = _selectedTime
    val printedTime = Transformations.map(selectedTime) {
        timeFormatter.print(it)
    }

    private val _transactionAction = MutableLiveData(TransactionAction.MONEY_FROM_USER)
    val transactionAction: LiveData<TransactionAction> = _transactionAction

    val valueString = MutableLiveData("")

    private var isNewTransaction: Boolean = false

    private val _selectedPerson: MutableLiveData<Person> = MutableLiveData()
    val selectedPerson: LiveData<Person> = _selectedPerson

    private val _transactionUpdated = MutableLiveData<Event<Int>>()
    val transactionUpdated: LiveData<Event<Int>> = _transactionUpdated

    fun start(transactionId: Long = 0L, referringPersonId: Long = 0L) {
        if (transactionId > 0L) {
            this.isNewTransaction = true

            // Load the transaction
            viewModelScope.launch {
                when (val result = repository.getTransaction(transactionId)) {
                    is Result.Success -> onTransactionLoaded(result.data)
                    is Result.Error -> TODO()
                }
            }
        }

        if (referringPersonId > 0L) {
            viewModelScope.launch {
                when (val person = repository.getPerson(referringPersonId)) {
                    // TODO Check nullable
                    is Result.Success -> _selectedPerson.value = person.data!!
                    is Result.Error -> TODO()
                }
            }
        }
    }

    private fun onTransactionLoaded(transaction: Transaction) {
        reasonShort.value = transaction.reason
        reasonLong.value = transaction.reasonLong
        valueString.value = FormatHelper.printAsFloat(transaction.amount)
        _transactionAction.value = transaction.action
        _selectedDate.value = transaction.date.toLocalDate()
        _selectedTime.value = transaction.date.toLocalTime()
        id = transaction.id
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
                // TODO This value should be optional
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

    fun selectPerson(selectedPerson: Person) {
        if (_selectedPerson.value != null && _selectedPerson.value!! == selectedPerson) {
            return
        }
        _selectedPerson.value = selectedPerson
    }

    fun selectAction(selectedAction: TransactionAction) {
        if (_transactionAction.value != selectedAction) {
            _transactionAction.value = selectedAction
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun selectTime(time: LocalTime) {
        _selectedTime.value = time
    }
}