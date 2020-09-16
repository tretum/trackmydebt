package com.mmutert.trackmydebt.ui.recentactivity

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.data.TransactionAndPerson
import com.mmutert.trackmydebt.databinding.ItemDateBinding
import com.mmutert.trackmydebt.databinding.ItemTransactionRecentActivityBinding
import com.mmutert.trackmydebt.util.FormatHelper
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.Locale

class RecentActivityListAdapter(
    val context: Context,
    private val viewModel: RecentActivityViewModel
) :
    RecyclerView.Adapter<RecentActivityListAdapter.RecentActivityListViewHolder>() {

    private lateinit var transactionToDelete: ListEntry.TransactionEntry
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

        return when (viewType) {
            DATE -> {
                val inflate = ItemDateBinding.inflate(inflater, parent, false)
                RecentActivityListViewHolder.DateViewHolder(inflate)
            }
            TRANSACTION -> {
                val binding =
                    ItemTransactionRecentActivityBinding.inflate(inflater, parent, false)
                RecentActivityListViewHolder.TransactionViewHolder(binding)
            }
            else -> RecentActivityListViewHolder.DateViewHolder(
                ItemDateBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecentActivityListViewHolder, position: Int) {
        when (holder) {
            is RecentActivityListViewHolder.TransactionViewHolder -> {
                val listEntry = (entries[position] as ListEntry.TransactionEntry).transaction
                holder.bind(context, viewModel, listEntry.transaction, listEntry.person)
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
        class TransactionViewHolder(val binding: ItemTransactionRecentActivityBinding) :
            RecentActivityListViewHolder(binding.root) {
            fun bind(context: Context, viewModel: RecentActivityViewModel, transaction: Transaction, person: Person) {
                val (id, partnerId, received, amount, date, action, reason, reasonLong) = transaction

                binding.apply {
                    this.viewModel = viewModel
                    this.transaction = transaction
                    this.person = person
                }

                val dateFormatter: DateTimeFormatter =
                    DateTimeFormat.shortTime().withLocale(Locale.getDefault())
                binding.timeInclude.tvTransactionTime.text = dateFormatter.print(date)
                binding.amountInclude.tvAmount.text =
                    FormatHelper.printAsCurrency(amount)

                binding.recentActivityCard.strokeColor =
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.grey_100,
                        null
                    )
            }
        }

        class DateViewHolder(val mBinding: ItemDateBinding) :
            RecentActivityListViewHolder(mBinding.root)
    }

    companion object {
        private val LOG_TAG = RecentActivityListAdapter::class.simpleName
    }
}