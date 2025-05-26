package com.ities45.skycast.ui.settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ities45.skycast.model.repository.settings.SettingsRepository
import com.ities45.skycast.model.repository.settings.SettingsRepository.Companion.KEY_LANGUAGE
import com.ities45.skycast.model.repository.settings.SettingsRepository.Companion.KEY_LOCATION
import com.ities45.skycast.model.repository.settings.SettingsRepository.Companion.KEY_NOTIFICATIONS
import com.ities45.skycast.model.repository.settings.SettingsRepository.Companion.KEY_TEMPERATURE
import com.ities45.skycast.model.repository.settings.SettingsRepository.Companion.KEY_WIND_SPEED

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _location = MutableLiveData<String>()
    val location: LiveData<String> get() = _location

    private val _language = MutableLiveData<String>()
    val language: LiveData<String> get() = _language

    private val _temperature = MutableLiveData<String>()
    val temperature: LiveData<String> get() = _temperature

    private val _windSpeed = MutableLiveData<String>()
    val windSpeed: LiveData<String> get() = _windSpeed

    private val _notifications = MutableLiveData<String>()
    val notifications: LiveData<String> get() = _notifications

    fun loadSettings() {
        _location.value = repository.getSetting(KEY_LOCATION, "gps")
        _language.value = repository.getSetting(KEY_LANGUAGE, "en")
        _temperature.value = repository.getSetting(KEY_TEMPERATURE, "celsius")
        _windSpeed.value = repository.getSetting(KEY_WIND_SPEED, "meter_sec")
        _notifications.value = repository.getSetting(KEY_NOTIFICATIONS, "enable")
    }

    fun updateSetting(key: String, value: String) {
        repository.saveSetting(key, value)
        when (key) {
            KEY_LOCATION -> _location.value = value
            KEY_LANGUAGE -> _language.value = value
            KEY_TEMPERATURE -> _temperature.value = value
            KEY_WIND_SPEED -> _windSpeed.value = value
            KEY_NOTIFICATIONS -> _notifications.value = value
        }
    }
}
