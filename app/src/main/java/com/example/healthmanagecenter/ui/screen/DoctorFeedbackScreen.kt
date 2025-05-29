package com.example.healthmanagecenter.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
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
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.healthmanagecenter.ui.components.FeedbackCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorFeedbackScreen(
    elderId: Long,
    healthRecordId: Long,
    doctorId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DoctorFeedbackViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Record Feedback") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Health Record Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Health Record Details",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // Health data will be displayed here
                }
            }

            // Comment Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Add Your Feedback",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Write your feedback...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { onNavigateBack() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    val success = viewModel.addFeedback(
                                        elderId = elderId,
                                        doctorId = doctorId,
                                        healthRecordId = healthRecordId,
                                        comment = comment
                                    )
                                    if (success) {
                                        comment = ""
                                        showSuccessMessage = true
                                        onNavigateBack()
                                    } else {
                                        showErrorMessage = true
                                    }
                                }
                            },
                            enabled = comment.isNotBlank()
                        ) {
                            Icon(Icons.Default.Comment, contentDescription = "Submit")
                            Spacer(Modifier.width(4.dp))
                            Text("Submit Feedback")
                        }
                    }
                }
            }
        }

        if (showSuccessMessage) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showSuccessMessage = false }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text("Feedback submitted successfully")
            }
        }

        if (showErrorMessage) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showErrorMessage = false }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text("Failed to submit feedback. Please try again.")
            }
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