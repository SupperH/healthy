package com.example.healthmanagecenter.data.dao

import androidx.room.*
import com.example.healthmanagecenter.data.entity.DoctorFeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorFeedbackDao {
    @Insert
    suspend fun insertFeedback(feedback: DoctorFeedbackEntity): Long

    @Query("SELECT * FROM doctor_feedback WHERE elderId = :elderId AND isRead = 0")
    fun getUnreadFeedbacksForElder(elderId: Long): Flow<List<DoctorFeedbackEntity>>

    @Query("SELECT * FROM doctor_feedback WHERE doctorId = :doctorId ORDER BY timestamp DESC")
    fun getFeedbacksForDoctor(doctorId: Long): Flow<List<DoctorFeedbackEntity>>

    @Query("UPDATE doctor_feedback SET isRead = 1 WHERE id = :id")
    suspend fun markFeedbackAsRead(id: Long)
} 