package com.mmutert.trackmydebt.ui.transaction

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.databinding.FragmentAddTransactionBinding
import com.mmutert.trackmydebt.util.FormatHelper

class AddTransactionFragment : Fragment() {

    private val LOG_TAG: String = "AddTransactionFragment"
    private lateinit var viewModel: AddTransactionViewModel

    private lateinit var personArrayAdapter: PersonArrayAdapter
    private lateinit var binding: FragmentAddTransactionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddTransactionViewModel::class.java)

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_transaction, container, false)

        binding.apply {
            viewModel = this@AddTransactionFragment.viewModel

            spTransactionCauseSelector.adapter = TransactionStateArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item
            )
        }


        personArrayAdapter = PersonArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item
        )
        viewModel.persons.observe(viewLifecycleOwner) {
            personArrayAdapter.persons = it
        }
        binding.spSelectPerson.adapter = personArrayAdapter

        binding.spSelectPerson.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.selectedPartnerId = personArrayAdapter.getSelectedPerson(position).id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.selectedPartnerId = 0L
            }
        }

        binding.floatingActionButton.setOnClickListener {
            var noErrors = true

            for (textInputLayout in listOf(binding.tilReasonShortInput, binding.tilValueInput)) {
                val editTextString = textInputLayout.editText!!.text.toString()
                if (editTextString.isBlank()) {
                    textInputLayout.error = resources.getString(R.string.error_string)
                    noErrors = false
                } else {
                    textInputLayout.error = null
                }
            }

            if (noErrors) {
                val value = FormatHelper.parseNumber(binding.etValueInput.text.toString())
                viewModel.save(value)
                findNavController().navigateUp()
            }
        }

        // TODO Setup Date picker dialog
        // TODO Setup time picker dialog

        Log.d(LOG_TAG, "Finished creating view")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val args = AddTransactionFragmentArgs.fromBundle(requireArguments())
        val transactionId = args.transactionId
        val referringPersonId = args.referringPersonId

        Log.d(LOG_TAG, "Argument transactionId: $transactionId")
        Log.d(LOG_TAG, "Argument referringPersonId: $referringPersonId")

        if(transactionId > 0L) {
            val transactionLiveData = viewModel.loadTransaction(transactionId)
            transactionLiveData.observe(viewLifecycleOwner) {
                viewModel.transaction = it
                // TODO Setup all values in the layout
                binding.etReasonShortInput.text = Editable.Factory.getInstance().newEditable(it.reason)
                binding.etValueInput.text = Editable.Factory.getInstance().newEditable(FormatHelper.printAsFloat(it.amount))
                setupPerson(it.partnerId)

                // TODO Save editing status for preserving after rotation
            }
        } else if(referringPersonId > 0L) {
            setupPerson(referringPersonId)
        }


        // viewModel.prepare(transactionId, referringPersonId)
    }

    private fun setupPerson(personId: Long) {
        val referringPerson : LiveData<Person> = viewModel.loadPerson(personId)
        referringPerson.observe(viewLifecycleOwner) {
            val indexOfPerson = personArrayAdapter.getIndexOfPerson(it)
            binding.spSelectPerson.setSelection(indexOfPerson)
            Log.d(LOG_TAG, "Set person spinner to index $indexOfPerson, displaying person ${it.name}")
        }
    }
}