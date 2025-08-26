package com.eromn.microfintracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eromn.microfintracker.data.AppDatabase
import com.eromn.microfintracker.data.TransactionRepository
import com.eromn.microfintracker.databinding.ActivityMainBinding
import com.eromn.microfintracker.viewmodel.HistoryViewModel
import com.eromn.microfintracker.viewmodel.HistoryViewModelFactory

class MainActivity : AppCompatActivity(), AddTransactionBS.TransactionDetailsListener {
    // Holds references to all views in the activity_main.xml layout for easy access.
    private lateinit var binding: ActivityMainBinding
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(
            TransactionRepository(AppDatabase.getDatabase(applicationContext).transactionDao())
        )
    }

    // TODO: Add "other expenses" layout / functionality

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
            historyViewModel.logTransaction("Metro", 5.0)
        }

        binding.btnBus.setOnClickListener {
            Toast.makeText(this, "Bus logged!", Toast.LENGTH_SHORT).show()
            historyViewModel.logTransaction("Metrobús", 6.0)
        }

        binding.btnOther.setOnClickListener {
            val bsFragment = AddTransactionBS.newInstance()
            // Set the listener to this Activity, because this Activity
            // implements TransactionDetailsListener
            bsFragment.setTransactionDetailsListener(this)
            // Show the BottomSheet
            bsFragment.show(supportFragmentManager, AddTransactionBS.TAG)
        }

        binding.btnLog.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }// end onCreate

    override fun onTransactionDetailsEntered(description: String, amount: Double) {
        // This is where you receive the data from the BottomSheetDialogFragment
        historyViewModel.logTransaction(description, amount)
    }
}// end class MainActivity