package com.mmutert.trackmydebt.ui.persondetail

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.databinding.FragmentPersonDetailBinding
import com.mmutert.trackmydebt.ui.home.SharedViewModel
import com.mmutert.trackmydebt.util.FormatHelper
import com.mmutert.trackmydebt.util.IntentHelper
import java.math.BigDecimal

class PersonDetailFragment : Fragment(), PersonDetailAdapter.TransactionClickedListener {

    private lateinit var mBinding: FragmentPersonDetailBinding
    private lateinit var mViewModel: PersonDetailViewModel
    private lateinit var mAdapter: PersonDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        // Set up the sum display and the paypal button
        mViewModel.sum.observe(viewLifecycleOwner) {
            val formattedSum = FormatHelper.printAsCurrency(-it)
            mBinding.formattedSum = formattedSum

            when {
                it < BigDecimal.ZERO -> {
                    mBinding.tvDebtDescription.text = getString(R.string.fragment_person_detail_credit_label)
                    mBinding.btPaypal.apply {
                        visibility = View.VISIBLE
                        text = context.getString(R.string.button_paypal_request_label)
                        setOnClickListener {
                            // Share your paypal.me link or just a message which includes the amount

                            val username = PreferenceManager.getDefaultSharedPreferences(context)
                                .getString(context.getString(R.string.pref_paypal_username_key), "")

                            val shareIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND

                                // TODO Refactor with string resources
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
                            startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    context.getString(R.string.paypal_link_share_menu_title)
                                )
                            )

                        }
                    }
                }
                it > BigDecimal.ZERO -> {
                    mBinding.tvDebtDescription.text = getString(R.string.fragment_person_detail_debt_label)
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
                    mBinding.tvDebtDescription.text = ""
                }
            }
        }

        mBinding.fabAddTransaction.setOnClickListener {
            val directions =
                PersonDetailFragmentDirections.actionPersonDetailFragmentToAddTransactionFragment(
                    title = getString(R.string.fragment_title_new_transaction),
                    referringPersonId = mViewModel.person.id
                )
            findNavController().navigate(directions)
        }

        // Set the adapter
        mAdapter = PersonDetailAdapter(requireContext(), this)
        mBinding.list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
            mViewModel.transactions.observe(
                viewLifecycleOwner,
                { mAdapter.setTransactions(it.toMutableList()) })
        }
        createSwipeHelper().attachToRecyclerView(mBinding.list)
        return mBinding.root
    }


    /**
     * Creates the ItemTouchHelper that archives items in the item list on swipe to the right.
     *
     * @return The item touch helper
     */
    private fun createSwipeHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.START
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val listEntry = mAdapter.getElementAtPosition(pos)

                if (direction == ItemTouchHelper.START) {
                    deleteTransaction(pos)
                } else {
                    mAdapter.notifyItemChanged(pos)
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (viewHolder != null) {
                    val personDetailViewHolder =
                        (viewHolder as PersonDetailAdapter.PersonDetailViewHolder)
                    when (personDetailViewHolder) {
                        is PersonDetailAdapter.PersonDetailViewHolder.TransactionViewHolder -> {
                            getDefaultUIUtil().onSelected(personDetailViewHolder.mBinding.personTransactionCard)
                        }
                        is PersonDetailAdapter.PersonDetailViewHolder.DateViewHolder -> {
                            getDefaultUIUtil().onSelected(personDetailViewHolder.mBinding.tvTransactionDate)
                        }
                    }
                }
            }

            override fun onChildDrawOver(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {

                val personDetailViewHolder =
                    (viewHolder as PersonDetailAdapter.PersonDetailViewHolder)
                lateinit var foregroundView: View
                when (personDetailViewHolder) {
                    is PersonDetailAdapter.PersonDetailViewHolder.TransactionViewHolder -> {
                        foregroundView = personDetailViewHolder.mBinding.personTransactionCard
                    }
                    is PersonDetailAdapter.PersonDetailViewHolder.DateViewHolder -> {
                        foregroundView = personDetailViewHolder.mBinding.tvTransactionDate
                    }
                }
                getDefaultUIUtil().onDrawOver(
                    c,
                    recyclerView,
                    foregroundView,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                val personDetailViewHolder =
                    (viewHolder as PersonDetailAdapter.PersonDetailViewHolder)
                when (personDetailViewHolder) {
                    is PersonDetailAdapter.PersonDetailViewHolder.TransactionViewHolder-> {
                        val foregroundView =
                            personDetailViewHolder.mBinding.personTransactionCard
                        getDefaultUIUtil().clearView(foregroundView)
                    }
                    is PersonDetailAdapter.PersonDetailViewHolder.DateViewHolder -> {
                        val foregroundView =
                            personDetailViewHolder.mBinding.tvTransactionDate
                        getDefaultUIUtil().clearView(foregroundView)
                    }
                }
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val personDetailViewHolder =
                    viewHolder as PersonDetailAdapter.PersonDetailViewHolder
                when (personDetailViewHolder) {
                    is PersonDetailAdapter.PersonDetailViewHolder.TransactionViewHolder -> {
                        val foregroundView: View =
                            personDetailViewHolder.mBinding.personTransactionCard
                        getDefaultUIUtil().onDraw(
                            c,
                            recyclerView,
                            foregroundView,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    }
                    is PersonDetailAdapter.PersonDetailViewHolder.DateViewHolder -> {
                        // Noop
                    }
                }
            }
        })
    }



    /**
     * Displays a snackbar that allows undoing the operation and on dismissing of the snackbar deletes the item
     */
    private fun deleteTransaction(position: Int) {

        val elementAtPosition = mAdapter.getElementAtPosition(position)

        val transaction =
            (elementAtPosition as PersonDetailAdapter.ListEntry.TransactionEntry).transaction
        val mDeleteSnackbar = Snackbar.make(
            mBinding.list,
            "Removed transaction ${transaction.id}",
            Snackbar.LENGTH_LONG
        )
        mDeleteSnackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE

        // Actually archive the item
        // This causes the list to be updated and the RV to be updated.
        // We do not cancel the scheduled notifications here and only do that only if the action was not undone.

        mAdapter.markTransactionToDelete(position)

        // Undoing the action restores the item from the archive and the RV will be updated automatically
        // Scheduling the notifications is not required since they were not cancelled until undo is no longer possible
        mDeleteSnackbar.setAction(requireContext().getString(R.string.undo_button_label)) {
            mAdapter.restoreTransaction(position)
            Log.d("PersonDetailFragment", "Restoring transaction")
        }

        // Adds a callback that finally actually archives the item when the snackbar times out
        mDeleteSnackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_CONSECUTIVE || event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_MANUAL) {
                    mViewModel.deleteTransaction(transaction)
                }
                super.onDismissed(transientBottomBar, event)
            }
        })
        mDeleteSnackbar.show()
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

    override fun onTransactionClicked(transaction: Transaction) {
        val directions =
            PersonDetailFragmentDirections.actionPersonDetailFragmentToAddTransactionFragment(
                transaction.id,
                title = getString(R.string.fragment_title_edit_transaction),
                referringPersonId = mViewModel.person.id
            )
        findNavController().navigate(directions)
    }
}