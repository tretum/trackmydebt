package com.mmutert.trackmydebt.ui.transaction

import android.content.Context
import android.widget.ArrayAdapter
import com.mmutert.trackmydebt.TransactionAction
import java.util.ArrayList

class TransactionStateArrayAdapter(context: Context, resource: Int) :
        ArrayAdapter<CharSequence?>(context, resource) {

    private val values: MutableList<CharSequence> = ArrayList()

    fun getSelectedAction(position: Int): TransactionAction {
        return TransactionAction.values()[position]
    }

    fun getIndexOfAction(action: TransactionAction): Int {
        return TransactionAction.values().indexOf(action)
    }

    init {
        for (value in TransactionAction.values()) {
            val valueString = context.resources.getString(value.labelId)
            values.add(valueString)
        }
        super.addAll(values)
    }
}