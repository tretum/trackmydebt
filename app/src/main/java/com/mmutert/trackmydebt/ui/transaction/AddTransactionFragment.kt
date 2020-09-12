package com.mmutert.trackmydebt.ui.transaction

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.databinding.FragmentAddTransactionBinding
import com.mmutert.trackmydebt.util.getViewModelFactory
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import java.util.Date

class AddTransactionFragment : Fragment() {

    companion object {
        private val LOG_TAG = AddTransactionFragment::class.simpleName
    }

    private lateinit var transactionStateAdapter: TransactionStateArrayAdapter
    private val viewModel: AddTransactionViewModel by viewModels { getViewModelFactory() }

    private lateinit var personArrayAdapter: PersonArrayAdapter
    private lateinit var binding: FragmentAddTransactionBinding
    private lateinit var materialTimePicker: TimePickerDialog
    private lateinit var datePickerBuilder: MaterialDatePicker.Builder<Long>

    private val args: AddTransactionFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_add_transaction, container, false
        )

        Log.d(LOG_TAG, "Finished creating view")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val transactionId = args.transactionId
        val referringPersonId = args.referringPersonId

        Log.d(LOG_TAG, "Argument transactionId: $transactionId")
        Log.d(LOG_TAG, "Argument referringPersonId: $referringPersonId")

        viewModel.start(transactionId, referringPersonId)

        // Set up the person selection spinner
        setupPersonSpinner()
        setupTransactionActionSpinner()

        setupDatePicker()
        setupTimePicker()

        binding.apply {
            viewModel = this@AddTransactionFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        // Set up the Floating Action button to save the entry on click if there is no error
        setupSaveFAB()

        viewModel.transactionUpdated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    private fun setupPersonSpinner() {
        personArrayAdapter = PersonArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item
        )
        viewModel.persons.observe(viewLifecycleOwner) {
            personArrayAdapter.persons = it

            viewModel.selectedPerson.observe(viewLifecycleOwner) {
                // TODO Investigate wrong person preselected
                val indexOfPerson = personArrayAdapter.getIndexOfPerson(it)
                binding.spSelectPerson.setSelection(indexOfPerson)
                Log.d(
                    LOG_TAG,
                    "Set person spinner to index $indexOfPerson, displaying person ${it.name}"
                )
            }
        }
        binding.spSelectPerson.apply {
            adapter = personArrayAdapter

            onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedPerson = personArrayAdapter.getSelectedPerson(position)
                        viewModel.selectPerson(selectedPerson)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }
        }
    }

    /**
     * Sets up the behavior of the floating action button.
     * This includes checking that required fields are filled in.
     */
    private fun setupSaveFAB() {
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
                viewModel.save()
            }
        }
    }

    private fun setupTransactionActionSpinner() {
        transactionStateAdapter = TransactionStateArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spTransactionActionSelector.adapter = transactionStateAdapter
        binding.spTransactionActionSelector.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedAction = transactionStateAdapter.getSelectedAction(position)
                    viewModel.selectAction(selectedAction)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        viewModel.transactionAction.observe(viewLifecycleOwner) {
            val indexOfAction = transactionStateAdapter.getIndexOfAction(it)
            binding.spTransactionActionSelector.setSelection(indexOfAction)
        }
    }

    private fun setupDatePicker() {

        // Set up the date picker
        datePickerBuilder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.fragment_add_transaction_date_picker_title))

        viewModel.selectedDate.observe(viewLifecycleOwner) {
            // TODO Fix hack with adding hours to get the correct selection
            datePickerBuilder.setSelection(
                it.toLocalDateTime(LocalTime.MIDNIGHT.plusHours(12))
                    .toDate().time
            )
        }

        // Open the date picker on clicking the text view
        binding.btDateSelection.setOnClickListener {
            // Add the behavior for the positive button of the date picker
            val picker = datePickerBuilder.build()
            picker.addOnPositiveButtonClickListener { selection: Long ->
                val date: LocalDate = convertSelectedDate(selection)
                viewModel.selectDate(date)
            }

            picker.show(parentFragmentManager, datePickerBuilder.toString())
        }
    }

    private fun setupTimePicker() {

        materialTimePicker = TimePickerDialog(
            requireContext(),
            { view, hourOfDay, minute ->
                val localTime = LocalTime(hourOfDay, minute)
                viewModel.selectTime(localTime)
            }, 0, 0, is24HourFormat(context)
        )

        viewModel.selectedTime.observe(viewLifecycleOwner) {
            val hourOfDay = it.hourOfDay
            val minuteOfHour = it.minuteOfHour
            materialTimePicker.updateTime(hourOfDay, minuteOfHour)
        }

        binding.btTimeSelection.setOnClickListener {
            materialTimePicker.show()
        }
    }

    /**
     * Converts the selected date from a date picker to a [LocalDate].
     *
     * @param selection The selected date from the date picker
     * @return The selected date converted to LocalDate.
     */
    private fun convertSelectedDate(selection: Long): LocalDate {
        return LocalDate.fromDateFields(Date(selection))
    }
}