package com.eromn.microfintracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eromn.microfintracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // Holds references to all views in the activity_main.xml layout for easy access.
    private lateinit var binding: ActivityMainBinding
    private lateinit var txDataSource: TxDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        txDataSource = TxDataSource(this)

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
            txDataSource.logTransaction("train")
        }

        binding.btnBus.setOnClickListener {
            Toast.makeText(this, "Bus logged!", Toast.LENGTH_SHORT).show()
            txDataSource.logTransaction("bus")
        }

        binding.btnLog.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

}