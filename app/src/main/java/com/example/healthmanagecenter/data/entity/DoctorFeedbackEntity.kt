package com.example.healthmanagecenter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctor_feedback")
data class DoctorFeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val elderId: Long,
    val healthRecordId: Long,
    val doctorId: Long,
    val comment: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val isAbnormal: Boolean = false,  // 标记是否为异常数据
    val abnormalType: String? = null  // 异常类型（如：心率异常、血压异常等）
) 