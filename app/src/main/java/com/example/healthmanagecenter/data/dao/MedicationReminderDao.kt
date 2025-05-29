package com.example.healthmanagecenter.data.dao

import androidx.room.*
import com.example.healthmanagecenter.data.entity.MedicationReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationReminderDao {
    @Query("SELECT * FROM medication_reminders ORDER BY id DESC")
    fun getAllReminders(): Flow<List<MedicationReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MedicationReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: MedicationReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: MedicationReminderEntity)

    @Query("SELECT * FROM medication_reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): MedicationReminderEntity?
} 