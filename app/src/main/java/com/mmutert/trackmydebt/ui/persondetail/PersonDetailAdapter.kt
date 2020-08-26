package com.mmutert.trackmydebt.ui.persondetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.databinding.DateItemBinding
import com.mmutert.trackmydebt.databinding.PersonDetailItemBinding
import com.mmutert.trackmydebt.util.FormatHelper
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.Locale

class PersonDetailAdapter : RecyclerView.Adapter<PersonDetailAdapter.PersonDetailViewHolder>() {

    var entries: List<ListEntry> = ArrayList()

    private val DATE: Int = 1
    private val TRANSACTION = 2

    sealed class ListEntry {
        class TransactionEntry(val transaction: Transaction) : ListEntry()
        class DateEntry(val date: LocalDate) : ListEntry()
    }

    fun setTransactions(transactions: List<Transaction>) {
        val dates: List<LocalDate> = transactions.map { it.date.toLocalDate() }.distinctBy {
            it.toString()
        }
        val result: ArrayList<ListEntry> = ArrayList()
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
                val binding : DateItemBinding = DataBindingUtil.inflate(inflater, R.layout.date_item, parent, false)
                PersonDetailViewHolder.DateViewHolder(binding)
            }
            else -> {
                val binding: PersonDetailItemBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.person_detail_item,
                    parent,
                    false
                )
                PersonDetailViewHolder.TransactionViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: PersonDetailViewHolder, position: Int) {
        when (val entry = entries[position]) {
            is ListEntry.TransactionEntry -> {
                val binding = (holder as PersonDetailViewHolder.TransactionViewHolder).mBinding
                val printAsCurrency = FormatHelper.printAsCurrency(entry.transaction.amount)
                binding.tvAmount.text = printAsCurrency
                binding.tvReason.text = entry.transaction.reason
            }
            is ListEntry.DateEntry -> {
                val binding = (holder as PersonDetailViewHolder.DateViewHolder).mBinding

                val dateFormatter: DateTimeFormatter =
                    DateTimeFormat.longDate().withLocale(Locale.getDefault())
                binding.tvTransactionDate.text = dateFormatter.print(entry.date)
            }
        }
    }

    override fun getItemCount(): Int = entries.size

    override fun getItemViewType(position: Int): Int {
        return when (entries[position]) {
            is ListEntry.TransactionEntry -> TRANSACTION
            is ListEntry.DateEntry -> DATE
        }
    }

    sealed class PersonDetailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class TransactionViewHolder(val mBinding: PersonDetailItemBinding) :
            PersonDetailViewHolder(mBinding.root)

        class DateViewHolder(val mBinding: DateItemBinding) :
            PersonDetailViewHolder(mBinding.root)
    }
}