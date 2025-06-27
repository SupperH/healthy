package com.example.healthmanagecenter.ui.screen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import com.example.healthmanagecenter.viewmodel.HealthRecordViewModel
import com.example.healthmanagecenter.viewmodel.DoctorFeedbackViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.ViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderHealthDetailScreen(
    elderId: Long,
    doctorId: Long,
    onBack: () -> Unit,
    onNavigateToFeedback: (healthRecordId: Long, elderId: Long, doctorId: Long) -> Unit,
    healthRecordViewModel: HealthRecordViewModel = viewModel(factory = HealthRecordViewModelFactory(LocalContext.current.applicationContext as Application, elderId)),
    doctorFeedbackViewModel: DoctorFeedbackViewModel = viewModel()
) {
    val recentRecords by healthRecordViewModel.healthRecordDao.getRecentRecords(elderId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Records") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) { 
            items(recentRecords) { record ->
                HealthRecordItem(
                    record = record,
                    isAbnormal = doctorFeedbackViewModel.checkHealthRecordAbnormal(record).first,
                    abnormalType = doctorFeedbackViewModel.checkHealthRecordAbnormal(record).second,
                    onCommentClick = { healthRecordId, eldId, docId ->
                        onNavigateToFeedback(healthRecordId, eldId, docId)
                    },
                    doctorId = doctorId,
                    elderId = elderId
                )
            }
        }
    }
}

@Composable
fun HealthRecordItem(
    record: HealthRecordEntity,
    isAbnormal: Boolean,
    abnormalType: String?,
    onCommentClick: (healthRecordId: Long, elderId: Long, doctorId: Long) -> Unit,
    doctorId: Long,
    elderId: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isAbnormal) Color(0xFFFFEBEE) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date(record.timestamp)),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            record.weight?.let { Text("Weight: $it kg") }
            record.height?.let { Text("Height: $it cm") }
            if (record.weight != null && record.height != null && record.height > 0) {
                val h = record.height / 100.0f
                val bmi = String.format("%.2f", record.weight / (h * h))
                Text("BMI: $bmi")
            }
            record.heartRate?.let { Text("Heart Rate: $it bpm") }
            record.bloodPressureHigh?.let { high ->
                record.bloodPressureLow?.let { low ->
                    Text("Blood Pressure: $high/$low mmHg")
                }
            }
            record.sleepHours?.let { Text("Sleep: $it hours") }
            
            if (isAbnormal) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Abnormal: $abnormalType",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onCommentClick(record.recordId, elderId, doctorId) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Comment, contentDescription = "Comment")
                Spacer(Modifier.width(4.dp))
                Text("Comment")
            }
        }
    }
}

// Factory for HealthRecordViewModel
class HealthRecordViewModelFactory(private val application: Application, private val userId: Long) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthRecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HealthRecordViewModel(application, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}