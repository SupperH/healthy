package com.example.healthmanagecenter.data.dao

import androidx.room.*
import com.example.healthmanagecenter.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE name = :name AND password = :password AND role = :role LIMIT 1")
    suspend fun login(name: String, password: String, role: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'doctor'")
    fun getAllDoctors(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'elder' AND doctorId = :doctorId")
    fun getEldersByDoctorId(doctorId: Long): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE name = :name AND email = :email LIMIT 1")
    suspend fun getUserByNameAndEmail(name: String, email: String): UserEntity?
} 