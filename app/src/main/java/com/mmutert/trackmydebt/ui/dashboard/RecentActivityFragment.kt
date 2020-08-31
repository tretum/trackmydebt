package com.mmutert.trackmydebt.ui.dashboard

import android.content.Context
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

        mAdapter =  RecentActivityListAdapter(mViewModel, requireContext())
        mBinding.rvRecentActivityList.adapter = mAdapter
        mBinding.rvRecentActivityList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        mViewModel.transactionAndPerson.observe(viewLifecycleOwner, { mAdapter.setTransactions(it) })

        return mBinding.root
    }

    class RecentActivityListAdapter(val viewModel : RecentActivityViewModel, val context: Context) :
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
                    holder.mBinding.amountInclude.tvAmount.text = FormatHelper.printAsCurrency(amount)
                    when {
                        reason.isBlank() -> {
                            holder.mBinding.reasonInclude.tvTransactionReason.visibility = View.GONE
                        }
                        else -> {
                            holder.mBinding.reasonInclude.tvTransactionReason.visibility = View.VISIBLE
                            holder.mBinding.reasonInclude.tvTransactionReason.text = reason
                        }
                    }

                    when (received) {
                        true -> {
                            holder.mBinding.recentActivityCard.strokeColor = context.resources.getColor(
                                R.color.positive_100
                            )
                        }
                        else -> {
                            holder.mBinding.recentActivityCard.strokeColor = context.resources.getColor(
                                R.color.negative_100
                            )
                        }
                    }

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
            class TransactionViewHolder(val mBinding: ItemRecentActivityBinding) :
                RecentActivityListViewHolder(mBinding.root)

            class DateViewHolder(val binding: ItemDateBinding) :
                RecentActivityListViewHolder(binding.root)
        }
    }
}