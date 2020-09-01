package com.mmutert.trackmydebt.ui.dashboard

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.TransactionAndPerson
import com.mmutert.trackmydebt.databinding.FragmentRecentActivityBinding
import com.mmutert.trackmydebt.databinding.ItemDateBinding
import com.mmutert.trackmydebt.databinding.ItemRecentActivityBinding
import com.mmutert.trackmydebt.util.FormatHelper
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.Locale

class RecentActivityFragment : Fragment() {

    private lateinit var mViewModel: RecentActivityViewModel
    private lateinit var mBinding: FragmentRecentActivityBinding

    private lateinit var mAdapter: RecentActivityListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel =
            ViewModelProvider(this).get(RecentActivityViewModel::class.java)

        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_recent_activity,
            container,
            false
        )

        mAdapter = RecentActivityListAdapter(mViewModel, requireContext())
        mBinding.rvRecentActivityList.adapter = mAdapter
        mBinding.rvRecentActivityList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        createSwipeHelper().attachToRecyclerView(mBinding.rvRecentActivityList)

        mViewModel.transactionAndPerson.observe(
            viewLifecycleOwner,
            { mAdapter.setTransactions(it) })

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
                        (viewHolder as RecentActivityListAdapter.RecentActivityListViewHolder)
                    when (personDetailViewHolder) {
                        is RecentActivityListAdapter.RecentActivityListViewHolder.TransactionViewHolder -> {
                            getDefaultUIUtil().onSelected(personDetailViewHolder.mBinding.recentActivityCard)
                        }
                        is RecentActivityListAdapter.RecentActivityListViewHolder.DateViewHolder -> {
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
                    (viewHolder as RecentActivityListAdapter.RecentActivityListViewHolder)
                lateinit var foregroundView: View
                when (personDetailViewHolder) {
                    is RecentActivityListAdapter.RecentActivityListViewHolder.TransactionViewHolder -> {
                        foregroundView = personDetailViewHolder.mBinding.recentActivityCard
                    }
                    is RecentActivityListAdapter.RecentActivityListViewHolder.DateViewHolder -> {
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
                    (viewHolder as RecentActivityListAdapter.RecentActivityListViewHolder)
                when (personDetailViewHolder) {
                    is RecentActivityListAdapter.RecentActivityListViewHolder.TransactionViewHolder -> {
                        val foregroundView =
                            personDetailViewHolder.mBinding.recentActivityCard
                        getDefaultUIUtil().clearView(foregroundView)
                    }
                    is RecentActivityListAdapter.RecentActivityListViewHolder.DateViewHolder -> {
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
                    viewHolder as RecentActivityListAdapter.RecentActivityListViewHolder
                when (personDetailViewHolder) {
                    is RecentActivityListAdapter.RecentActivityListViewHolder.TransactionViewHolder -> {
                        val foregroundView: View =
                            personDetailViewHolder.mBinding.recentActivityCard
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
                    is RecentActivityListAdapter.RecentActivityListViewHolder.DateViewHolder -> {
                        // Noop
                    }
                }
            }
        })
    }

    /**
     * Displays a snackbar that allows undoing the operation and on dismissing of the snackbar deletes the item
     *
     * @param itemToArchive The item to archive.
     */
    private fun deleteTransaction(position: Int) {

        val elementAtPosition = mAdapter.getElementAtPosition(position)

        val transactionAndPerson =
            (elementAtPosition as RecentActivityListAdapter.ListEntry.TransactionEntry).transaction
        val mDeleteSnackbar = Snackbar.make(
            mBinding.rvRecentActivityList,
            // TODO Update message
            "Removed transaction ${transactionAndPerson.transaction.id}",
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
                    mViewModel.deleteTransaction(transactionAndPerson.transaction)
                }
                super.onDismissed(transientBottomBar, event)
            }
        })
        mDeleteSnackbar.show()
    }

    class RecentActivityListAdapter(val viewModel: RecentActivityViewModel, val context: Context) :
        RecyclerView.Adapter<RecentActivityListAdapter.RecentActivityListViewHolder>() {

        private lateinit var transactionToDelete: ListEntry.TransactionEntry
        private val LOG_TAG: String = "RecentActivityListAdapter"
        private val DATE: Int = 1
        private val TRANSACTION = 2

        sealed class ListEntry {
            class TransactionEntry(val transaction: TransactionAndPerson) : ListEntry()
            class DateEntry(val date: LocalDate) : ListEntry()
        }

        var entries: MutableList<ListEntry> = ArrayList()
            private set

        fun setTransactions(transactions: List<TransactionAndPerson>) {
            val dates: List<LocalDate> =
                transactions.map { it.transaction.date.toLocalDate() }.distinctBy {
                    it.toString()
                }
            val result: ArrayList<ListEntry> = ArrayList()
            dates.forEach { date ->
                result.add(ListEntry.DateEntry(date))
                transactions.filter { it.transaction.date.toLocalDate().isEqual(date) }
                    .forEach { t ->
                        result.add(ListEntry.TransactionEntry(t))
                    }
            }
            this.entries = result
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return when (entries[position]) {
                is ListEntry.TransactionEntry -> TRANSACTION
                is ListEntry.DateEntry -> DATE
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecentActivityListViewHolder {
            val inflater = LayoutInflater.from(parent.context)

            when (viewType) {
                DATE -> {
                    val inflate: ItemDateBinding =
                        DataBindingUtil.inflate(inflater, R.layout.item_date, parent, false)
                    return RecentActivityListViewHolder.DateViewHolder(inflate)
                }
                TRANSACTION -> {
                    val binding = ItemRecentActivityBinding.inflate(inflater, parent, false)
                    return RecentActivityListViewHolder.TransactionViewHolder(binding)
                }
                else -> return RecentActivityListViewHolder.DateViewHolder(
                    DataBindingUtil.inflate(
                        inflater,
                        R.layout.item_date,
                        parent,
                        false
                    )
                )
            }
        }

        override fun onBindViewHolder(holder: RecentActivityListViewHolder, position: Int) {
            when (holder) {
                is RecentActivityListViewHolder.TransactionViewHolder -> {
                    val listEntry = (entries[position] as ListEntry.TransactionEntry).transaction
                    val (id, partnerId, received, amount, date, reason) = listEntry.transaction

                    val dateFormatter: DateTimeFormatter =
                        DateTimeFormat.shortTime().withLocale(Locale.getDefault())
                    holder.mBinding.timeInclude.tvTransactionTime.text = dateFormatter.print(date)
                    holder.mBinding.amountInclude.tvAmount.text =
                        FormatHelper.printAsCurrency(amount)
                    when {
                        reason.isBlank() -> {
                            holder.mBinding.reasonInclude.tvTransactionReason.visibility = View.GONE
                        }
                        else -> {
                            holder.mBinding.reasonInclude.tvTransactionReason.visibility =
                                View.VISIBLE
                            holder.mBinding.reasonInclude.tvTransactionReason.text = reason
                        }
                    }

                    when (received) {
                        true -> {
                            holder.mBinding.recentActivityCard.strokeColor =
                                context.resources.getColor(
                                    R.color.positive_100
                                )
                        }
                        else -> {
                            holder.mBinding.recentActivityCard.strokeColor =
                                context.resources.getColor(
                                    R.color.negative_100
                                )
                        }
                    }

                    val (_, name) = listEntry.person
                    holder.mBinding.tvName.text = name
                }

                is RecentActivityListViewHolder.DateViewHolder -> {
                    val dateFormatter: DateTimeFormatter =
                        DateTimeFormat.longDate().withLocale(Locale.getDefault())

                    holder.mBinding.tvTransactionDate.text =
                        dateFormatter.print((entries[position] as ListEntry.DateEntry).date)
                }
            }
        }

        override fun getItemCount(): Int {
            return entries.size
        }

        fun markTransactionToDelete(position: Int) {
            Log.d(LOG_TAG, "Deleting transaction")
            this.transactionToDelete = entries[position] as ListEntry.TransactionEntry
            this.entries.removeAt(position)
            notifyItemRemoved(position)
        }

        fun restoreTransaction(position: Int) {
            Log.d(LOG_TAG, "Restoring transaction")
            this.entries.add(position, this.transactionToDelete)
            notifyItemInserted(position)
        }

        fun getElementAtPosition(position: Int): ListEntry {
            return entries[position]
        }

        sealed class RecentActivityListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            class TransactionViewHolder(val mBinding: ItemRecentActivityBinding) :
                RecentActivityListViewHolder(mBinding.root)

            class DateViewHolder(val mBinding: ItemDateBinding) :
                RecentActivityListViewHolder(mBinding.root)
        }
    }
}