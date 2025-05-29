package com.example.healthmanagecenter.ui.screen

import android.app.Application
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationReminderScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val viewModel: MedicationReminderViewModel = viewModel(factory = MedicationReminderViewModelFactory(context as Application))
    val reminders by viewModel.reminders.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<MedicationReminderEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用药提醒") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "添加提醒")
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
                viewModel.updateReminder(reminder.copy(
                    name = name,
                    instructions = instructions,
                    timeList = timeList
                ))
                editingReminder = null
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
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
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
        title = { Text(if (reminder == null) "添加用药提醒" else "编辑用药提醒") },
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
                    label = { Text("药品名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("服用说明") },
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
                                    contentDescription = "移除",
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
                    Text("添加时间")
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
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
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
        title = { Text("选择时间") },
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
                    // 小时选择
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("小时", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { 
                                selectedHour = (selectedHour - 1 + 24) % 24 
                            }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "增加小时")
                            }
                            Text(
                                text = String.format("%02d", selectedHour),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            IconButton(onClick = { 
                                selectedHour = (selectedHour + 1) % 24 
                            }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "减少小时")
                            }
                        }
                    }

                    Text(":", style = MaterialTheme.typography.headlineMedium)

                    // 分钟选择
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("分钟", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { 
                                selectedMinute = (selectedMinute - 5 + 60) % 60 
                            }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "增加分钟")
                            }
                            Text(
                                text = String.format("%02d", selectedMinute),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            IconButton(onClick = { 
                                selectedMinute = (selectedMinute + 5) % 60 
                            }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "减少分钟")
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
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
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