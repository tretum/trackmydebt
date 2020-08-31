package com.mmutert.trackmydebt.ui.persondetail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.databinding.FragmentPersonDetailBinding
import com.mmutert.trackmydebt.ui.home.SharedViewModel
import com.mmutert.trackmydebt.util.FormatHelper
import com.mmutert.trackmydebt.util.IntentHelper

class PersonDetailFragment : Fragment() {

    private var columnCount = 1
    private lateinit var mBinding: FragmentPersonDetailBinding
    private lateinit var mViewModel: PersonDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_person_detail, container, false)

        mViewModel = ViewModelProvider(this).get(PersonDetailViewModel::class.java)

        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedViewModel.selectedPerson.observe(viewLifecycleOwner) { person ->
            mViewModel.selectPerson(person)
        }
        mBinding.viewModel = mViewModel

        mViewModel.sum.observe(viewLifecycleOwner) {
            val formattedSum = FormatHelper.printAsCurrency(-it)
            mBinding.formattedSum = formattedSum

            when {
                it < 0L -> {
                    mBinding.btPaypal.apply {
                        visibility = View.VISIBLE
                        text = context.getString(R.string.button_paypal_request_label)
                        setOnClickListener {
                            // Share your paypal.me link or just a message which includes the amount

                            val username = PreferenceManager.getDefaultSharedPreferences(context)
                                .getString(context.getString(R.string.pref_paypal_username_key), "")

                            val shareIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND

                                val message =
                                    """
                                    You owe me $formattedSum. 
                                    ${
                                        if (username != null && username.isNotBlank()) {
                                            """
                                    Please send me the money using the following link:
                                    https://paypal.me/$username/$formattedSum
                                    """
                                        } else {
                                            ""
                                        }
                                    }
                                    """.trimIndent()
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    message
                                )
                                type = "text/plain"
                            }
                            startActivity(Intent.createChooser(shareIntent, "Share with..."))

                        }
                    }
                }
                it > 0L -> {
                    mBinding.btPaypal.apply {
                        visibility = View.VISIBLE
                        text = context.getString(R.string.button_paypal_send_label)
                        setOnClickListener {
                            // Paying = Opening the browser to the paypal.me site
                            val intent =
                                IntentHelper.createBrowserIntent("https://paypal.me/${mViewModel.person.paypalUserName}/$formattedSum")
                            requireActivity().startActivity(intent)
                        }
                    }
                }
                else -> {
                    mBinding.btPaypal.visibility = View.GONE
                }
            }
        }

        // Set the adapter
        mBinding.list.apply {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = PersonDetailAdapter(requireContext())

            mViewModel.transactions.observe(
                viewLifecycleOwner,
                { (this.adapter as PersonDetailAdapter).setTransactions(it) })
        }
        return mBinding.root
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            PersonDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_person_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_remove_person -> {
                mViewModel.removeSelectedPerson()
                findNavController().navigateUp()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}