package com.example.healthmanagecenter.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmanagecenter.viewmodel.ElderHomeViewModel
import com.example.healthmanagecenter.viewmodel.LoginRegisterViewModel
import kotlinx.coroutines.launch
import com.example.healthmanagecenter.ui.screen.AppDrawer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import com.example.healthmanagecenter.viewmodel.NotificationViewModel
import com.example.healthmanagecenter.ui.components.NotificationBell
import com.example.healthmanagecenter.viewmodel.HealthRecordViewModel
import com.example.healthmanagecenter.ui.components.HealthMetricsOverviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderHomeScreen(
    userId: Long,
    userName: String,
    doctorName: String,
    doctorPhone: String,
    onLogout: () -> Unit,
    onHealthRecords: () -> Unit,
    onMedication: () -> Unit,
    onAlerts: () -> Unit,
    onNotification: () -> Unit,
    onNavigateToElderFeedback: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext
    val notificationViewModel: NotificationViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel(context as Application, userId) as T
        }
    })
    val notificationUiState by notificationViewModel.uiState.collectAsState()

    val healthRecordViewModel: HealthRecordViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HealthRecordViewModel(context as Application, userId) as T
        }
    })
    val latestHealthRecord by healthRecordViewModel.latestHealthRecord.collectAsState(initial = null)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                userName = userName,
                userRole = "Elder",
                userId = userId,
                onLogout = onLogout,
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Elder Dashboard") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        NotificationBell(
                            unreadCount = notificationUiState.unreadFeedbackCount,
                            onClick = onNotification
                        )
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
                // Doctor Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Doctor", tint = Color(0xFF2196F3), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Doctor: Dr. $doctorName", style = MaterialTheme.typography.titleMedium)
                            Text("Phone: $doctorPhone", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Function card area
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FeatureCard("Health Records", Icons.Default.MonitorHeart, onHealthRecords, Color(0xFF4CAF50))
                    FeatureCard("Medication", Icons.Default.Medication, onMedication, Color(0xFF2196F3))
                    FeatureCard("Doctor Feedback", Icons.Default.Feedback, onNavigateToElderFeedback, Color(0xFF1976D2))
                }
                Spacer(Modifier.height(16.dp))
                // Health Metrics Chart
                HealthMetricsOverviewCard(latestRecord = latestHealthRecord)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, color: Color) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        modifier = Modifier
            .padding(8.dp)
            .size(120.dp, 140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = color)
        }
    }
}

