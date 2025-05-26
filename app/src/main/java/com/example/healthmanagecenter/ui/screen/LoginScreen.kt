package com.example.healthmanagecenter.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmanagecenter.viewmodel.LoginRegisterViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (userId: Long, isDoctor: Boolean, needPickDoctor: Boolean) -> Unit,
    viewModel: LoginRegisterViewModel = viewModel()
) {
    val loginState by viewModel.loginUiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            onLoginSuccess(user.userId, user.role == "doctor", false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = loginState.name,
            onValueChange = { viewModel.updateLoginName(it) },
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = loginState.password,
            onValueChange = { viewModel.updateLoginPassword(it) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                RadioButton(
                    selected = loginState.role == "elder",
                    onClick = { viewModel.updateLoginRole("elder") }
                )
                Text("Elder")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = loginState.role == "doctor",
                    onClick = { viewModel.updateLoginRole("doctor") }
                )
                Text("Doctor")
            }
        }

        if (loginState.errorMessage.isNotEmpty()) {
            Text(
                text = loginState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    val (userId, isDoctor, needPickDoctor) = viewModel.login()
                    if (userId != null) {
                        onLoginSuccess(userId, isDoctor, needPickDoctor)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Login")
        }

        TextButton(
            onClick = onNavigateToRegister
        ) {
            Text("No account? Register here")
        }
    }
} 