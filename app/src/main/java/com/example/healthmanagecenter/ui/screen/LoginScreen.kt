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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import com.example.healthmanagecenter.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (userId: Long, isDoctor: Boolean, needPickDoctor: Boolean) -> Unit,
    viewModel: LoginRegisterViewModel = viewModel()
) {
    val loginState by viewModel.loginUiState.collectAsState()
    val resetPasswordState by viewModel.resetPasswordUiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    var showResetPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            onLoginSuccess(user.userId, user.role == "doctor", false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F0))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp).padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = loginState.name,
            onValueChange = { viewModel.updateLoginName(it) },
            label = { Text("Username", color = Color.Gray) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF57C00), // Orange
                unfocusedBorderColor = Color(0xFFFFB74D), // Light Orange
                cursorColor = Color(0xFFF57C00) // Orange
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = loginState.password,
            onValueChange = { viewModel.updateLoginPassword(it) },
            label = { Text("Password", color = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(8.dp),
             colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF57C00), // Orange
                unfocusedBorderColor = Color(0xFFFFB74D), // Light Orange
                cursorColor = Color(0xFFF57C00) // Orange
            ),
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
                    onClick = { viewModel.updateLoginRole("elder") },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF57C00))
                )
                Text("Elder")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = loginState.role == "doctor",
                    onClick = { viewModel.updateLoginRole("doctor") },
                     colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF57C00))
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
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D))
        ) {
            Text("Login", color = Color.White)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onNavigateToRegister
            ) {
                Text("No account? Click to register", color = Color(0xFFF57C00))
            }
            TextButton(
                onClick = { showResetPasswordDialog = true }
            ) {
                Text("Forgot password?", color = Color(0xFFF57C00))
            }
        }
    }

    if (showResetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = resetPasswordState.name,
                        onValueChange = { viewModel.updateResetPasswordName(it) },
                        label = { Text("Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = resetPasswordState.email,
                        onValueChange = { viewModel.updateResetPasswordEmail(it) },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = resetPasswordState.newPassword,
                        onValueChange = { viewModel.updateResetPasswordNewPassword(it) },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    if (resetPasswordState.errorMessage.isNotEmpty()) {
                        Text(
                            text = resetPasswordState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = viewModel.resetPassword()
                            if (success) {
                                showResetPasswordDialog = false
                            }
                        }
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetPasswordDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
} 