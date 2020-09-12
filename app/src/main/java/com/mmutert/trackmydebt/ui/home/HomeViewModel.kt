package com.mmutert.trackmydebt.ui.home

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.Event
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.TransactionAction
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.PersonAndTransactions
import com.mmutert.trackmydebt.ui.home.FilterType.FILTER_ALL
import com.mmutert.trackmydebt.ui.home.FilterType.FILTER_CREDIT_ONLY
import com.mmutert.trackmydebt.ui.home.FilterType.FILTER_DEBT_ONLY
import com.mmutert.trackmydebt.util.FormatHelper
import com.mmutert.trackmydebt.util.balance
import kotlinx.coroutines.launch
import java.math.BigDecimal

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle, private val repository: AppRepository
) : ViewModel() {

    private val _noPersonsLabel = MutableLiveData<Int>()
    val noPersonsLabel: LiveData<Int> = _noPersonsLabel
    private val _noPersonsIcon = MutableLiveData<Int>()
    val noPersonsIcon: LiveData<Int> = _noPersonsIcon

    private val _forceUpdate = MutableLiveData(false)

    private val _persons: LiveData<List<PersonAndTransactions>> = _forceUpdate.switchMap {
        repository.personAndTransactions.distinctUntilChanged().switchMap {
            filterPersons(it)
        }
    }
    val persons: LiveData<List<PersonAndTransactions>> = _persons

    val balanceFormatted: LiveData<String> = Transformations.map(repository.balance) {
        FormatHelper.printAsCurrency(it)
    }

    private val _sumDebt: LiveData<BigDecimal> = Transformations.map(repository.transactions) {
        it.filter { p ->
            p.action == TransactionAction.LENT_TO_USER || p.action == TransactionAction.MONEY_TO_USER
        }.balance()
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
    val empty: LiveData<Boolean> = Transformations.map(persons) {
        it.isEmpty()
    }

    private val _snackbarTextId = MutableLiveData<Event<Int>>()
    val snackbarTextId: LiveData<Event<Int>> = _snackbarTextId

    private fun showSnackbarMessage(messageId: Int) {
        _snackbarTextId.value = Event(messageId)
    }

    fun showEditResultMessage(result: Int) {
        when (result) {
            // TODO
            PERSON_DELETED_OK -> showSnackbarMessage(R.string.successfully_deleted_person)
        }
    }

    fun setFilterMode(mode: FilterType) {
        savedStateHandle.set(PERSONS_FILTER_SAVED_STATE_KEY, mode)

        // TODO Apply changes to ui by setting the relevant resource ids
        when (mode) {
            FILTER_DEBT_ONLY -> {
                setFilter(
                    R.string.label_debt_persons,
                    R.string.no_persons_debt,
                    // R.drawable.logo_no_fill,
                    // true
                )
            }
            FILTER_ALL -> {
                setFilter(
                    R.string.label_all_persons,
                    R.string.no_persons_all,
                    // R.drawable.logo_no_fill,
                    // true
                )
            }
            FILTER_CREDIT_ONLY -> {
                setFilter(
                    R.string.label_credit_persons,
                    R.string.no_persons_credit,
                    // R.drawable.logo_no_fill,
                    // true
                )
            }
        }
        _forceUpdate.value = false
    }

    private fun setFilter(
        @StringRes filteringLabelString: Int,
        @StringRes noTasksLabelString: Int,
        // @DrawableRes noTaskIconDrawable: Int,
        // tasksAddVisible: Boolean
    ) {
        // _currentFilteringLabel.value = filteringLabelString
        _noPersonsLabel.value = noTasksLabelString
        // _noPersonsIcon.value = noTaskIconDrawable
        // _tasksAddViewVisible.value = tasksAddVisible
    }

    private fun filterPersons(personsResult: List<PersonAndTransactions>): LiveData<List<PersonAndTransactions>> {
        val result = MutableLiveData<List<PersonAndTransactions>>()

        viewModelScope.launch {
            result.value = filterItems(personsResult, getSavedFilterType())
        }

        return result
    }

    private fun filterItems(
        persons: List<PersonAndTransactions>,
        filterType: FilterType
    ): List<PersonAndTransactions> {
        val result = ArrayList<PersonAndTransactions>()

        persons.forEach {
            val balance = it.transactions.balance()
            when (filterType) {
                FILTER_DEBT_ONLY -> if (balance < BigDecimal.ZERO) result.add(it)
                FILTER_ALL -> result.add(it)
                FILTER_CREDIT_ONLY -> if (balance > BigDecimal.ZERO) result.add(it)
            }
        }

        return result
    }

    private fun getSavedFilterType(): FilterType {
        return savedStateHandle.get(PERSONS_FILTER_SAVED_STATE_KEY) ?: FILTER_ALL
    }

    private val _personClicked = MutableLiveData<Event<Person>>()
    val personClicked: LiveData<Event<Person>> = _personClicked

    fun openPersonOverview(person: Person) {
        _personClicked.value = Event(person)
    }

    fun addPerson(person: Person) {
        viewModelScope.launch {
            repository.addPerson(person)
        }
    }

    init {
        setFilterMode(getSavedFilterType())
    }
}

const val PERSONS_FILTER_SAVED_STATE_KEY = "PERSONS_FILTER_SAVED_STATE_KEY"