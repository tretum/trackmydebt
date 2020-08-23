package com.mmutert.trackmydebt.ui.dashboard

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
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.databinding.FragmentRecentActivityBinding
import com.mmutert.trackmydebt.databinding.RecentActivityItemBinding
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.Locale

class RecentActivityFragment : Fragment() {

    private lateinit var mViewModel: RecentActivityViewModel
    private lateinit var mBinding: FragmentRecentActivityBinding

    private val mAdapter = RecentActivityListAdapter()

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

        mBinding.rvRecentActivityList.adapter = mAdapter
        mBinding.rvRecentActivityList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        mBinding.rvRecentActivityList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        mViewModel.transactions.observe(viewLifecycleOwner, { mAdapter.setTransactions(it) })

        return mBinding.root
    }

    class RecentActivityListAdapter :
        RecyclerView.Adapter<RecentActivityListAdapter.RecentActivityListViewHolder>() {

        var transactions: List<Transaction> = ArrayList()
            private set

        fun setTransactions(transactions: List<Transaction>) {
            this.transactions = transactions
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecentActivityListViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = RecentActivityItemBinding.inflate(inflater, parent, false)
            return RecentActivityListViewHolder(binding)
        }

        override fun onBindViewHolder(holder: RecentActivityListViewHolder, position: Int) {
            val (id, partnerId, received, amount, date, reason) = transactions[position]

            val dateFormatter: DateTimeFormatter =
                DateTimeFormat.longDate().withLocale(Locale.getDefault())

            holder.mBinding.tvAmount.text = "$amount"
            holder.mBinding.tvReason.text = reason
            holder.mBinding.tvTransactionDate.text = dateFormatter.print(date)
            // TODO Replace with actual name
            holder.mBinding.tvName.text = "$partnerId"

            // TODO Change color if received or sent?
        }

        override fun getItemCount(): Int {
            return transactions.size
        }

        class RecentActivityListViewHolder(
            val mBinding: RecentActivityItemBinding
        ) : RecyclerView.ViewHolder(mBinding.root)
    }
}