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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "登录",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = loginState.name,
            onValueChange = { viewModel.updateLoginName(it) },
            label = { Text("用户名") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = loginState.password,
            onValueChange = { viewModel.updateLoginPassword(it) },
            label = { Text("密码") },
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
                Text("老人")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = loginState.role == "doctor",
                    onClick = { viewModel.updateLoginRole("doctor") }
                )
                Text("医生")
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
            Text("登录")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onNavigateToRegister
            ) {
                Text("没有账号？点击注册")
            }
            TextButton(
                onClick = { showResetPasswordDialog = true }
            ) {
                Text("忘记密码？")
            }
        }
    }

    if (showResetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            title = { Text("重置密码") },
            text = {
                Column {
                    OutlinedTextField(
                        value = resetPasswordState.name,
                        onValueChange = { viewModel.updateResetPasswordName(it) },
                        label = { Text("用户名") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = resetPasswordState.email,
                        onValueChange = { viewModel.updateResetPasswordEmail(it) },
                        label = { Text("邮箱") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = resetPasswordState.newPassword,
                        onValueChange = { viewModel.updateResetPasswordNewPassword(it) },
                        label = { Text("新密码") },
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
                    Text("重置")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetPasswordDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
} 