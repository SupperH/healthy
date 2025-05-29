package com.example.healthmanagecenter.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmanagecenter.data.entity.DoctorFeedbackEntity
import com.example.healthmanagecenter.viewmodel.DoctorFeedbackViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.healthmanagecenter.ui.components.FeedbackCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderFeedbackScreen(
    elderId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DoctorFeedbackViewModel = viewModel()
) {
    val feedbacks by viewModel.getFeedbacksByElderId(elderId).collectAsState(initial = emptyList())
    var selectedFeedback by remember { mutableStateOf<DoctorFeedbackEntity?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor's Feedback") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(feedbacks) { feedback ->
                FeedbackCard(
                    feedback = feedback,
                    onClick = {
                        selectedFeedback = feedback
                        scope.launch {
                            viewModel.markFeedbackAsRead(feedback.id)
                        }
                    }
                )
            }
        }
    }

    selectedFeedback?.let { feedback ->
        AlertDialog(
            onDismissRequest = { selectedFeedback = null },
            title = { Text("Doctor's Feedback") },
            text = {
                Column {
                    // Health Record Context
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Health Record",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                    .format(Date(feedback.timestamp))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (feedback.isAbnormal) {
                                Text(
                                    text = "Abnormal Type: ${feedback.abnormalType}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Doctor's Comment
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Doctor's Comment",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = feedback.comment,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedFeedback = null }) {
                    Text("Close")
                }
            }
        )
    }
} 