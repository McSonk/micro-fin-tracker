package com.eromn.microfintracker

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eromn.microfintracker.data.AppDatabase
import com.eromn.microfintracker.data.TransactionRepository
import com.eromn.microfintracker.ui.theme.FinTrackTheme
import com.eromn.microfintracker.viewmodel.HistoryViewModel
import com.eromn.microfintracker.viewmodel.HistoryViewModelFactory

class MainActivity : AppCompatActivity(), AddTransactionBS.TransactionDetailsListener {
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(
            TransactionRepository(AppDatabase.getDatabase(applicationContext).transactionDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FinTrackTheme() {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainCanvas()
                }
            }
        }
    }// end onCreate

    override fun onTransactionDetailsEntered(description: String, amount: Double) {
        // This is where you receive the data from the BottomSheetDialogFragment
        historyViewModel.logTransaction(description, amount)
    }


    @Composable
    fun LogUBike(){
        Button(onClick = {
            Toast.makeText(this, "¡Viaje agregado!", Toast.LENGTH_SHORT).show()
            historyViewModel.logTransaction("uBike", 10.0)
        }) {
            Text("uBike")
        }
    }

    @Composable
    fun LogMetro(){
        Button(onClick = {
            Toast.makeText(this, "¡Viaje agregado!", Toast.LENGTH_SHORT).show()
            historyViewModel.logTransaction("MRT", 20.0)
        }) {
            Text("MRT")
        }
    }

    @Composable
    fun AddOther(){
        Button(onClick = {
            val bsFragment = AddTransactionBS.newInstance()
            // Set the listener to this Activity, because this Activity
            // implements TransactionDetailsListener
            bsFragment.setTransactionDetailsListener(this)
            // Show the BottomSheet
            bsFragment.show(supportFragmentManager, AddTransactionBS.TAG)
        }) {
            Text("Agregar otro gasto")
        }
    }

    @Composable
    fun ReadLogs(){
        Button(onClick = {
            startActivity(Intent(this, HistoryActivity::class.java))
        }) {
            Text("Leer logs")
        }
    }

    @Composable
    fun MainCanvas(){
        Column (
            modifier= Modifier
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(vertical = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            LogUBike()
            LogMetro()
            AddOther()
            Spacer(Modifier.height(40.dp))
            ReadLogs()
        }
    }

    @Preview(showBackground = true)
    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        showBackground = true,
        name = "Dark mode"
    )
    @Composable
    fun MainPreview(){
        FinTrackTheme() {
            Surface() {
                MainCanvas()
            }
        }
    }


}// end class MainActivity