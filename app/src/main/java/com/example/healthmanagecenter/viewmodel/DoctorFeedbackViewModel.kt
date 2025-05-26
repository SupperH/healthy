package com.example.healthmanagecenter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.entity.DoctorFeedbackEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DoctorFeedbackViewModel(
    application: Application,
    private val doctorId: Long
) : AndroidViewModel(application) {
    private val db = HealthDatabase.getDatabase(application)
    private val feedbackDao = db.doctorFeedbackDao()

    val feedbackList = feedbackDao.getFeedbacksForDoctor(doctorId).stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    val unreadCount = feedbackList.map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun submitComment(feedback: DoctorFeedbackEntity, comment: String) = viewModelScope.launch {
        feedbackDao.insertFeedback(
            feedback.copy(comment = comment, timestamp = System.currentTimeMillis())
        )
    }
}