package com.example.healthmanagecenter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val alertId: Long = 0,
    val userId: Long,
    val recordId: Long,
    val message: String,
    val time: Long // timestamp in milliseconds
) 