package com.mmutert.trackmydebt.ui.addperson

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.databinding.DialogAddPersonBinding

class AddPersonDialogFragment(private val listener: PersonAddedListener) : DialogFragment() {

    interface PersonAddedListener {
        fun personAdded(name: String);
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: DialogAddPersonBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_add_person, null, false)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                listener.personAdded(binding.etNameInput.text.toString())
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> }
            .create()
    }
}