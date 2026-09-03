package com.eventsphere.scanner.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.eventsphere.scanner.data.api.RetrofitClient
import com.eventsphere.scanner.data.local.PreferencesManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    private val _baseUrl = MutableLiveData<String>()
    val baseUrl: LiveData<String> get() = _baseUrl

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> get() = _saveSuccess

    init {
        _baseUrl.value = preferencesManager.baseUrl
    }

    fun saveBaseUrl(newUrl: String) {
        if (newUrl.isNotBlank()) {
            preferencesManager.baseUrl = newUrl
            // Re-initialize RetrofitClient
            RetrofitClient.reset()
            _saveSuccess.value = true
        } else {
            _saveSuccess.value = false
        }
    }
}
