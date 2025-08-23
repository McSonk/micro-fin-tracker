package com.eromn.microfintracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eromn.microfintracker.databinding.ActivityMainBinding
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {
    // Holds references to all views in the activity_main.xml layout for easy access.
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate using View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Sets the main content view of the Activity's window.
        setContentView(binding.root)

        // Listen for window insets changes on the main layout.
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            // Get the insets dimensions for system bars (status and navigation).
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply padding to the view to account for system bar sizes.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // Return the insets, allowing other views to also process them.
            insets
        }

        binding.btnTrain.setOnClickListener{
            Toast.makeText(this, "Train logged!", Toast.LENGTH_SHORT).show()
            logTransportEvent("train")
        }

        binding.btnBus.setOnClickListener {
            Toast.makeText(this, "Bus logged!", Toast.LENGTH_SHORT).show()
            logTransportEvent("bus")
        }

        binding.btnLog.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun logTransportEvent(type: String) {
        val timestamp = System.currentTimeMillis()
        val line = "$timestamp,$type\n"
        openFileOutput("transport_log.csv", MODE_APPEND).use { output ->
            output.write(line.toByteArray())
        }
    }


    private fun readTransportLog(): List<Pair<Long, String>> {
        return try {
            openFileInput("transport_log.csv")
                .bufferedReader()
                .readLines()
                .mapNotNull { line ->
                    val parts = line.split(",")
                    if (parts.size == 2) {
                        val time = parts[0].toLongOrNull()
                        val type = parts[1]
                        if (time != null) time to type else null
                    } else null
                }
        } catch (e: FileNotFoundException) {
            emptyList()
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

}