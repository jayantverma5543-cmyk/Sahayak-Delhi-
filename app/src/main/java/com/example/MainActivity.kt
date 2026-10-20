package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.SahayakApp
import com.example.viewmodel.SahayakViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SahayakViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SahayakApp(viewModel = viewModel)
        }
    }

    override fun onStop() {
        super.onStop()
        // If app is put in background or closed, release mic and stop active sensors if not actively used
        viewModel.cleanUpResourcesOnAppClose()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanUpResourcesOnAppClose()
    }
}
