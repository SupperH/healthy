package com.example.healthmanagecenter.data.dao

import androidx.room.*
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {
    @Insert
    suspend fun insertHealthRecord(record: HealthRecordEntity): Long

    @Update
    suspend fun updateHealthRecord(record: HealthRecordEntity)

    @Delete
    suspend fun deleteHealthRecord(record: HealthRecordEntity)

    @Query("SELECT * FROM health_records WHERE userId = :userId ORDER BY time DESC")
    fun getHealthRecordsByUserId(userId: Long): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE userId = :userId AND time BETWEEN :startTime AND :endTime ORDER BY time DESC")
    fun getHealthRecordsByTimeRange(userId: Long, startTime: Long, endTime: Long): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE recordId = :recordId")
    suspend fun getHealthRecordById(recordId: Long): HealthRecordEntity?
} 