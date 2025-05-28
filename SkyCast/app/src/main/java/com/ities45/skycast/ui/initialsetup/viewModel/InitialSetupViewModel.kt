package com.ities45.skycast.ui.initialsetup.viewModel

import androidx.lifecycle.ViewModel
import com.ities45.skycast.model.repository.settings.SettingsRepository

class InitialSetupViewModel(private val repository: SettingsRepository) : ViewModel() {
    fun saveSettings(locationMethod: String, notificationsEnabled: Boolean) {
        repository.saveSetting(SettingsRepository.KEY_LOCATION, locationMethod)
        repository.saveSetting(
            SettingsRepository.KEY_NOTIFICATIONS,
            if (notificationsEnabled) "enable" else "disable"
        )
        repository.markFirstRunComplete()
    }

    fun isFirstRun() = repository.isFirstRun()
}