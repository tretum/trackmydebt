package com.mmutert.trackmydebt.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.databinding.DebtListItemBinding
import com.mmutert.trackmydebt.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

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
        val debtListAdapter = DebtListAdapter()
        debtListAdapter.setList(ArrayList())
        mBinding.rvDebtList.adapter = debtListAdapter

        mBinding.fabAddItem.setOnClickListener {
            // TODO Remove temporary action
            homeViewModel.addDemoTransaction()
        }

        return mBinding.root
    }

    class DebtListAdapter : RecyclerView.Adapter<DebtListAdapter.DebtListViewHolder>() {

        // private val mDiffer = AsyncListDiffer(this, DIFF_CALLBACK)
        private lateinit var list: List<Person>

        fun setList(list: List<Person>) {
            this.list = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtListViewHolder {
            val inflater = LayoutInflater.from(parent.context)

            return DataBindingUtil.inflate(inflater, R.layout.debt_list_item, parent, false)
        }

        override fun onBindViewHolder(holder: DebtListViewHolder, position: Int) {
            // TODO
        }

        override fun getItemCount(): Int {
            return list.size
        }

        class DebtListViewHolder(private val binding : DebtListItemBinding) : RecyclerView.ViewHolder(binding.root)

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
}