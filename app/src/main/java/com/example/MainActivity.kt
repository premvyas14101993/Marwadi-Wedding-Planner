package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.cloud.FirebaseAuthManager
import com.example.data.cloud.FirebaseInitializer
import com.example.data.cloud.WeddingFirestoreSyncManager
import com.example.data.local.AppDatabase
import com.example.data.repository.WeddingRepository
import com.example.ui.navigation.WeddingMainApp
import com.example.ui.theme.MarwadiWeddingTheme
import com.example.ui.viewmodel.WeddingViewModel
import com.example.ui.viewmodel.WeddingViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: WeddingViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = WeddingRepository(database)
        val authManager = FirebaseAuthManager(applicationContext)
        val syncManager = WeddingFirestoreSyncManager(applicationContext, database, lifecycleScope)
        WeddingViewModelFactory(repository, authManager, syncManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseInitializer.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            MarwadiWeddingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeddingMainApp(viewModel = viewModel)
                }
            }
        }
    }
}
