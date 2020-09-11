package com.mmutert.trackmydebt.ui.persondetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.Event
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.Result
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.util.FormatHelper
import com.mmutert.trackmydebt.util.balance
import kotlinx.coroutines.launch
import java.math.BigDecimal

class PersonDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository =
        AppRepository(AppDatabase.getDatabase(application).dao())

    private val _selection: MutableLiveData<Person> = MutableLiveData()

    lateinit var person: Person

    val transactions: LiveData<List<Transaction>> =
        Transformations.switchMap(_selection) { person -> repository.getTransactions(person) }

    val sum: LiveData<BigDecimal> = Transformations.map(transactions) {
        it.balance()
    }
    val formattedSum = Transformations.map(sum) {
        FormatHelper.printAsCurrency(it)
    }

    val paypalButtonLabelRes : LiveData<Int> = Transformations.map(sum) {
        when {
            it > BigDecimal.ZERO -> R.string.button_paypal_request_label
            it < BigDecimal.ZERO -> R.string.button_paypal_send_label
            else -> R.string.empty_string
        }
    }

    val empty : LiveData<Boolean> = Transformations.map(transactions) {
        it.isEmpty()
    }

    // EVENTS
    private val _deleteTaskEvent = MutableLiveData<Event<Unit>>()
    val deleteTaskEvent: LiveData<Event<Unit>> = _deleteTaskEvent

    private val _addTransactionEvent = MutableLiveData<Event<Long>>()
    val addTransactionEvent: LiveData<Event<Long>> = _addTransactionEvent

    private val _editTransactionEvent = MutableLiveData<Event<Pair<Long, Long>>>()
    val editTransactionEvent: LiveData<Event<Pair<Long, Long>>> = _editTransactionEvent

    private val _snackbarTextId = MutableLiveData<Event<Int>>()
    val snackbarTextId: LiveData<Event<Int>> = _snackbarTextId

    // FUNCTIONS
    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_transaction_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_transaction_message)
        }
    }

    private fun showSnackbarMessage(message: Int) {
        _snackbarTextId.value = Event(message)
    }

    fun loadPerson(personId: Long) {
        viewModelScope.launch {
            when (val person = repository.getPerson(personId)) {
                is Result.Success -> {
                    _selection.value = person.data
                    this@PersonDetailViewModel.person = person.data
                }
                is Result.Error -> TODO()
            }
        }
    }

    fun removeSelectedPerson() {
        viewModelScope.launch {
            repository.removePerson(person)
            _deleteTaskEvent.value = Event(Unit)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.removeTransaction(transaction)
        }
    }

    fun addTransaction() {
        _addTransactionEvent.value = Event(person.id)
    }

    fun editTransaction(transactionId : Long = 0L) {
        _editTransactionEvent.value = Event(Pair(transactionId, person.id))
    }
}
