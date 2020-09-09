package com.mmutert.trackmydebt.ui.persondetail

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.databinding.ItemDateBinding
import com.mmutert.trackmydebt.databinding.ItemPersonDetailBinding
import com.mmutert.trackmydebt.util.FormatHelper
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.Locale

class PersonDetailAdapter(val context: Context, val transactionClickedListener: TransactionClickedListener) :
    RecyclerView.Adapter<PersonDetailAdapter.PersonDetailViewHolder>() {

    private val LOG_TAG: String = "PersonDetailAdapter"
    private lateinit var transactionToDelete: ListEntry.TransactionEntry
    var entries: MutableList<ListEntry> = ArrayList()

    private val DATE: Int = 1
    private val TRANSACTION = 2

    interface TransactionClickedListener {
        fun onTransactionClicked(transaction: Transaction)
    }

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
                val binding: ItemDateBinding =
                    DataBindingUtil.inflate(inflater, R.layout.item_date, parent, false)
                PersonDetailViewHolder.DateViewHolder(binding)
            }
            else -> {
                val binding: ItemPersonDetailBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_person_detail,
                    parent,
                    false
                )
                PersonDetailViewHolder.TransactionViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: PersonDetailViewHolder, position: Int) {
        Log.d(LOG_TAG, "onBindViewHolder for position $position")
        when (val entry = entries[position]) {
            is ListEntry.TransactionEntry -> {
                Log.d(LOG_TAG, "Amount: ${entry.transaction.amount}")
                val binding = (holder as PersonDetailViewHolder.TransactionViewHolder).mBinding
                val printAsCurrency = FormatHelper.printAsCurrency(entry.transaction.amount)
                binding.amountInclude.tvAmount.text = printAsCurrency

                when (entry.transaction.reason.isBlank()) {
                    true -> binding.reasonInclude.tvTransactionReason.visibility = View.GONE
                    false -> {
                        binding.reasonInclude.tvTransactionReason.visibility = View.VISIBLE
                        binding.reasonInclude.tvTransactionReason.text = entry.transaction.reason
                    }
                }

                binding.personTransactionCard.strokeColor = context.resources.getColor(
                    R.color.grey_100
                )
                when (entry.transaction.received) {
                    true -> {
                        binding.tvTransactionDirection.text =
                            context.getString(R.string.received_transaction)
                    }
                    false -> {
                        binding.tvTransactionDirection.text =
                            context.getString(R.string.sent_transaction)
                    }
                }

                binding.root.setOnClickListener {
                    Log.d(LOG_TAG, "Clicked on item at position $position")
                    transactionClickedListener.onTransactionClicked(entry.transaction)
                }

                val dateFormatter: DateTimeFormatter =
                    DateTimeFormat.shortTime().withLocale(Locale.getDefault())
                binding.timeInclude.tvTransactionTime.text = dateFormatter.print(entry.transaction.date)
                binding.personTransactionCard.visibility = View.VISIBLE
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
        class TransactionViewHolder(val mBinding: ItemPersonDetailBinding) :
            PersonDetailViewHolder(mBinding.root)

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
}