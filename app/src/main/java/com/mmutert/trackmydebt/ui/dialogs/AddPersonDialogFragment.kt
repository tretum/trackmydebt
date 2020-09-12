package com.mmutert.trackmydebt.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.databinding.DialogAddPersonBinding

class AddPersonDialogFragment(private val listener: PersonAddedListener) : DialogFragment() {

    interface PersonAddedListener {
        fun personAdded(person: Person)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: DialogAddPersonBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_add_person, null, false)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                listener.personAdded(
                    Person(0,
                    binding.etNameInput.text.toString(),
                    binding.etPaypalUsernameInput.text.toString())
                )
            }
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> }
            .create()
    }
}