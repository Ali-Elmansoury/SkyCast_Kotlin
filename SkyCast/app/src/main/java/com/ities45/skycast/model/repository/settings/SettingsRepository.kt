package com.ities45.skycast.model.repository.settings

import android.content.SharedPreferences

class SettingsRepository(private val prefs: SharedPreferences) {

    companion object {
        const val KEY_LOCATION = "location"
        const val KEY_LANGUAGE = "language"
        const val KEY_TEMPERATURE = "temperature"
        const val KEY_WIND_SPEED = "wind_speed"
        const val KEY_NOTIFICATIONS = "notifications"
    }

    fun getSetting(key: String, default: String): String {
        return prefs.getString(key, default) ?: default
    }

    fun saveSetting(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}
