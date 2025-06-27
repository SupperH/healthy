package com.example.healthmanagecenter.ui.screen

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.dao.MedicationReminderDao
import com.example.healthmanagecenter.data.entity.MedicationReminderEntity
import com.example.healthmanagecenter.viewmodel.MedicationReminderViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationReminderScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val activity = LocalContext.current as ComponentActivity
    val viewModel: MedicationReminderViewModel = viewModel(factory = MedicationReminderViewModelFactory(context as Application))
    val reminders by viewModel.reminders.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<MedicationReminderEntity?>(null) }
    var showNotificationPermissionDeniedDialog by remember { mutableStateOf(false) }

    // Request POST_NOTIFICATIONS permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (!isGranted) {
                    showNotificationPermissionDeniedDialog = true
                }
            }
        )

        LaunchedEffect(Unit) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explain to user why permission is needed
                    showNotificationPermissionDeniedDialog = true
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    // Request SCHEDULE_EXACT_ALARM permission for Android 12+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
        val alarmPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { result ->
                // Handle result if needed
            }
        )

        LaunchedEffect(Unit) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                alarmPermissionLauncher.launch(intent)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medication Reminder") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Reminder")
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
            items(reminders) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onEdit = { editingReminder = reminder },
                    onDelete = { viewModel.deleteReminder(reminder) }
                )
            }
        }
    }

    if (showAddDialog) {
        ReminderDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, instructions, timeList ->
                Log.d("MedicationReminderScreen", "Attempting to add reminder: $name, $instructions, $timeList")
                viewModel.addReminder(name, instructions, timeList)
                showAddDialog = false
            }
        )
    }

    editingReminder?.let { reminder ->
        ReminderDialog(
            reminder = reminder,
            onDismiss = { editingReminder = null },
            onSave = { name, instructions, timeList ->
                Log.d("MedicationReminderScreen", "Attempting to update reminder: ${reminder.id}, $name, $instructions, $timeList")
                viewModel.updateReminder(reminder.copy(
                    name = name,
                    instructions = instructions,
                    timeList = timeList
                ))
                editingReminder = null
            }
        )
    }

    if (showNotificationPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationPermissionDeniedDialog = false },
            title = { Text("Notification Permission Denied") },
            text = { Text("Medication reminders require notification permission to display in the system taskbar. Please enable the permission in settings.") },
            confirmButton = {
                Button(onClick = { 
                    showNotificationPermissionDeniedDialog = false
                    // Navigate to app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    activity.startActivity(intent)
                }) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationPermissionDeniedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderCard(
    reminder: MedicationReminderEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reminder.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = reminder.instructions,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                reminder.timeList.forEach { time ->
                    AssistChip(
                        onClick = { },
                        label = { Text(time) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderDialog(
    reminder: MedicationReminderEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, instructions: String, timeList: List<String>) -> Unit
) {
    var name by remember { mutableStateOf(reminder?.name ?: "") }
    var instructions by remember { mutableStateOf(reminder?.instructions ?: "") }
    var timeList by remember { mutableStateOf(reminder?.timeList ?: emptyList()) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (reminder == null) "Add Medication Reminder" else "Edit Medication Reminder") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medicine Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Dosage Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeList.forEach { time ->
                        AssistChip(
                            onClick = { timeList = timeList - time },
                            label = { Text(time) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
                
                Button(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Time")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && instructions.isNotBlank() && timeList.isNotEmpty()) {
                        onSave(name, instructions, timeList)
                    }
                },
                enabled = name.isNotBlank() && instructions.isNotBlank() && timeList.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                if (time !in timeList) {
                    timeList = timeList + time
                }
                showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hour", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { 
                                selectedHour = (selectedHour - 1 + 24) % 24 
                            }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase Hour")
                            }
                            Text(
                                text = String.format("%02d", selectedHour),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            IconButton(onClick = { 
                                selectedHour = (selectedHour + 1) % 24 
                            }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease Hour")
                            }
                        }
                    }

                    Text(":", style = MaterialTheme.typography.headlineMedium)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Minute", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { 
                                selectedMinute = (selectedMinute - 1 + 60) % 60 
                            }) { // 修改步长为1分钟
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase Minute")
                            }
                            Text(
                                text = String.format("%02d", selectedMinute),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            IconButton(onClick = { 
                                selectedMinute = (selectedMinute + 1) % 60 
                            }) { // 修改步长为1分钟
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease Minute")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTimeSelected(selectedHour, selectedMinute) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

class MedicationReminderViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicationReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val database = HealthDatabase.getDatabase(application)
            val reminderDao = database.medicationReminderDao()
            return MedicationReminderViewModel(reminderDao, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 