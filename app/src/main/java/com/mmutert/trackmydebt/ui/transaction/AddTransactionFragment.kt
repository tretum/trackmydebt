package com.mmutert.trackmydebt.ui.transaction

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.format.DateFormat.is24HourFormat
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
import com.google.android.material.datepicker.MaterialDatePicker
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.databinding.FragmentAddTransactionBinding
import com.mmutert.trackmydebt.util.FormatHelper
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.util.Date
import java.util.Locale

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

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_add_transaction, container, false
        )

        Log.d(LOG_TAG, "Finished creating view")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val args = AddTransactionFragmentArgs.fromBundle(requireArguments())
        val transactionId = args.transactionId
        val referringPersonId = args.referringPersonId

        Log.d(LOG_TAG, "Argument transactionId: $transactionId")
        Log.d(LOG_TAG, "Argument referringPersonId: $referringPersonId")

        if (transactionId > 0L) {
            val transactionLiveData = viewModel.loadTransaction(transactionId)
            transactionLiveData.observe(viewLifecycleOwner) {
                viewModel.transaction = it
                // TODO Setup all values in the layout
                binding.etReasonShortInput.text =
                    Editable.Factory.getInstance().newEditable(it.reason)
                binding.etValueInput.text =
                    Editable.Factory.getInstance().newEditable(FormatHelper.printAsFloat(it.amount))
                setupPerson(it.partnerId)

                // TODO Save editing status for preserving after rotation
            }
        } else if (referringPersonId > 0L) {
            setupPerson(referringPersonId)
        }

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
                        viewModel.selectedPartnerId =
                            personArrayAdapter.getSelectedPerson(position).id
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        viewModel.selectedPartnerId = 0L
                    }
                }
        }


        // Set up the Floating Action button to save the entry on click if there is no error
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

        setupDatePicker()
        setupTimePicker()

    }

    private fun setupPerson(personId: Long) {
        val referringPerson: LiveData<Person> = viewModel.loadPerson(personId)
        referringPerson.observe(viewLifecycleOwner) {
            val indexOfPerson = personArrayAdapter.getIndexOfPerson(it)
            binding.spSelectPerson.setSelection(indexOfPerson)
            Log.d(
                LOG_TAG,
                "Set person spinner to index $indexOfPerson, displaying person ${it.name}"
            )
        }
    }

    private fun setupDatePicker() {
        binding.btDateSelection.text = viewModel.DATE_FORMATTER.print(viewModel.transaction.date)

        // Set up the freezing date picker
        val datePicker = createDatePicker(
            R.string.fragment_add_transaction_date_picker_title,
            viewModel.transaction.date.toLocalDate()
        )

        // Add the behavior for the positive button of the date picker
        datePicker.addOnPositiveButtonClickListener { selection: Long ->
            val date: LocalDate = convertSelectedDate(selection)

            viewModel.selectedDate = date

            val selectedFrozenDateFormatted = viewModel.DATE_FORMATTER.print(date)
            binding.btDateSelection.text = selectedFrozenDateFormatted
        }

        // Open the date picker on clicking the text view
        binding.btDateSelection.setOnClickListener {
            datePicker.show(parentFragmentManager, datePicker.toString())
        }
    }

    private fun setupTimePicker() {
        val minute: Int = viewModel.selectedTime.minuteOfHour
        val hour: Int = viewModel.selectedTime.hourOfDay

        // Initial label for the button#
        val timeFormatter = DateTimeFormat.shortTime().withLocale(Locale.getDefault())
        binding.btTimeSelection.text = timeFormatter.print(viewModel.selectedTime)

        val materialTimePicker = TimePickerDialog(
            requireContext(),
            { view, hourOfDay, minute ->
                viewModel.selectedTime = LocalTime(hourOfDay, minute)
                binding.btTimeSelection.text = timeFormatter.print(viewModel.selectedTime)
            }, hour, minute, is24HourFormat(context)
        )

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

    /**
     * Creates a material date picker. This can be shown using result.show()
     *
     * @param titleStringId        The string id of the title text
     * @param defaultSelectionDate The date
     * @return The date picker
     */
    private fun createDatePicker(
        titleStringId: Int,
        defaultSelectionDate: LocalDate
    ): MaterialDatePicker<Long> {

        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText(resources.getString(titleStringId))
        // TODO Fix hack with adding hours to get the correct selection
        builder.setSelection(
            defaultSelectionDate.toLocalDateTime(LocalTime.MIDNIGHT.plusHours(12))
                .toDate().time
        )
        return builder.build()
    }
}