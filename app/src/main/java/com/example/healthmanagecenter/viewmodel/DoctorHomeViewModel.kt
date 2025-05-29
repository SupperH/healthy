package com.example.healthmanagecenter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.entity.AlertEntity
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import com.example.healthmanagecenter.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

data class ElderWithLatestHealth(
    val elder: UserEntity,
    val latestRecord: HealthRecordEntity?
)

class DoctorHomeViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = HealthDatabase.getDatabase(application).userDao()
    private val alertDao = HealthDatabase.getDatabase(application).alertDao()
    private val healthRecordDao = HealthDatabase.getDatabase(application).healthRecordDao()
    private val doctorFeedbackDao = HealthDatabase.getDatabase(application).doctorFeedbackDao()
    private val doctorFeedbackViewModel = DoctorFeedbackViewModel(application)

    fun getElders(doctorId: Long): Flow<List<UserEntity>> {
        return userDao.getEldersByDoctorId(doctorId)
    }

    fun getAlerts(doctorId: Long): Flow<List<AlertEntity>> {
        return alertDao.getAlertsByDoctorId(doctorId)
    }

    suspend fun getLatestHealthRecord(userId: Long): HealthRecordEntity? {
        return healthRecordDao.getHealthRecordsByUserId(userId)
            .firstOrNull()?.firstOrNull()
    }

    fun getAbnormalFeedbackCount(doctorId: Long): Flow<Int> {
        return doctorFeedbackDao.getAbnormalFeedbackCount(doctorId)
    }

    suspend fun addFeedback(
        elderId: Long,
        doctorId: Long,
        healthRecordId: Long,
        comment: String
    ): Boolean {
        return try {
            val record = healthRecordDao.getHealthRecordById(healthRecordId)
            record?.let {
                val (isAbnormal, abnormalType) = doctorFeedbackViewModel.checkHealthRecordAbnormal(it)
                doctorFeedbackViewModel.addFeedback(
                    elderId = elderId,
                    doctorId = doctorId,
                    healthRecordId = healthRecordId,
                    comment = comment,
                    isAbnormal = isAbnormal,
                    abnormalType = abnormalType
                )
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
} 