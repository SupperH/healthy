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
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: (userId: Long, isDoctor: Boolean) -> Unit,
    viewModel: LoginRegisterViewModel = viewModel()
) {
    val registerState by viewModel.registerUiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            onRegisterSuccess(user.userId, user.role == "doctor")
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
            text = "Register",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = registerState.name,
            onValueChange = { viewModel.updateRegisterName(it) },
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = registerState.phone,
            onValueChange = { viewModel.updateRegisterPhone(it) },
            label = { Text("Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = registerState.password,
            onValueChange = { viewModel.updateRegisterPassword(it) },
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
                    selected = registerState.role == "elder",
                    onClick = { viewModel.updateRegisterRole("elder") }
                )
                Text("Elder")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = registerState.role == "doctor",
                    onClick = { viewModel.updateRegisterRole("doctor") }
                )
                Text("Doctor")
            }
        }

        if (registerState.errorMessage.isNotEmpty()) {
            Text(
                text = registerState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    val success = viewModel.register()
                    if (success) onNavigateBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Register")
        }

        TextButton(
            onClick = onNavigateBack
        ) {
            Text("Back to Login")
        }
    }
} 