package com.ities45.skycast.ui.home.viewModel

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyItem
import com.ities45.skycast.model.repository.weather.IWeatherRepository
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val data: CurrentWeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class HomeViewModel(private val repo: IWeatherRepository, private val context: Context) : ViewModel() {
    private val hourlyForecastList = MutableLiveData<List<HourlyItem>>()
    val onlineHourlyForecastList: LiveData<List<HourlyItem>> = hourlyForecastList

    private val currentForecastState = MutableLiveData<WeatherUiState>()
    val onlineCurrentForecastState: LiveData<WeatherUiState> = currentForecastState

    private val next4SummariesAtNoon = MutableLiveData<List<HourlyForecastItem>>()
    val onlineNext4Days: LiveData<List<HourlyForecastItem>> = next4SummariesAtNoon

    private var latitude: String? = null
    private var longitude: String? = null

    fun setCoordinates(latitude: Double, longitude: Double) {
        this.latitude = latitude.toString()
        this.longitude = longitude.toString()

        // Get language from settings
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val language = prefs.getString("language", "en") ?: "en"

        fetchCurrentWeather(language)
        fetchHourlyForecast(language)
    }

    fun fetchHourlyForecast(language: String = "en") {
        viewModelScope.launch {
            try {
                val lat = latitude ?: "30.07111902938482" // Default if not set
                val lon = longitude ?: "31.021081747751275"
                val hourly = repo.fetchHourlyForecast(lat, lon, language, "metric")
                val hourlyForecastItem = hourly.getOrNull()
                if (hourlyForecastItem != null) {
                    val groupedForecast = repo.groupByDay(
                        hourlyForecastItem.list,
                        repo.getHourlyTimezoneOffsetSeconds(hourlyForecastItem)
                    )
                    val today = groupedForecast.keys.firstOrNull()
                    if (today != null) {
                        val timezoneOffset = repo.getHourlyTimezoneOffsetSeconds(hourlyForecastItem)
                        val temperatures = repo.getTemperatures(today, groupedForecast)
                        val hourLabels = repo.getHourLabels(today, groupedForecast, timezoneOffset)
                        val icon = groupedForecast[today]?.firstOrNull()?.weather?.firstOrNull()?.icon ?: "01d"
                        val displayItems = temperatures.mapIndexed { index, temp ->
                            HourlyItem(
                                temp = temp,
                                hour = hourLabels.getOrNull(index) ?: "",
                                icon = icon
                            )
                        }
                        hourlyForecastList.postValue(displayItems)
                        val nextDays = repo.getNextDaysSummariesAtNoon(groupedForecast)
                        next4SummariesAtNoon.postValue(nextDays)
                    } else {
                        Log.e("HomeViewModel", "No forecast data for today")
                    }
                } else {
                    Log.e("HomeViewModel", "Hourly forecast response is null")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "fetchHourlyForecast failed: ${e.message}", e)
            }
        }
    }

    fun fetchCurrentWeather(language: String = "en") {
        viewModelScope.launch {
            currentForecastState.postValue(WeatherUiState.Loading)
            try {
                val lat = latitude ?: "30.07111902938482" // Default if not set
                val lon = longitude ?: "31.021081747751275"
                val current = repo.fetchCurrentWeather(lat, lon, language, "metric")
                val response = current.getOrNull()
                if (response != null) {
                    currentForecastState.postValue(WeatherUiState.Success(response))
                } else {
                    currentForecastState.postValue(WeatherUiState.Error("Failed to fetch current weather"))
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "fetchCurrentWeather failed: ${e.message}", e)
                currentForecastState.postValue(WeatherUiState.Error(e.message ?: "Unknown error"))
            }
        }
    }
}