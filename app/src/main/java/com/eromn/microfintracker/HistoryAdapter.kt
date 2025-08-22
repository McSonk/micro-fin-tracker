package com.eromn.microfintracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eromn.microfintracker.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

data class Trip(val timestamp: Long, val type: String)

class HistoryAdapter(private val trips: List<Trip>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val trip = trips[position]
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.binding.txtDate.text = sdf.format(Date(trip.timestamp))
        holder.binding.txtType.text = trip.type
    }

    override fun getItemCount() = trips.size
}
