package com.example.carebridge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.carebridge.ui.screens.*

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable(route = "home") {
            HomeScreen(navController = navController)
        }
        composable(route = "scan") {
            ScanScreen(navController = navController)
        }
        composable(route = "insights") {
            InsightsScreen(navController = navController)
        }
    }
}
