package com.mmutert.trackmydebt.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.TransactionAction
import com.mmutert.trackmydebt.databinding.DialogTransactionBinding
import com.mmutert.trackmydebt.util.FormatHelper
import java.math.BigDecimal

class TransactionDialogFragment(
    private val receiving: Boolean,
    private val listener: TransactionConfirmedListener
) : DialogFragment() {

    interface TransactionConfirmedListener {
        fun transactionConfirmed(
            amount: BigDecimal,
            receiving: Boolean,
            action: TransactionAction,
            reason: String,
            reasonLong: String
        )
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
                val amount = FormatHelper.parseNumber(amountInput)
                val reason: String = binding.etReasonInput.text.toString()
                val action = if(receiving) {
                    TransactionAction.MONEY_TO_USER
                } else {
                    TransactionAction.MONEY_FROM_USER
                }
                listener.transactionConfirmed(amount, receiving, action, reason, "")
            }
            .setNegativeButton(android.R.string.cancel) { dialog, which -> }
            .create()
    }
}
