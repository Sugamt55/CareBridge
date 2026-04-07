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
        startDestination = "home" // Temporarily set to home for testing. Change back to "login" later.
    ) {
        composable(route = "login") {
            LoginScreen(navController = navController)
        }
        composable(route = "signup") {
            SignUpScreen(navController = navController)
        }
        composable(route = "reset") {
            ResetPasswordScreen(navController = navController)
        }
        composable(route = "home") {
            HomeScreen(navController = navController)
        }
        composable(route = "scan") {
            ScanScreen(navController = navController)
        }
        composable(route = "chat") {
            ChatScreen(navController = navController)
        }
    }
}