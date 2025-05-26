package com.example.healthmanagecenter.data.dao

import androidx.room.*
import com.example.healthmanagecenter.data.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Insert
    suspend fun insertAlert(alert: AlertEntity): Long

    @Delete
    suspend fun deleteAlert(alert: AlertEntity)

    @Query("SELECT * FROM alerts WHERE userId IN (SELECT userId FROM users WHERE doctorId = :doctorId) ORDER BY time DESC")
    fun getAlertsByDoctorId(doctorId: Long): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE userId = :userId ORDER BY time DESC")
    fun getAlertsByUserId(userId: Long): Flow<List<AlertEntity>>
} 