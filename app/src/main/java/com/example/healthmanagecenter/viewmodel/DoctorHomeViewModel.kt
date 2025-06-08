package com.example.healthmanagecenter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.entity.AlertEntity
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import com.example.healthmanagecenter.data.entity.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.healthmanagecenter.viewmodel.DoctorFeedbackViewModel

data class ElderWithLatestHealth(
    val elder: UserEntity,
    val latestRecord: HealthRecordEntity?
)

data class ElderBrief(
    val userId: Long,
    val name: String,
    val phone: String,
    val lastRecordTime: String, // 可补充健康数据时间
    val healthStatus: String // 可补充健康状态
)

class DoctorHomeViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = HealthDatabase.getDatabase(application).userDao()
    private val alertDao = HealthDatabase.getDatabase(application).alertDao()
    private val healthRecordDao = HealthDatabase.getDatabase(application).healthRecordDao()
    private val doctorFeedbackDao = HealthDatabase.getDatabase(application).doctorFeedbackDao()
    private val doctorFeedbackViewModel = DoctorFeedbackViewModel(application)

    fun getEldersWithLatestHealth(doctorId: Long): Flow<List<ElderBrief>> = flow {
        userDao.getEldersByDoctorId(doctorId).collect { elders ->
            val elderBriefList = elders.map { elder ->
                val latestRecord = healthRecordDao.getLatestRecord(elder.userId).firstOrNull()
                val lastRecordTime = latestRecord?.let {
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it.timestamp))
                } ?: "暂无记录"
                val healthStatus = latestRecord?.let {
                    val (isAbnormal, abnormalType) = doctorFeedbackViewModel.checkHealthRecordAbnormal(it)
                    if (isAbnormal) abnormalType ?: "异常" else "正常"
                } ?: "暂无数据"
                ElderBrief(
                    userId = elder.userId,
                    name = elder.name,
                    phone = elder.phone,
                    lastRecordTime = lastRecordTime,
                    healthStatus = healthStatus
                )
            }
            emit(elderBriefList)
        }
    }

    fun getAlerts(doctorId: Long): Flow<List<AlertEntity>> {
        return alertDao.getAlertsByDoctorId(doctorId)
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