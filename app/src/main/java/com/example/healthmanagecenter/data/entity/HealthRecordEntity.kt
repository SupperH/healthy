package com.example.healthmanagecenter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_records")
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val recordId: Long = 0,
    val userId: Long,
    val timestamp: Long, // record time in millis
    val weight: Float?, // kg
    val height: Float?, // cm
    val heartRate: Int?, // bpm
    val bloodPressureHigh: Int?, // systolic
    val bloodPressureLow: Int?, // diastolic
    val sleepHours: Float?, // hours
    val isAnalyzed: Boolean = false, // whether analyzed
    val hasFeedback: Boolean = false // whether doctor has provided feedback for this record
) 