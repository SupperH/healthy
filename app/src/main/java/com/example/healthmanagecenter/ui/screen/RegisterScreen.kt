package com.example.healthmanagecenter.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: (userId: Long, isDoctor: Boolean) -> Unit,
    viewModel: LoginRegisterViewModel = viewModel()
) {
    val registerState by viewModel.registerUiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            onRegisterSuccess(user.userId, user.role == "doctor")
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
            value = registerState.name,
            onValueChange = { viewModel.updateRegisterName(it) },
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
            value = registerState.phone,
            onValueChange = { viewModel.updateRegisterPhone(it) },
            label = { Text("Phone Number", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
            value = registerState.email,
            onValueChange = { viewModel.updateRegisterEmail(it) },
            label = { Text("Email", color = Color.Gray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
            value = if (registerState.dateOfBirth > 0) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(registerState.dateOfBirth))
            } else "",
            onValueChange = { },
            label = { Text("Date of Birth", color = Color.Gray) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            },
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
            value = registerState.password,
            onValueChange = { viewModel.updateRegisterPassword(it) },
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

        // Gender selection
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
                    selected = registerState.gender == "male",
                    onClick = { viewModel.updateRegisterGender("male") },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF57C00))
                )
                Text("Male")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = registerState.gender == "female",
                    onClick = { viewModel.updateRegisterGender("female") },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF57C00))
                )
                Text("Female")
            }
        }

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
                    onClick = { viewModel.updateRegisterRole("elder") },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF57C00))
                )
                Text("Elder")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = registerState.role == "doctor",
                    onClick = { viewModel.updateRegisterRole("doctor") },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFF57C00))
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
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D))
        ) {
            Text("Register", color = Color.White)
        }

        TextButton(
            onClick = onNavigateBack
        ) {
            Text("Already have an account? Back to login", color = Color(0xFFF57C00))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = registerState.dateOfBirth.takeIf { it > 0 }
                ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.updateRegisterDateOfBirth(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Select Date of Birth") },
                headline = { Text("Select Date of Birth") },
                showModeToggle = false
            )
        }
    }
} 