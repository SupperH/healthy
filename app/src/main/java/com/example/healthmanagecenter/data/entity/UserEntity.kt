package com.example.healthmanagecenter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val name: String,
    val phone: String,
    val role: String, // "doctor" or "elder"
    val password: String,
    val email: String,
    val dateOfBirth: Long,
    val gender: String = "male", // "male" or "female"
    val doctorId: Long? = null // Only for elders
) 