package com.example.healthmanagecenter.data.dao

import androidx.room.*
import com.example.healthmanagecenter.data.entity.MedicationReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationReminderDao {
    @Insert
    suspend fun insertReminder(reminder: MedicationReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: MedicationReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: MedicationReminderEntity)

    @Query("SELECT * FROM medication_reminders WHERE userId = :userId ORDER BY reminderTime ASC")
    fun getRemindersByUserId(userId: Long): Flow<List<MedicationReminderEntity>>

    @Query("SELECT * FROM medication_reminders WHERE reminderId = :reminderId")
    suspend fun getReminderById(reminderId: Long): MedicationReminderEntity?
} 