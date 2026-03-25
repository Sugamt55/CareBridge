package com.example.carebridge.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.carebridge.ui.viewmodel.ResetResult
import com.example.carebridge.ui.viewmodel.ResetPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    navController: NavController,
    viewModel: ResetPasswordViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }

    val resetState by viewModel.resetState.observeAsState(initial = ResetResult.Idle)
    val context = LocalContext.current

    LaunchedEffect(resetState) {
        when (resetState) {
            is ResetResult.Success -> {
                Toast.makeText(context, "Reset email sent successfully!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is ResetResult.Error -> {
                Toast.makeText(context, (resetState as ResetResult.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text("Enter your email address to receive a password reset link.", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                enabled = resetState !is ResetResult.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (resetState is ResetResult.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.sendResetEmail(email) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send Reset Link")
                }
            }
        }
    }
}
