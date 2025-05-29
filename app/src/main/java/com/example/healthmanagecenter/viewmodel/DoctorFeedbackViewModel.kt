package com.example.healthmanagecenter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.entity.DoctorFeedbackEntity
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DoctorFeedbackViewModel(application: Application) : AndroidViewModel(application) {
    private val doctorFeedbackDao = HealthDatabase.getDatabase(application).doctorFeedbackDao()
    private val healthRecordDao = HealthDatabase.getDatabase(application).healthRecordDao()

    fun getFeedbacksByElderId(elderId: Long): Flow<List<DoctorFeedbackEntity>> {
        return doctorFeedbackDao.getFeedbacksByElderId(elderId)
    }

    fun getUnreadFeedbackCount(elderId: Long): Flow<Int> {
        return doctorFeedbackDao.getUnreadFeedbackCount(elderId)
    }

    fun getAbnormalFeedbackCount(doctorId: Long): Flow<Int> {
        return doctorFeedbackDao.getAbnormalFeedbackCount(doctorId)
    }

    suspend fun addFeedback(
        elderId: Long,
        doctorId: Long,
        healthRecordId: Long,
        comment: String,
        isAbnormal: Boolean = false,
        abnormalType: String? = null
    ): Boolean {
        return try {
            val record = healthRecordDao.getHealthRecordById(healthRecordId)
            if (record == null) {
                return false
            }
            
            val feedback = DoctorFeedbackEntity(
                elderId = elderId,
                doctorId = doctorId,
                healthRecordId = healthRecordId,
                comment = comment,
                timestamp = System.currentTimeMillis(),
                isAbnormal = isAbnormal,
                abnormalType = abnormalType
            )
            doctorFeedbackDao.insertFeedback(feedback)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markFeedbackAsRead(feedbackId: Long) {
        doctorFeedbackDao.markFeedbackAsRead(feedbackId)
    }

    fun checkHealthRecordAbnormal(record: HealthRecordEntity): Pair<Boolean, String?> {
        val abnormalChecks = mutableListOf<Pair<Boolean, String>>()
        
        // Check heart rate
        record.heartRate?.let { heartRate ->
            if (heartRate < 60 || heartRate > 100) {
                abnormalChecks.add(true to "Abnormal Heart Rate")
            }
        }
        
        // Check blood pressure
        record.bloodPressureHigh?.let { high ->
            record.bloodPressureLow?.let { low ->
                if (high > 140 || low > 90 || low < 60) {
                    abnormalChecks.add(true to "Abnormal Blood Pressure")
                }
            }
        }
        
        // Check sleep hours
        record.sleepHours?.let { sleep ->
            if (sleep < 6 || sleep > 9) {
                abnormalChecks.add(true to "Abnormal Sleep Duration")
            }
        }

        return if (abnormalChecks.isNotEmpty()) {
            true to abnormalChecks.joinToString(", ") { it.second }
        } else {
            false to null
        }
    }
}