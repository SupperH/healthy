package com.example.healthmanagecenter.data.dao

import androidx.room.*
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: HealthRecordEntity): Long

    @Query("SELECT * FROM health_records WHERE userId = :userId AND date(timestamp/1000, 'unixepoch') = date(:today/1000, 'unixepoch') LIMIT 1")
    suspend fun getTodayRecord(userId: Long, today: Long): HealthRecordEntity?

    @Query("SELECT * FROM health_records WHERE userId = :userId ORDER BY timestamp DESC LIMIT 7")
    fun getRecentRecords(userId: Long): Flow<List<HealthRecordEntity>>

    @Update
    suspend fun updateHealthRecord(record: HealthRecordEntity)

    @Delete
    suspend fun deleteHealthRecord(record: HealthRecordEntity)

    @Query("SELECT * FROM health_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHealthRecordsByUserId(userId: Long): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getHealthRecordsByTimeRange(userId: Long, startTime: Long, endTime: Long): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE recordId = :recordId")
    suspend fun getHealthRecordById(recordId: Long): HealthRecordEntity?

    @Query("SELECT * FROM health_records WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRecord(userId: Long): Flow<HealthRecordEntity?>
} 