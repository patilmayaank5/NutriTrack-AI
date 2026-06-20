package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.NutriTrackApp
import com.example.ui.NutriViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core view model init
        val viewModel = ViewModelProvider(this)[NutriViewModel::class.java]

        setContent {
            NutriTrackApp(viewModel)
        }
    }
}
