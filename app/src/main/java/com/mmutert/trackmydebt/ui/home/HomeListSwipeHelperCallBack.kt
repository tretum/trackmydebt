package com.mmutert.trackmydebt.ui.home

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Creates the ItemTouchHelper that archives items in the item list on swipe to the right.
 *
 * @return The item touch helper
 */
class HomeListSwipeHelperCallBack(private val mAdapter: HomeListAdapter) :
    ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.START or ItemTouchHelper.END
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
        val partner = mAdapter.currentList[pos].person

        if (direction == ItemTouchHelper.RIGHT) {
            // TODO Delete transaction on swipe
        } else if (direction == ItemTouchHelper.LEFT) {
            // TODO Show transaction editor on swipe
        }
        mAdapter.notifyItemChanged(pos)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (viewHolder != null) {
            val binding = (viewHolder as HomeListAdapter.HomeListViewHolder).binding
            val foregroundView: View = binding.listItemForeground
            getDefaultUIUtil().onSelected(foregroundView)
        }
    }

    override fun onChildDrawOver(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {

        val binding = (viewHolder as HomeListAdapter.HomeListViewHolder).binding
        val giveMoneyBackground: View = binding.listItemGiveMoneyBackground
        val receiveMoneyBackground: View = binding.listItemReceiveMoneyBackground
        when {
            dX < 0 -> {
                giveMoneyBackground.visibility = View.INVISIBLE
                receiveMoneyBackground.visibility = View.VISIBLE
            }
            dX > 0 -> {
                giveMoneyBackground.visibility = View.VISIBLE
                receiveMoneyBackground.visibility = View.INVISIBLE
            }
            else -> {
                giveMoneyBackground.visibility = View.INVISIBLE
                receiveMoneyBackground.visibility = View.INVISIBLE
            }
        }
        val foregroundView: View = binding.listItemForeground
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
        val binding = (viewHolder as HomeListAdapter.HomeListViewHolder).binding
        val foregroundView: View = binding.listItemForeground
        getDefaultUIUtil().clearView(foregroundView)
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ) {
        val binding = (viewHolder as HomeListAdapter.HomeListViewHolder).binding
        val foregroundView: View = binding.listItemForeground
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
}