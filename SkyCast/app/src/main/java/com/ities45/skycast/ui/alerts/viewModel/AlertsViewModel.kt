package com.ities45.skycast.ui.alerts.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ities45.skycast.model.pojo.Alert
import java.util.Date

class AlertsViewModel : ViewModel() {
    private val _alerts = MutableLiveData<List<Alert>>(emptyList())
    val alerts: LiveData<List<Alert>> get() = _alerts
    private var nextId = 0 // Counter for generating unique IDs

    fun addAlert(
        fromTime: Date,
        fromDate: Date,
        toTime: Date,
        toDate: Date,
        isAlarmEnabled: Boolean,
        isNotificationEnabled: Boolean
    ) {
        val newAlert = Alert(
            id = nextId++,
            fromTime = fromTime,
            fromDate = fromDate,
            toTime = toTime,
            toDate = toDate,
            isAlarmEnabled = isAlarmEnabled,
            isNotificationEnabled = isNotificationEnabled
        )
        val currentList = _alerts.value ?: emptyList()
        _alerts.value = currentList + newAlert
    }

    fun removeAlert(alert: Alert) {
        val currentList = _alerts.value?.toMutableList() ?: mutableListOf()
        currentList.remove(alert)
        _alerts.value = currentList
    }
}