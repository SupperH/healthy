package com.example.healthmanagecenter.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmanagecenter.data.entity.AlertEntity
import com.example.healthmanagecenter.data.entity.UserEntity
import com.example.healthmanagecenter.viewmodel.DoctorHomeViewModel
import com.example.healthmanagecenter.viewmodel.LoginRegisterViewModel
import com.example.healthmanagecenter.viewmodel.HealthRecordViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.healthmanagecenter.ui.screen.AppDrawer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorHomeScreen(
    userId: Long,
    userName: String,
    boundElders: List<ElderBrief>,
    alertsCount: Int,
    onLogout: () -> Unit,
    onElderClick: (Long) -> Unit,
    onHealthDetail: (Long) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                userName = userName,
                userRole = "Doctor",
                userId = userId,
                onLogout = onLogout,
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Doctor Dashboard") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            containerColor = Color(0xFFF9F9F9)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Alerts Summary
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Alerts", tint = Color(0xFFFF9800), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(16.dp))
                        Text("Unhandled Alerts: $alertsCount", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Bound Elders List
                Text("Bound Elders", style = MaterialTheme.typography.titleLarge)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(boundElders) { elder ->
                        ElderBriefCard(elder, onElderClick, onHealthDetail)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderBriefCard(elder: ElderBrief, onElderClick: (Long) -> Unit, onHealthDetail: (Long) -> Unit) {
    Card(
        onClick = { onElderClick(elder.userId) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = "Elder", tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(elder.name, style = MaterialTheme.typography.titleMedium)
                    Text("Last Record: ${elder.lastRecordTime}", style = MaterialTheme.typography.bodySmall)
                    Text("Status: ${elder.healthStatus}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onHealthDetail(elder.userId) }, modifier = Modifier.align(Alignment.End)) {
                Text("View Health Data")
            }
        }
    }
}

// ElderBrief data class
data class ElderBrief(
    val userId: Long,
    val name: String,
    val lastRecordTime: String,
    val healthStatus: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertCard(alert: AlertEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date(alert.time)),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ElderCard(
    elder: UserEntity,
    onElderClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onElderClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = elder.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Phone: ${elder.phone}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderHealthDetailScreen(
    elderId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val viewModel: HealthRecordViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return HealthRecordViewModel(context as android.app.Application, elderId) as T
        }
    })
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Elder Health Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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
            if (uiState.weight.isBlank() && uiState.height.isBlank() && uiState.heartRate.isBlank() && uiState.bloodPressureHigh.isBlank() && uiState.bloodPressureLow.isBlank() && uiState.sleepHours.isBlank()) {
                Text("No health data available.", style = MaterialTheme.typography.bodyLarge)
            } else {
                Text("Weight: ${uiState.weight}", style = MaterialTheme.typography.bodyLarge)
                Text("Height: ${uiState.height}", style = MaterialTheme.typography.bodyLarge)
                Text("Heart Rate: ${uiState.heartRate}", style = MaterialTheme.typography.bodyLarge)
                Text("Blood Pressure: ${uiState.bloodPressureHigh}/${uiState.bloodPressureLow}", style = MaterialTheme.typography.bodyLarge)
                Text("Sleep Hours: ${uiState.sleepHours}", style = MaterialTheme.typography.bodyLarge)
                Text("Score: ${uiState.score}", style = MaterialTheme.typography.bodyLarge)
                if (uiState.abnormalTips.isNotEmpty()) {
                    Text("Abnormal: ${uiState.abnormalTips}", color = Color(0xFFE53935), style = MaterialTheme.typography.bodyMedium)
                }
                if (uiState.doctorComment.isNotEmpty()) {
                    Divider(Modifier.padding(vertical = 8.dp))
                    Text("Doctor's Comment:", style = MaterialTheme.typography.titleSmall)
                    Text(uiState.doctorComment, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
} 