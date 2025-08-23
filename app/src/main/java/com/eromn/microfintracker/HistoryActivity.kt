package com.eromn.microfintracker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.eromn.microfintracker.databinding.ActivityHistoryBinding
import java.io.FileNotFoundException

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val trips = readTransportLog()
        binding.recyclerHistory.adapter = HistoryAdapter(trips)
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
    }

    private fun readTransportLog(): List<Trip> {
        return try {
            openFileInput("transport_log.csv")
                .bufferedReader()
                .readLines()
                .mapNotNull { line ->
                    val parts = line.split(",")
                    if (parts.size == 2) {
                        val time = parts[0].toLongOrNull()
                        val type = parts[1]
                        if (time != null) Trip(time, type) else null
                    } else null
                }
        } catch (e: FileNotFoundException) {
            emptyList()
        }
    }
}
