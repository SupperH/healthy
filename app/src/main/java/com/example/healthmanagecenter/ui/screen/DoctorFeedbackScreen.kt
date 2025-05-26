package com.example.healthmanagecenter.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.healthmanagecenter.viewmodel.DoctorFeedbackViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmanagecenter.data.entity.DoctorFeedbackEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorFeedbackScreen(
    doctorId: Long,
    elderId: Long,
    onMenu: () -> Unit,
    onNotification: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val viewModel: DoctorFeedbackViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return DoctorFeedbackViewModel(context as android.app.Application, doctorId) as T
        }
    })
    val feedbacks by viewModel.feedbackList.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val scope = rememberCoroutineScope()
    val filteredFeedbacks = feedbacks.filter { it.elderId == elderId && it.comment.isNotBlank() && !it.comment.startsWith("System detected") }
    var dialogContent by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feedbacks") },
                navigationIcon = {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    NotificationBell(
                        unreadCount = unreadCount,
                        onClick = onNotification
                    )
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
            items(filteredFeedbacks) { feedback ->
                FeedbackCard(
                    feedback = feedback,
                    onClick = { dialogContent = feedback.comment }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
        if (dialogContent != null) {
            AlertDialog(
                onDismissRequest = { dialogContent = null },
                title = { Text("Doctor's Feedback") },
                text = { Text(dialogContent!!) },
                confirmButton = {
                    Button(onClick = { dialogContent = null }) { Text("OK") }
                }
            )
        }
    }
}

@Composable
fun FeedbackCard(
    feedback: DoctorFeedbackEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Feedback, contentDescription = null, tint = Color(0xFF4A90E2))
                Spacer(Modifier.width(8.dp))
                Text("Elder ID: ${feedback.elderId}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(feedback.timestamp)), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Text("Doctor's Comment:", style = MaterialTheme.typography.labelMedium)
            Text(
                feedback.comment,
                color = Color.Unspecified,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun FeedbackInputArea(
    comment: String,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    height: String
) {
    Column {
        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChange,
            label = { Text("Add Comment") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Button(
            onClick = onSubmit,
            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
        ) {
            Text("Submit")
        }
        if (height.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Normal Ranges: Weight: ±15% of standard, Heart Rate: 60-100 bpm, Blood Pressure: 90-140/60-90 mmHg, Sleep: 6-10h",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
} 