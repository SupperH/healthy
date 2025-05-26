package com.example.healthmanagecenter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_reminders")
data class MedicationReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val reminderId: Long = 0,
    val userId: Long,
    val medicineName: String,
    val reminderTime: String, // Format: "HH:mm"
    val note: String?
) 