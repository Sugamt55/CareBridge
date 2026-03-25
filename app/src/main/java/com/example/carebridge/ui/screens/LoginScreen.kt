package com.example.carebridge.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.carebridge.R
import com.example.carebridge.ui.viewmodel.LoginResult
import com.example.carebridge.ui.viewmodel.LoginViewModel

/**
 * LoginScreen (The View in MVVM).
 * Connected to Firebase Authentication.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val loginState by viewModel.loginState.observeAsState(initial = LoginResult.Idle)
    val context = LocalContext.current

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginResult.Success -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is LoginResult.Error -> {
                Toast.makeText(context, (loginState as LoginResult.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.carebridge_logo),
            contentDescription = "CareBridge Logo",
            modifier = Modifier.size(100.dp)
        )
        Text("Welcome Back", style = MaterialTheme.typography.headlineMedium)
        Text("Log in to access your health dashboard", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            enabled = loginState !is LoginResult.Loading
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = loginState !is LoginResult.Loading
        )

        TextButton(
            onClick = { navController.navigate("reset") },
            modifier = Modifier.align(Alignment.End),
            enabled = loginState !is LoginResult.Loading
        ) {
            Text("Forgot Password?")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loginState is LoginResult.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Login")
            }
        }

        TextButton(
            onClick = { navController.navigate("signup") },
            enabled = loginState !is LoginResult.Loading
        ) {
            Text("New user? Create an account")
        }
    }
}
