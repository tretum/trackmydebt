package com.mmutert.trackmydebt.ui.transaction

import android.content.Context
import android.widget.ArrayAdapter
import com.mmutert.trackmydebt.data.Person
import java.util.ArrayList

class PersonArrayAdapter(context: Context, resource: Int) :
        ArrayAdapter<CharSequence?>(context, resource) {

    var persons: List<Person> = ArrayList()
        set(value) {
            field = value
            super.clear()
            super.addAll(value.map { it.name })
            notifyDataSetChanged()
        }

    fun getSelectedPerson(position: Int): Person {
        return persons[position]
    }

    fun getIndexOfPerson(person: Person): Int {
        return persons.indexOf(person)
    }


}