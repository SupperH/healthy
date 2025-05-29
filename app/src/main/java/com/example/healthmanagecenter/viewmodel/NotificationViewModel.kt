package com.example.healthmanagecenter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.entity.DoctorFeedbackEntity
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import com.example.healthmanagecenter.data.entity.MedicationReminderEntity
import com.example.healthmanagecenter.data.dao.DoctorFeedbackDao
import com.example.healthmanagecenter.data.dao.MedicationReminderDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class NotificationUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val unreadFeedbackCount: Int = 0
) {
    data class NotificationItem(
        val id: Long,
        val type: Type,
        val title: String,
        val content: String,
        val timeStr: String,
        val isRead: Boolean
    )

    enum class Type {
        Feedback,
        Medication
    }
}

class NotificationViewModel(application: Application, private val userId: Long) : AndroidViewModel(application) {
    private val db = HealthDatabase.getDatabase(application)
    private val doctorFeedbackDao = db.doctorFeedbackDao()
    private val medicationReminderDao = db.medicationReminderDao()

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            combine(
                doctorFeedbackDao.getFeedbacksByElderId(userId),
                doctorFeedbackDao.getUnreadFeedbackCount(userId),
                medicationReminderDao.getAllReminders()
            ) { feedbacks, unreadCount, reminders ->
                val feedbackItems = feedbacks.map {
                    NotificationUiState.NotificationItem(
                        id = it.id,
                        type = NotificationUiState.Type.Feedback,
                        title = "医生反馈",
                        content = it.comment,
                        timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            .format(it.timestamp),
                        isRead = it.isRead
                    )
                }

                val reminderItems = reminders.map {
                    NotificationUiState.NotificationItem(
                        id = it.id,
                        type = NotificationUiState.Type.Medication,
                        title = "用药提醒",
                        content = "${it.name} - ${it.instructions}",
                        timeStr = it.timeList.joinToString(", "),
                        isRead = true
                    )
                }

                NotificationUiState(
                    notifications = (feedbackItems + reminderItems).sortedByDescending { it.timeStr },
                    unreadFeedbackCount = unreadCount
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun markAsRead(item: NotificationUiState.NotificationItem) {
        if (item.type == NotificationUiState.Type.Feedback) {
            viewModelScope.launch {
                doctorFeedbackDao.markFeedbackAsRead(item.id)
            }
        }
    }
}