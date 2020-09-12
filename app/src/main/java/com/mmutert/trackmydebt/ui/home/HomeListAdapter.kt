package com.mmutert.trackmydebt.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.data.PersonAndTransactions
import com.mmutert.trackmydebt.databinding.ItemPersonOverviewBinding
import com.mmutert.trackmydebt.util.FormatHelper
import com.mmutert.trackmydebt.util.balance
import java.math.BigDecimal

class HomeListAdapter(
    val context: Context,
    private val viewModel: HomeViewModel
) : ListAdapter<PersonAndTransactions, HomeListAdapter.HomeListViewHolder>(HomeListDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeListViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = ItemPersonOverviewBinding.inflate(inflater, parent, false)

        return HomeListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeListViewHolder, position: Int) {
        val (person, transactions) = getItem(position)
        holder.bind(context, viewModel, person, transactions.balance())
    }

    class HomeListViewHolder(val binding: ItemPersonOverviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(context: Context, viewModel: HomeViewModel, person: Person, balance: BigDecimal) {

            val printAsCurrency = FormatHelper.printAsCurrency(balance)

            binding.apply {
                tvAmount.text = printAsCurrency
                tvName.text = person.name
                listItemForegroundCard.strokeColor =
                    ResourcesCompat.getColor(context.resources, R.color.grey_100, null)

                this.viewModel = viewModel
                this.person = person
            }

            binding.executePendingBindings()
        }
    }

    private class HomeListDiffUtil : DiffUtil.ItemCallback<PersonAndTransactions>() {
        override fun areItemsTheSame(
            oldItem: PersonAndTransactions,
            newItem: PersonAndTransactions
        ): Boolean {
            return oldItem.person.id == newItem.person.id
        }

        override fun areContentsTheSame(
            oldItem: PersonAndTransactions,
            newItem: PersonAndTransactions
        ): Boolean {
            return oldItem.person == newItem.person && oldItem.transactions == newItem.transactions
        }
    }
}