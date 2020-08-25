package com.mmutert.trackmydebt.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.databinding.DialogTransactionBinding
import java.lang.NumberFormatException
import kotlin.math.roundToLong

class TransactionDialogFragment(
    private val receiving: Boolean,
    private val listener: TransactionConfirmedListener
) : DialogFragment() {

    interface TransactionConfirmedListener {
        fun transactionConfirmed(amount: Long, receiving: Boolean, reason: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding: DialogTransactionBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_transaction, null, false)

        binding.dialogAddPersonTitle.text = when (receiving) {
            true -> "Received money"
            false -> "Give money"
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                val amountInput = binding.etAmountInput.text.toString()
                val amount : Long = if(amountInput.contains('.') || amountInput.contains(',')) {
                    try {
                        (amountInput.toFloat() * 100).roundToLong()
                    } catch (e : NumberFormatException) {
                        0L
                    }
                } else {
                    try {
                        amountInput.toLong() * 100
                    } catch (e: NumberFormatException) {
                        0
                    }
                }
                val reason : String = binding.etReasonInput.text.toString()
                listener.transactionConfirmed(amount, receiving, reason)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, which -> }
            .create()
    }
}
