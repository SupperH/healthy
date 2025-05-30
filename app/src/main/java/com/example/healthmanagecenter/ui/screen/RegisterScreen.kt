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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmanagecenter.viewmodel.LoginRegisterViewModel
import kotlinx.coroutines.launch
import com.example.healthmanagecenter.R

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
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = registerState.name,
            onValueChange = { viewModel.updateRegisterName(it) },
            label = { Text(stringResource(R.string.name_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = registerState.phone,
            onValueChange = { viewModel.updateRegisterPhone(it) },
            label = { Text(stringResource(R.string.phone_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = registerState.password,
            onValueChange = { viewModel.updateRegisterPassword(it) },
            label = { Text(stringResource(R.string.password_label)) },
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
                Text(stringResource(R.string.login_user_role_elder))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = registerState.role == "doctor",
                    onClick = { viewModel.updateRegisterRole("doctor") }
                )
                Text(stringResource(R.string.login_user_role_doctor))
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
            Text(stringResource(R.string.register_title))
        }

        TextButton(
            onClick = onNavigateBack
        ) {
            Text(stringResource(R.string.back_to_login_button_text))
        }
    }
} 