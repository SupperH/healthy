package com.example.healthmanagecenter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.healthmanagecenter.data.converter.TimeListConverter

@Entity(tableName = "medication_reminders")
@TypeConverters(TimeListConverter::class)
data class MedicationReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,           // Medication name
    val instructions: String,   // Usage instructions
    val timeList: List<String>  // List of reminder times in "HH:mm" format
) 