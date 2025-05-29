package com.example.healthmanagecenter.data.dao

import androidx.room.*
import com.example.healthmanagecenter.data.entity.DoctorFeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorFeedbackDao {
    @Insert
    suspend fun insertFeedback(feedback: DoctorFeedbackEntity): Long

    @Update
    suspend fun updateFeedback(feedback: DoctorFeedbackEntity)

    @Query("SELECT * FROM doctor_feedback WHERE elderId = :elderId ORDER BY timestamp DESC")
    fun getFeedbacksByElderId(elderId: Long): Flow<List<DoctorFeedbackEntity>>

    @Query("SELECT * FROM doctor_feedback WHERE doctorId = :doctorId ORDER BY timestamp DESC")
    fun getFeedbacksByDoctorId(doctorId: Long): Flow<List<DoctorFeedbackEntity>>

    @Query("SELECT COUNT(*) FROM doctor_feedback WHERE elderId = :elderId AND isRead = 0")
    fun getUnreadFeedbackCount(elderId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM doctor_feedback WHERE doctorId = :doctorId AND isAbnormal = 1")
    fun getAbnormalFeedbackCount(doctorId: Long): Flow<Int>

    @Query("UPDATE doctor_feedback SET isRead = 1 WHERE id = :feedbackId")
    fun markFeedbackAsRead(feedbackId: Long)
} 