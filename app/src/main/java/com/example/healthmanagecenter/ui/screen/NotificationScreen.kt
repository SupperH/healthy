package com.example.healthmanagecenter.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.healthmanagecenter.viewmodel.NotificationViewModel
import com.example.healthmanagecenter.viewmodel.NotificationUiState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    userId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val viewModel: NotificationViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel(context as android.app.Application, userId) as T
        }
    })
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Notifications, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(uiState.notifications) { item ->
                NotificationCard(
                    item = item,
                    onClick = { viewModel.markAsRead(item) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    item: NotificationUiState.NotificationItem,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        onClick = onClick
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (item.type == NotificationUiState.Type.Feedback) Icons.Default.Feedback else Icons.Default.Notifications,
                contentDescription = null,
                tint = if (item.type == NotificationUiState.Type.Feedback) Color(0xFF4A90E2) else Color(0xFF43A047)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(item.content, style = MaterialTheme.typography.bodyMedium)
                Text(item.timeStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (!item.isRead) {
                Box(
                    Modifier
                        .size(16.dp)
                        .background(Color(0xFFE53935), shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text("", color = Color.White)
                }
            }
        }
    }
} 