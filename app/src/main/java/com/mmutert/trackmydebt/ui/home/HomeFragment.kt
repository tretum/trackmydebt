package com.mmutert.trackmydebt.ui.home

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.databinding.DebtListItemBinding
import com.mmutert.trackmydebt.databinding.FragmentHomeBinding
import com.mmutert.trackmydebt.model.PersonModel
import com.mmutert.trackmydebt.ui.addperson.AddPersonDialogFragment
import com.mmutert.trackmydebt.ui.dialogs.TransactionDialogFragment

class HomeFragment : Fragment(), AddPersonDialogFragment.PersonAddedListener {

    private lateinit var mViewModel: HomeViewModel
    private lateinit var mBinding: FragmentHomeBinding

    private lateinit var mAdapter: DebtListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )

        mBinding.rvDebtList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        mAdapter = DebtListAdapter(requireContext())
        mAdapter.setList(ArrayList())
        mBinding.rvDebtList.adapter = mAdapter
        mBinding.rvDebtList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        mBinding.fabAddItem.setOnClickListener {
            AddPersonDialogFragment(this).show(parentFragmentManager, "AddPerson")
        }

        mViewModel.persons.observe(viewLifecycleOwner, {
            mAdapter.setList(it)
        })

        val createSwipeHelper = createSwipeHelper()
        createSwipeHelper.attachToRecyclerView(mBinding.rvDebtList)

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
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Delete the item
                val pos = viewHolder.adapterPosition
                val partner = mAdapter.getElementAtPosition(pos)

                if (direction == ItemTouchHelper.RIGHT) {
                    TransactionDialogFragment(
                        false,
                        object : TransactionDialogFragment.TransactionConfirmedListener {
                            override fun transactionConfirmed(amount: Long, receiving: Boolean) {
                                mViewModel.giveMoney(partner, amount, "")
                            }
                        }).show(parentFragmentManager, "GiveMoney")
                } else if (direction == ItemTouchHelper.LEFT) {
                    TransactionDialogFragment(
                        true,
                        object : TransactionDialogFragment.TransactionConfirmedListener {
                            override fun transactionConfirmed(amount: Long, receiving: Boolean) {
                                mViewModel.receiveMoney(partner, amount, "")
                            }
                        }).show(parentFragmentManager, "ReceiveMoney")
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (viewHolder != null) {
                    val binding = (viewHolder as DebtListAdapter.DebtListViewHolder).binding
                    val foregroundView: View = binding.listItemForeground
                    getDefaultUIUtil().onSelected(foregroundView)
                }
            }

            override fun onChildDrawOver(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {

                val binding = (viewHolder as DebtListAdapter.DebtListViewHolder).binding
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
                val binding = (viewHolder as DebtListAdapter.DebtListViewHolder).binding
                val foregroundView: View = binding.listItemForeground
                getDefaultUIUtil().clearView(foregroundView)
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val binding = (viewHolder as DebtListAdapter.DebtListViewHolder).binding
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
        })
    }

    class DebtListAdapter(val context: Context) :
        RecyclerView.Adapter<DebtListAdapter.DebtListViewHolder>() {

        // private val mDiffer = AsyncListDiffer(this, DIFF_CALLBACK)
        private lateinit var list: List<PersonModel>

        fun setList(list: List<PersonModel>) {
            this.list = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtListViewHolder {
            val inflater = LayoutInflater.from(parent.context)

            val binding: DebtListItemBinding =
                DataBindingUtil.inflate(inflater, R.layout.debt_list_item, parent, false)

            return DebtListViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DebtListViewHolder, position: Int) {
            val (id, name, sum) = list[position]

            holder.binding.tvAmount.text = "$sum"
            holder.binding.tvName.text = name
            when {
                sum == 0L -> {
                    holder.binding.listItemForeground.setBackgroundColor(context.resources.getColor(R.color.white))
                }
                sum > 0L -> {
                    holder.binding.listItemForeground.setBackgroundColor(context.resources.getColor(R.color.debt_item_card_background_positive))
                }
                sum < 0L -> {
                    holder.binding.listItemForeground.setBackgroundColor(context.resources.getColor(R.color.debt_item_card_background_negative))
                }
            }
        }

        fun getElementAtPosition(position: Int): PersonModel {
            return list[position]
        }

        override fun getItemCount(): Int {
            return list.size
        }

        class DebtListViewHolder(val binding: DebtListItemBinding) :
            RecyclerView.ViewHolder(binding.root)

        // companion object {
        //     /**
        //      * The Callback for the DiffUtil.
        //      */
        //     private val DIFF_CALLBACK: DiffUtil.ItemCallback<FrozenItem> =
        //         object : DiffUtil.ItemCallback<FrozenItem>() {
        //             override fun areItemsTheSame(
        //                 oldFrozenItem: FrozenItem, newFrozenItem: FrozenItem): Boolean {
        //                 // FrozenItem properties may have changed if reloaded from the DB, but ID is fixed
        //                 return oldFrozenItem.id == newFrozenItem.id
        //             }
        //
        //             override fun areContentsTheSame(
        //                 oldFrozenItem: FrozenItem, newFrozenItem: FrozenItem): Boolean {
        //                 // NOTE: if you use equals, your object must properly override Object#equals()
        //                 // Incorrectly returning false here will result in too many animations.
        //                 return oldFrozenItem == newFrozenItem
        //             }
        //         }
        // }
    }

    override fun personAdded(name: String) {
        mViewModel.addPerson(name)
    }
}