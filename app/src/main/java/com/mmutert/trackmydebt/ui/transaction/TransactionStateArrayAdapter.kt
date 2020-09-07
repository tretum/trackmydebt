package com.mmutert.trackmydebt.ui.transaction

import android.content.Context
import android.widget.ArrayAdapter
import com.mmutert.trackmydebt.TransactionState
import java.util.ArrayList

class TransactionStateArrayAdapter(context: Context, resource: Int) :
        ArrayAdapter<CharSequence?>(context, resource) {

    private val values: MutableList<CharSequence> = ArrayList()

    fun getSelectedUnit(position: Int): TransactionState {
        return TransactionState.values()[position]
    }

    fun getIndexOfUnit(unit: TransactionState): Int {
        return TransactionState.values().indexOf(unit)
    }

    init {
        for (value in TransactionState.values()) {
            val valueString = context.resources.getString(value.labelId)
            values.add(valueString)
        }
        super.addAll(values)
    }
}