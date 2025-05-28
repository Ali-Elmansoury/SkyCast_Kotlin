package com.ities45.skycast.ui.initialsetup.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ities45.skycast.model.repository.settings.SettingsRepository

class InitialSetupViewModelFactory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InitialSetupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InitialSetupViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}