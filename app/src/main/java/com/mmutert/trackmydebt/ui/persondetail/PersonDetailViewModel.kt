package com.mmutert.trackmydebt.ui.persondetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.Transaction

class PersonDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository =
        AppRepository(AppDatabase.getDatabase(application).dao())

    private val selection: MutableLiveData<Person> = MutableLiveData()

    fun selectPerson(p: Person) {
        selection.value = p
    }

    val transactions: LiveData<List<Transaction>> =
        Transformations.switchMap(selection) { person -> repository.getTransactions(person) }
}