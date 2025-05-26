package com.example.healthmanagecenter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.entity.DoctorFeedbackEntity
import com.example.healthmanagecenter.data.entity.MedicationReminderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationViewModel(application: Application, private val userId: Long) : AndroidViewModel(application) {
    private val db = HealthDatabase.getDatabase(application)
    private val doctorFeedbackDao = db.doctorFeedbackDao()
    private val medicationReminderDao = db.medicationReminderDao()

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init { loadNotifications() }

    fun loadNotifications() = viewModelScope.launch {
        combine(
            doctorFeedbackDao.getUnreadFeedbacksForElder(userId),
            medicationReminderDao.getRemindersByUserId(userId)
        ) { feedbacks, reminders ->
            val feedbackItems = feedbacks.map {
                NotificationUiState.NotificationItem(
                    id = it.id,
                    type = NotificationUiState.Type.Feedback,
                    title = "Doctor Feedback",
                    content = it.comment,
                    timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it.timestamp)),
                    isRead = it.isRead
                )
            }
            val reminderItems = reminders.map {
                NotificationUiState.NotificationItem(
                    id = it.reminderId,
                    type = NotificationUiState.Type.Reminder,
                    title = "Medication Reminder",
                    content = "Take ${it.medicineName} at ${it.reminderTime}",
                    timeStr = it.reminderTime,
                    isRead = false // 可扩展为已读
                )
            }
            (feedbackItems + reminderItems).sortedByDescending { it.timeStr }
        }.collect { list ->
            _uiState.value = _uiState.value.copy(notifications = list)
        }
    }

    fun markAsRead(item: NotificationUiState.NotificationItem) = viewModelScope.launch {
        if (item.type == NotificationUiState.Type.Feedback) {
            doctorFeedbackDao.markFeedbackAsRead(item.id)
        }
        // MedicationReminder 可扩展为已读
        loadNotifications()
    }
}

data class NotificationUiState(
    val notifications: List<NotificationItem> = emptyList()
) {
    data class NotificationItem(
        val id: Long,
        val type: Type,
        val title: String,
        val content: String,
        val timeStr: String,
        val isRead: Boolean
    )
    enum class Type { Feedback, Reminder }
} 