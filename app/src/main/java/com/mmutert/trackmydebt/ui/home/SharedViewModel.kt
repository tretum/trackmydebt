package com.mmutert.trackmydebt.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mmutert.trackmydebt.data.Person

class SharedViewModel : ViewModel() {

    val selectedPerson: MutableLiveData<Person> = MutableLiveData()

    fun selectPerson(p: Person) {
        selectedPerson.value = p
    }
}