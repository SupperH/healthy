package com.example.healthmanagecenter.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmanagecenter.viewmodel.HealthRecordViewModel
import com.example.healthmanagecenter.viewmodel.HealthRecordUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthRecordScreen(
    userId: Long,
    onBack: () -> Unit,
    onNotification: () -> Unit,
    onNavigateToHealthTrend: (Long) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val viewModel: HealthRecordViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return HealthRecordViewModel(context as android.app.Application, userId) as T
        }
    })
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Record") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    NotificationBell(
                        unreadCount = uiState.unreadCount,
                        onClick = onNotification
                    )
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            HealthInputCard(uiState, viewModel)
            Spacer(Modifier.height(16.dp))
            HealthAnalysisCard(uiState)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onNavigateToHealthTrend(userId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Health Trend Chart")
            }
        }
    }
}

@Composable
fun HealthInputCard(uiState: HealthRecordUiState, viewModel: HealthRecordViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Input Today's Data", style = MaterialTheme.typography.titleMedium)
            Text("Date: ${uiState.dateStr}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.weight,
                onValueChange = { viewModel.updateWeight(it) },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.height,
                onValueChange = { viewModel.updateHeight(it) },
                label = { Text("Height (cm)") },
                enabled = uiState.isFirstInput,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.heartRate,
                onValueChange = { viewModel.updateHeartRate(it) },
                label = { Text("Heart Rate (bpm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row {
                OutlinedTextField(
                    value = uiState.bloodPressureHigh,
                    onValueChange = { viewModel.updateBloodPressureHigh(it) },
                    label = { Text("Systolic") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = uiState.bloodPressureLow,
                    onValueChange = { viewModel.updateBloodPressureLow(it) },
                    label = { Text("Diastolic") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = uiState.sleepHours,
                onValueChange = { viewModel.updateSleepHours(it) },
                label = { Text("Sleep Duration (hours)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.saveOrUpdateRecord() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
                Spacer(Modifier.width(8.dp))
                Text("Save")
            }
        }
    }
}

@Composable
fun HealthAnalysisCard(uiState: HealthRecordUiState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BarChart, contentDescription = "Analysis", tint = Color(0xFF4A90E2))
                Spacer(Modifier.width(8.dp))
                Text("BMI: ${uiState.bmi}", style = MaterialTheme.typography.titleMedium)
            }
            if (uiState.abnormalTips.isNotEmpty()) {
                Text(
                    "Abnormal: ${uiState.abnormalTips}",
                    color = Color(0xFFE53935),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (uiState.doctorComment.isNotEmpty()) {
                Divider(Modifier.padding(vertical = 8.dp))
                Text("Doctor's Comment:", style = MaterialTheme.typography.titleSmall)
                Text(uiState.doctorComment, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
