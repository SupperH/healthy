package com.example.healthmanagecenter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ElderHomeViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = HealthDatabase.getDatabase(application).userDao()

    private val _doctor = MutableStateFlow<UserEntity?>(null)
    val doctor: StateFlow<UserEntity?> = _doctor.asStateFlow()

    suspend fun loadDoctor(doctorId: Long) {
        _doctor.value = userDao.getUserById(doctorId)
    }

    fun getAllDoctors(): Flow<List<UserEntity>> {
        return userDao.getAllDoctors()
    }
} 