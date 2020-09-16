package com.mmutert.trackmydebt.ui.recentactivity

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.databinding.FragmentRecentActivityBinding
import com.mmutert.trackmydebt.util.getViewModelFactory

class RecentActivityFragment : Fragment() {

    private val viewModel: RecentActivityViewModel by viewModels { getViewModelFactory() }
    private lateinit var binding: FragmentRecentActivityBinding

    private lateinit var adapter: RecentActivityListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_recent_activity,
            container,
            false
        )

        adapter = RecentActivityListAdapter(requireContext(), viewModel)
        binding.rvRecentActivityList.adapter = adapter
        binding.rvRecentActivityList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        createSwipeHelper().attachToRecyclerView(binding.rvRecentActivityList)

        viewModel.transactionAndPerson.observe(
            viewLifecycleOwner,
            { adapter.setTransactions(it) })

        return binding.root
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
                    adapter.notifyItemChanged(pos)
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (viewHolder != null) {
                    val personDetailViewHolder =
                        (viewHolder as RecentActivityListAdapter.RecentActivityListViewHolder)
                    when (personDetailViewHolder) {
                        is RecentActivityListAdapter.RecentActivityListViewHolder.TransactionViewHolder -> {
                            getDefaultUIUtil().onSelected(personDetailViewHolder.binding.recentActivityCard)
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
                val foregroundView = when (personDetailViewHolder) {
                    is RecentActivityListAdapter.RecentActivityListViewHolder.TransactionViewHolder -> {
                        personDetailViewHolder.binding.recentActivityCard
                    }
                    is RecentActivityListAdapter.RecentActivityListViewHolder.DateViewHolder -> {
                        personDetailViewHolder.mBinding.tvTransactionDate
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
                            personDetailViewHolder.binding.recentActivityCard
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
                            personDetailViewHolder.binding.recentActivityCard
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

        val elementAtPosition = adapter.getElementAtPosition(position)

        val transactionAndPerson =
            (elementAtPosition as RecentActivityListAdapter.ListEntry.TransactionEntry).transaction
        val mDeleteSnackbar = Snackbar.make(
            binding.rvRecentActivityList,
            "Removed transaction",
            Snackbar.LENGTH_LONG
        )
        mDeleteSnackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE

        // Actually archive the item
        // This causes the list to be updated and the RV to be updated.
        // We do not cancel the scheduled notifications here and only do that only if the action was not undone.

        adapter.markTransactionToDelete(position)

        // Undoing the action restores the item from the archive and the RV will be updated automatically
        // Scheduling the notifications is not required since they were not cancelled until undo is no longer possible
        mDeleteSnackbar.setAction(requireContext().getString(R.string.undo_button_label)) {
            adapter.restoreTransaction(position)
            Log.d("PersonDetailFragment", "Restoring transaction")
        }

        // Adds a callback that finally actually archives the item when the snackbar times out
        mDeleteSnackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_CONSECUTIVE || event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_MANUAL) {
                    viewModel.deleteTransaction(transactionAndPerson.transaction)
                }
                super.onDismissed(transientBottomBar, event)
            }
        })
        mDeleteSnackbar.show()
    }
}