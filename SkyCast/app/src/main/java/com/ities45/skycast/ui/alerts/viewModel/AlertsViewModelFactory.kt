package com.ities45.skycast.ui.alerts.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AlertsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
            return AlertsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}