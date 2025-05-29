package com.example.healthmanagecenter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.entity.DoctorFeedbackEntity
import com.example.healthmanagecenter.data.entity.HealthRecordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HealthRecordViewModel(application: Application, private val userId: Long) : AndroidViewModel(application) {
    private val db = HealthDatabase.getDatabase(application)
    val healthRecordDao = db.healthRecordDao()
    private val doctorFeedbackDao = db.doctorFeedbackDao()

    private val _uiState = MutableStateFlow(HealthRecordUiState())
    val uiState: StateFlow<HealthRecordUiState> = _uiState.asStateFlow()

    init { loadTodayRecord() }

    fun loadTodayRecord() = viewModelScope.launch {
        val today = getTodayMillis()
        val record = healthRecordDao.getTodayRecord(userId, today)
        if (record != null) {
            _uiState.value = _uiState.value.copy(
                weight = record.weight?.toString() ?: "",
                height = record.height?.toString() ?: "",
                heartRate = record.heartRate?.toString() ?: "",
                bloodPressureHigh = record.bloodPressureHigh?.toString() ?: "",
                bloodPressureLow = record.bloodPressureLow?.toString() ?: "",
                sleepHours = record.sleepHours?.toString() ?: "",
                isFirstInput = false
            )
            analyze(record)
        } else {
            _uiState.value = _uiState.value.copy(isFirstInput = true)
        }
    }

    fun updateWeight(v: String) { _uiState.value = _uiState.value.copy(weight = v) }
    fun updateHeight(v: String) { _uiState.value = _uiState.value.copy(height = v) }
    fun updateHeartRate(v: String) { _uiState.value = _uiState.value.copy(heartRate = v) }
    fun updateBloodPressureHigh(v: String) { _uiState.value = _uiState.value.copy(bloodPressureHigh = v) }
    fun updateBloodPressureLow(v: String) { _uiState.value = _uiState.value.copy(bloodPressureLow = v) }
    fun updateSleepHours(v: String) { _uiState.value = _uiState.value.copy(sleepHours = v) }

    fun saveOrUpdateRecord() = viewModelScope.launch {
        val record = HealthRecordEntity(
            userId = userId,
            timestamp = System.currentTimeMillis(),
            weight = _uiState.value.weight.toFloatOrNull(),
            height = _uiState.value.height.toFloatOrNull(),
            heartRate = _uiState.value.heartRate.toIntOrNull(),
            bloodPressureHigh = _uiState.value.bloodPressureHigh.toIntOrNull(),
            bloodPressureLow = _uiState.value.bloodPressureLow.toIntOrNull(),
            sleepHours = _uiState.value.sleepHours.toFloatOrNull(),
            isAnalyzed = true
        )
        healthRecordDao.insertOrUpdate(record)
        analyze(record)
    }

    private fun analyze(record: HealthRecordEntity) {
        var score = 100
        val tips = mutableListOf<String>()
        val normalWeight = 65f
        if (record.weight != null && (record.weight < normalWeight * 0.85f || record.weight > normalWeight * 1.15f)) {
            score -= 10; tips.add("Abnormal weight")
        }
        if (record.heartRate != null && (record.heartRate < 60 || record.heartRate > 100)) {
            score -= 15; tips.add("Abnormal heart rate")
        }
        if (record.bloodPressureHigh != null && record.bloodPressureLow != null) {
            if (record.bloodPressureHigh > 140 || record.bloodPressureLow > 90 ||
                record.bloodPressureHigh < 90 || record.bloodPressureLow < 60) {
                score -= 15; tips.add("Abnormal blood pressure")
            }
        }
        if (record.sleepHours != null && (record.sleepHours < 6 || record.sleepHours > 10)) {
            score -= 10; tips.add("Abnormal sleep duration")
        }
        _uiState.value = _uiState.value.copy(score = score, abnormalTips = tips.joinToString())
    }

    private fun getTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

data class HealthRecordUiState(
    val weight: String = "",
    val height: String = "",
    val heartRate: String = "",
    val bloodPressureHigh: String = "",
    val bloodPressureLow: String = "",
    val sleepHours: String = "",
    val isFirstInput: Boolean = true,
    val score: Int = 100,
    val abnormalTips: String = "",
    val doctorComment: String = "",
    val dateStr: String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
    val unreadCount: Int = 0
) 