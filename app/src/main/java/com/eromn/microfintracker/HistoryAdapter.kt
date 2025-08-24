package com.eromn.microfintracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.eromn.microfintracker.data.Transaction
import com.eromn.microfintracker.databinding.ItemHistoryBinding
import com.eromn.microfintracker.utils.DateUtils

class HistoryAdapter(
    internal var transactions: MutableList<Transaction> = mutableListOf()
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val dateUtils = DateUtils()

    inner class HistoryViewHolder(
        val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            // TODO: change to static
            binding.txtDate.text = dateUtils.formatTimestamp(transaction.timestamp)
            binding.txtType.text = transaction.description

            val context = binding.root.context
            if (transaction.isRead) {
                // Option 1: Change description text color (txtType) to gray
                binding.txtType.setTextColor(
                    ContextCompat.getColor(context, R.color.text_color_read) // Define this color
                )
                // Optionally, dim the whole item slightly
                binding.root.alpha = 0.7f

                // If you want to change other text colors (like the date):
                binding.txtDate.setTextColor(ContextCompat.getColor(context, R.color.text_color_read_secondary))

            } else {
                // Reset to default appearance for unread items

                // Option 1: Reset description text color (txtType) to default
                binding.txtType.setTextColor(
                    ContextCompat.getColor(context, R.color.text_color_default) // Define this color
                )
                // Reset alpha
                binding.root.alpha = 1.0f

                // Reset other text colors if you changed them:
                binding.txtDate.setTextColor(ContextCompat.getColor(context, R.color.text_color_default_secondary))
            }
        }// end bind
    }// end HistoryViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        // TODO: Use DiffUtil instead
        notifyDataSetChanged()
    }
}
