package com.eventsphere.scanner.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eventsphere.scanner.data.local.PreferencesManager
import com.eventsphere.scanner.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val authRepository = AuthRepository(preferencesManager)

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        _userData.value = UserData(
            name = preferencesManager.userName ?: "N/A",
            email = preferencesManager.userEmail ?: "N/A",
            role = preferencesManager.userRole ?: "N/A"
        )
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val result = authRepository.logout()
            result.onSuccess {
                _uiState.value = ProfileUiState.LoggedOut
            }.onFailure {
                // Even if API logout fails, we cleared local auth in repository
                _uiState.value = ProfileUiState.LoggedOut
            }
        }
    }
}

data class UserData(
    val name: String,
    val email: String,
    val role: String
)

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    object LoggedOut : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
