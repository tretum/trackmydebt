package com.mmutert.trackmydebt.ui.persondetail

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.databinding.ItemDateBinding
import com.mmutert.trackmydebt.databinding.ItemTransactionPersonBinding
import com.mmutert.trackmydebt.util.FormatHelper
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.Locale

class PersonDetailAdapter(
    val context: Context,
    val viewModel: PersonDetailViewModel,
) :
    RecyclerView.Adapter<PersonDetailAdapter.PersonDetailViewHolder>() {

    private lateinit var transactionToDelete: ListEntry.TransactionEntry
    var entries: MutableList<ListEntry> = ArrayList()

    private val DATE: Int = 1
    private val TRANSACTION = 2

    sealed class ListEntry {
        class TransactionEntry(val transaction: Transaction) : ListEntry()
        class DateEntry(val date: LocalDate) : ListEntry()
    }

    fun setTransactions(transactions: MutableList<Transaction>) {
        val dates: List<LocalDate> = transactions.map { it.date.toLocalDate() }.distinctBy {
            it.toString()
        }
        val result: MutableList<ListEntry> = ArrayList()
        dates.forEach { date ->
            result.add(ListEntry.DateEntry(date))
            transactions.filter { it.date.toLocalDate().isEqual(date) }.forEach { t ->
                result.add(ListEntry.TransactionEntry(t))
            }
        }
        this.entries = result
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonDetailViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            DATE -> {
                val binding = ItemDateBinding.inflate(inflater, parent, false)
                PersonDetailViewHolder.DateViewHolder(binding)
            }
            else -> {
                val binding = ItemTransactionPersonBinding.inflate(inflater, parent, false)
                PersonDetailViewHolder.TransactionViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: PersonDetailViewHolder, position: Int) {
        Log.d(LOG_TAG, "onBindViewHolder for position $position")
        when (val entry = entries[position]) {
            is ListEntry.TransactionEntry -> {
                Log.d(LOG_TAG, "Amount: ${entry.transaction.amount}")

                (holder as PersonDetailViewHolder.TransactionViewHolder).bind(
                    context,
                    viewModel,
                    entry.transaction
                )
            }
            is ListEntry.DateEntry -> {
                val binding = (holder as PersonDetailViewHolder.DateViewHolder).mBinding

                val dateFormatter: DateTimeFormatter =
                    DateTimeFormat.longDate().withLocale(Locale.getDefault())
                binding.tvTransactionDate.text = dateFormatter.print(entry.date)
            }
        }
    }

    override fun getItemCount(): Int =
        entries.size

    override fun getItemViewType(position: Int): Int {
        return when (entries[position]) {
            is ListEntry.TransactionEntry -> TRANSACTION
            is ListEntry.DateEntry -> DATE
        }
    }

    sealed class PersonDetailViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class TransactionViewHolder(val binding: ItemTransactionPersonBinding) :
            PersonDetailViewHolder(binding.root) {

            /**
             * Bind the given transaction to the view holder
             */
            fun bind(context: Context, viewModel: PersonDetailViewModel, transaction: Transaction) {
                val printAsCurrency = FormatHelper.printAsCurrency(transaction.amount)
                val dateFormatter: DateTimeFormatter =
                    DateTimeFormat.shortTime().withLocale(Locale.getDefault())

                binding.apply {
                    this.viewModel = viewModel
                    this.transaction = transaction

                    amountInclude.tvAmount.text = printAsCurrency

                    tvTransactionDirection.text = when (transaction.received) {
                        true -> context.getString(R.string.received_transaction)
                        false -> context.getString(R.string.sent_transaction)
                    }

                    personTransactionCard.strokeColor = ResourcesCompat.getColor(
                        context.resources, R.color.grey_100, null
                    )

                    reasonInclude.apply {
                        when (transaction.reason.isBlank()) {
                            true -> tvTransactionReason.visibility = View.GONE
                            false -> {
                                tvTransactionReason.visibility = View.VISIBLE
                                tvTransactionReason.text = transaction.reason
                            }
                        }
                    }

                    timeInclude.tvTransactionTime.text = dateFormatter.print(transaction.date)
                    personTransactionCard.visibility = View.VISIBLE
                }
            }
        }

        class DateViewHolder(val mBinding: ItemDateBinding) :
            PersonDetailViewHolder(mBinding.root)
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

    companion object {
        private val LOG_TAG = PersonDetailAdapter::class.simpleName
    }
}