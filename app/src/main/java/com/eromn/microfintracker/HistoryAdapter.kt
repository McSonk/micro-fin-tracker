package com.eromn.microfintracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eromn.microfintracker.databinding.ItemHistoryBinding

class HistoryAdapter(private val trips: List<Trip>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)
    private lateinit var txDataSource: TxDataSource

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        txDataSource = TxDataSource(parent.context)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val trip = trips[position]
        holder.binding.txtDate.text = txDataSource.formatTimestamp(trip.timestamp)
        holder.binding.txtType.text = trip.type
    }

    override fun getItemCount() = trips.size
}
