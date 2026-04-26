package com.example.nutriscanai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.nutriscanai.ui.navigation.SetupNavGraph
import com.example.nutriscanai.ui.theme.NutriScanAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 1. Apply your app's custom medical theme
            NutriScanAITheme {

                // 2. A surface container to provide a clean background for the telemedicine app
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 3. Create the NavController (The driver)
                    val navController = rememberNavController()
                    SetupNavGraph(navController = navController)

                    // 4. Call your Navigation Graph (The map)
                    // This will automatically start the app at your "login" route

                }
            }
        }
    }
}