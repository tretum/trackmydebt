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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mmutert.trackmydebt.EventObserver
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.databinding.FragmentPersonDetailBinding
import com.mmutert.trackmydebt.ui.home.PERSON_DELETED_OK
import com.mmutert.trackmydebt.util.FormatHelper
import com.mmutert.trackmydebt.util.IntentHelper
import com.mmutert.trackmydebt.util.setupSnackbar
import java.math.BigDecimal

const val EDIT_RESULT_OK = 1
const val ADD_EDIT_RESULT_OK = 2

class PersonDetailFragment : Fragment(), PersonDetailAdapter.TransactionClickedListener {

    companion object {
        private val LOG_TAG = PersonDetailFragment::class.simpleName
    }

    private lateinit var binding: FragmentPersonDetailBinding
    private lateinit var viewModel: PersonDetailViewModel
    private lateinit var mAdapter: PersonDetailAdapter

    private val args: PersonDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(PersonDetailViewModel::class.java)

        binding = FragmentPersonDetailBinding.inflate(inflater, container, false).apply {
            viewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.loadPerson(args.personId)

        // Set the adapter
        mAdapter = PersonDetailAdapter(requireContext(), this)
        binding.rvTransactionList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
            viewModel.transactions.observe(
                viewLifecycleOwner,
                { mAdapter.setTransactions(it.toMutableList()) })
        }
        createSwipeHelper().attachToRecyclerView(binding.rvTransactionList)

        setupAddTransactionFAB()
        setupPayPalButton()
        setupSnackbar()
        setupNavigation()
    }

    private fun setupAddTransactionFAB() {
        binding.fabAddTransaction.setOnClickListener {
            viewModel.addTransaction()
        }
    }

    /**
     * Set up the PayPal button
     */
    private fun setupPayPalButton() {
        viewModel.sum.observe(viewLifecycleOwner) {
            val formattedSum = FormatHelper.printAsCurrency(it)

            when {
                it > BigDecimal.ZERO -> preparePayPalShareButton(formattedSum)
                it < BigDecimal.ZERO -> preparePayPalPayButton(formattedSum)
                else -> {
                    binding.btPaypal.visibility = View.GONE
                    binding.tvOverallDescription.text = ""
                }
            }
        }
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarTextId, Snackbar.LENGTH_SHORT)
        arguments?.let {
            viewModel.showEditResultMessage(args.userMessage)
        }
    }

    private fun preparePayPalPayButton(formattedSum: String) {
        binding.tvOverallDescription.text = getString(R.string.fragment_person_detail_debt_label)
        binding.btPaypal.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                // Paying = Opening the browser to the paypal.me site
                val intent =
                    IntentHelper.createBrowserIntent("https://paypal.me/${viewModel.person.paypalUserName}/$formattedSum")
                requireActivity().startActivity(intent)
            }
        }
    }

    private fun preparePayPalShareButton(formattedSum: String) {
        binding.tvOverallDescription.text = getString(R.string.fragment_person_detail_credit_label)
        binding.btPaypal.apply {
            visibility = View.VISIBLE
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
                    is PersonDetailAdapter.PersonDetailViewHolder.TransactionViewHolder -> {
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
            binding.rvTransactionList,
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
                    viewModel.deleteTransaction(transaction)
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
                viewModel.removeSelectedPerson()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupNavigation() {
        viewModel.deleteTaskEvent.observe(viewLifecycleOwner) {
            val action = PersonDetailFragmentDirections
                .actionPersonDetailFragmentToNavigationHome(PERSON_DELETED_OK)
            findNavController().navigate(action)
        }
        viewModel.addTransactionEvent.observe(viewLifecycleOwner, EventObserver {
            val directions =
                PersonDetailFragmentDirections.actionPersonDetailFragmentToAddTransactionFragment(
                    title = getString(R.string.fragment_title_new_transaction),
                    referringPersonId = it
                )
            findNavController().navigate(directions)
        })
        viewModel.editTransactionEvent.observe(viewLifecycleOwner, EventObserver {
            val directions =
                PersonDetailFragmentDirections.actionPersonDetailFragmentToAddTransactionFragment(
                    it.first, getString(R.string.fragment_title_edit_transaction), it.second
                )
            findNavController().navigate(directions)
        })
    }

    override fun onTransactionClicked(transaction: Transaction) {
        viewModel.editTransaction(transaction.id)
    }
}