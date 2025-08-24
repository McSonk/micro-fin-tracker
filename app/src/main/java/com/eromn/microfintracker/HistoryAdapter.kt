package com.eromn.microfintracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.databinding.ItemHistoryBinding
import com.eromn.microfintracker.utils.DateUtils

class HistoryAdapter(internal var transactions: MutableList<Transaction> = mutableListOf()) :
    // TODO: Mark as "read"

    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.binding.txtDate.text = DateUtils().formatTimestamp(transaction.timestamp)
        holder.binding.txtType.text = transaction.description
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }
}
