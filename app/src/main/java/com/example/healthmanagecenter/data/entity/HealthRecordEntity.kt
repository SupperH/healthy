package com.example.healthmanagecenter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_records")
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val recordId: Long = 0,
    val userId: Long,
    val time: Long, // timestamp in milliseconds
    val weight: Float?, // in kg
    val height: Float?, // in cm
    val heartRate: Int?, // beats per minute
    val bloodPressureHigh: Int?, // systolic
    val bloodPressureLow: Int?, // diastolic
    val sleepHours: Float? // hours of sleep
) 