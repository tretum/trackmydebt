package com.mmutert.trackmydebt.ui.persondetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Transaction
import com.mmutert.trackmydebt.databinding.PersonDetailItemBinding
import com.mmutert.trackmydebt.util.FormatHelper
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.Locale

class PersonDetailAdapter : RecyclerView.Adapter<PersonDetailAdapter.ViewHolder>() {

    var transactions: List<Transaction> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:PersonDetailItemBinding = DataBindingUtil.inflate(inflater, R.layout.person_detail_item, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (id, partnerId, received, amount, date, reason) = transactions[position]

        val dateFormatter: DateTimeFormatter =
            DateTimeFormat.longDate().withLocale(Locale.getDefault())

        val printAsCurrency = FormatHelper.printAsCurrency(amount)
        holder.binding.tvAmount.text = printAsCurrency
        holder.binding.tvTransactionDate.text = dateFormatter.print(date)
        holder.binding.tvReason.text = reason
    }

    override fun getItemCount(): Int = transactions.size

    class ViewHolder(val binding: PersonDetailItemBinding) : RecyclerView.ViewHolder(binding.root)
}