package com.example.healthmanagecenter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmanagecenter.data.HealthDatabase
import com.example.healthmanagecenter.data.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.healthmanagecenter.navigation.Screen

data class LoginUiState(
    val name: String = "",
    val password: String = "",
    val role: String = "elder",
    val errorMessage: String = ""
)

data class RegisterUiState(
    val name: String = "",
    val phone: String = "",
    val password: String = "",
    val role: String = "elder",
    val errorMessage: String = ""
)

class LoginRegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = HealthDatabase.getDatabase(application).userDao()

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow(RegisterUiState())
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    val isUserLoggedIn: Boolean
        get() = _currentUser.value != null

    fun getStartDestinationBasedOnRole(): String {
        return when (_currentUser.value?.role) {
            "doctor" -> Screen.DoctorHome.createRoute(_currentUser.value!!.userId.toString())
            "elder" -> {
                if (_currentUser.value?.doctorId == null || _currentUser.value?.doctorId == 0L) {
                     Screen.PickDoctor.createRoute(_currentUser.value!!.userId.toString())
                } else {
                     Screen.ElderHome.createRoute(_currentUser.value!!.userId.toString())
                }
            }
            else -> Screen.Login.route
        }
    }

    fun updateLoginName(name: String) {
        _loginUiState.value = _loginUiState.value.copy(name = name)
    }

    fun updateLoginPassword(password: String) {
        _loginUiState.value = _loginUiState.value.copy(password = password)
    }

    fun updateLoginRole(role: String) {
        _loginUiState.value = _loginUiState.value.copy(role = role)
    }

    fun updateRegisterName(name: String) {
        _registerUiState.value = _registerUiState.value.copy(name = name)
    }

    fun updateRegisterPhone(phone: String) {
        _registerUiState.value = _registerUiState.value.copy(phone = phone)
    }

    fun updateRegisterPassword(password: String) {
        _registerUiState.value = _registerUiState.value.copy(password = password)
    }

    fun updateRegisterRole(role: String) {
        _registerUiState.value = _registerUiState.value.copy(role = role)
    }

    suspend fun login(): Triple<Long?, Boolean, Boolean> {
        val state = loginUiState.value
        return try {
            val user = userDao.login(state.name, state.password, state.role)
            if (user != null) {
                _currentUser.value = user
                val isDoctor = user.role == "doctor"
                val needPickDoctor = user.role == "elder" && user.doctorId == null
                Triple(user.userId, isDoctor, needPickDoctor)
            } else {
                _loginUiState.value = state.copy(errorMessage = "Invalid name, password or role")
                Triple(null, false, false)
            }
        } catch (e: Exception) {
            _loginUiState.value = state.copy(errorMessage = "Login failed: ${e.message}")
            Triple(null, false, false)
        }
    }

    suspend fun register(): Boolean {
        val state = registerUiState.value
        return try {
            val userId = userDao.insertUser(
                UserEntity(
                    name = state.name,
                    phone = state.phone,
                    password = state.password,
                    role = state.role
                )
            )
            true
        } catch (e: Exception) {
            _registerUiState.value = state.copy(errorMessage = "Register failed: ${e.message}")
            false
        }
    }

    suspend fun updateUserDoctor(userId: Long, doctorId: Long): Boolean {
        return try {
            val user = userDao.getUserById(userId)?.copy(doctorId = doctorId)
            if (user != null) {
                userDao.updateUser(user)
                _currentUser.value = user
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun getUserByIdState(userId: Long): StateFlow<UserEntity?> {
        val flow = MutableStateFlow<UserEntity?>(null)
        viewModelScope.launch {
            flow.value = userDao.getUserById(userId)
        }
        return flow.asStateFlow()
    }

    fun getDoctorByIdState(doctorId: Long?): StateFlow<UserEntity?> {
        val flow = MutableStateFlow<UserEntity?>(null)
        if (doctorId != null && doctorId > 0) {
            viewModelScope.launch {
                flow.value = userDao.getUserById(doctorId)
            }
        }
        return flow.asStateFlow()
    }

    fun getEldersByDoctorIdState(doctorId: Long): StateFlow<List<UserEntity>> {
        val flow = MutableStateFlow<List<UserEntity>>(emptyList())
        viewModelScope.launch {
            userDao.getEldersByDoctorId(doctorId).collect {
                flow.value = it
            }
        }
        return flow.asStateFlow()
    }
} 