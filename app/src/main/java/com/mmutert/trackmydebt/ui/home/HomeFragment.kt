package com.mmutert.trackmydebt.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.databinding.DebtListItemBinding
import com.mmutert.trackmydebt.databinding.FragmentHomeBinding
import com.mmutert.trackmydebt.model.PersonModel
import com.mmutert.trackmydebt.ui.addperson.AddPersonDialogFragment

class HomeFragment : Fragment(), AddPersonDialogFragment.PersonAddedListener {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mBinding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )

        mBinding.rvDebtList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val debtListAdapter = DebtListAdapter(requireContext())
        debtListAdapter.setList(ArrayList())
        mBinding.rvDebtList.adapter = debtListAdapter
        mBinding.rvDebtList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        mBinding.fabAddItem.setOnClickListener {
            // TODO Remove temporary action

            AddPersonDialogFragment(this).show(parentFragmentManager, "AddPerson")

            // homeViewModel.addDemoTransaction()
        }

        homeViewModel.persons.observe(viewLifecycleOwner, {
            debtListAdapter.setList(it)
        })

        return mBinding.root
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
                    holder.binding.debtItemLayout.setBackgroundColor(context.resources.getColor(R.color.white))
                }
                sum > 0L -> {
                    holder.binding.debtItemLayout.setBackgroundColor(context.resources.getColor(R.color.debt_item_card_background_positive))
                }
                sum < 0L -> {
                    holder.binding.debtItemLayout.setBackgroundColor(context.resources.getColor(R.color.debt_item_card_background_negative))
                }
            }
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
        homeViewModel.addPerson(name)
    }
}