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
    onElderClick: (Long) -> Unit
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
                        ElderBriefCard(elder, onElderClick)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderBriefCard(elder: ElderBrief, onElderClick: (Long) -> Unit) {
    Card(
        onClick = { onElderClick(elder.userId) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Group, contentDescription = "Elder", tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(elder.name, style = MaterialTheme.typography.titleMedium)
                Text("Last Record: ${elder.lastRecordTime}", style = MaterialTheme.typography.bodySmall)
                Text("Status: ${elder.healthStatus}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ElderBrief 数据类
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
                text = "联系电话：${elder.phone}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
} 