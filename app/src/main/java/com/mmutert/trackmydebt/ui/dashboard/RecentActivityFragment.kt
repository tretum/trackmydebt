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
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.data.TransactionAndPerson
import com.mmutert.trackmydebt.databinding.DateItemBinding
import com.mmutert.trackmydebt.databinding.FragmentRecentActivityBinding
import com.mmutert.trackmydebt.databinding.RecentActivityItemBinding
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

        mAdapter =  RecentActivityListAdapter(mViewModel)
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

        mViewModel.transactionAndPerson.observe(viewLifecycleOwner, { mAdapter.setTransactions(it) })

        return mBinding.root
    }

    class RecentActivityListAdapter(val viewModel : RecentActivityViewModel) :
        RecyclerView.Adapter<RecentActivityListAdapter.RecentActivityListViewHolder>() {

        private val DATE: Int = 1
        private val TRANSACTION = 2

        sealed class ListEntry {
            class TransactionEntry(val transaction: TransactionAndPerson) : ListEntry()
            class DateEntry(val date: LocalDate) : ListEntry()
        }

        var entries: List<ListEntry> = ArrayList()
            private set

        fun setTransactions(transactions: List<TransactionAndPerson>) {
            val dates: List<LocalDate> = transactions.map { it.transaction.date.toLocalDate() }.distinctBy {
                it.toString()
            }
            val result: ArrayList<ListEntry> = ArrayList()
            dates.forEach { date ->
                result.add(ListEntry.DateEntry(date))
                transactions.filter { it.transaction.date.toLocalDate().isEqual(date) }.forEach { t ->
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
                    val inflate: DateItemBinding =
                        DataBindingUtil.inflate(inflater, R.layout.date_item, parent, false)
                    return RecentActivityListViewHolder.DateViewHolder(inflate)
                }
                TRANSACTION -> {
                    val binding = RecentActivityItemBinding.inflate(inflater, parent, false)
                    return RecentActivityListViewHolder.TransactionViewHolder(binding)
                }
                else -> return RecentActivityListViewHolder.DateViewHolder(
                    DataBindingUtil.inflate(
                        inflater,
                        R.layout.date_item,
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
                    holder.mBinding.tvTransactionDate.text = dateFormatter.print(date)
                    holder.mBinding.tvAmount.text = FormatHelper.printAsCurrency(amount)
                    if (reason.isBlank()) {
                        holder.mBinding.tvReason.visibility = View.GONE
                    } else {
                        holder.mBinding.tvReason.visibility = View.VISIBLE
                        holder.mBinding.tvReason.text = reason
                    }
                    // TODO Replace with actual name
                    val (_, name) = listEntry.person
                    holder.mBinding.tvName.text = name

                    // TODO Change color if received or sent?
                }
                is RecentActivityListViewHolder.DateViewHolder -> {
                    val dateFormatter: DateTimeFormatter =
                        DateTimeFormat.longDate().withLocale(Locale.getDefault())

                    holder.binding.tvTransactionDate.text =
                        dateFormatter.print((entries[position] as ListEntry.DateEntry).date)
                }
            }
        }

        override fun getItemCount(): Int {
            return entries.size
        }

        sealed class RecentActivityListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            class TransactionViewHolder(val mBinding: RecentActivityItemBinding) :
                RecentActivityListViewHolder(mBinding.root)

            class DateViewHolder(val binding: DateItemBinding) :
                RecentActivityListViewHolder(binding.root)
        }
    }
}