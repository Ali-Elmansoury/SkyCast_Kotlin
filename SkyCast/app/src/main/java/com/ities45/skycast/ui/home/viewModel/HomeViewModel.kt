package com.ities45.skycast.ui.home.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyItem
import com.ities45.skycast.model.repository.IWeatherRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: IWeatherRepository) : ViewModel() {

    private val hourlyForecastList = MutableLiveData<List<HourlyItem>>()
    val onlineHourlyForecastList : LiveData<List<HourlyItem>> = hourlyForecastList

    private val currentForecast = MutableLiveData<CurrentWeatherResponse>()
    val onlineCurrentForecast: LiveData<CurrentWeatherResponse> = currentForecast

    private val next4SummariesAtNoon = MutableLiveData<List<HourlyForecastItem>>()
    val onlineNext4Days : LiveData<List<HourlyForecastItem>> = next4SummariesAtNoon

    init {
        fetchHourlyForecast()
        fetchCurrentWeather()
    }

    fun fetchHourlyForecast(){
        viewModelScope.launch {
            try {
                val hourly = repo.fetchHourlyForecast(
                    "30.07111902938482",
                    "31.021081747751275",
                    "en",
                    "metric"
                )
                val hourlyForecastItem = hourly.getOrNull()

                val groupedForecast = repo.groupByDay(
                    hourlyForecastItem!!.list,
                    repo.getHourlyTimezoneOffsetSeconds(hourlyForecastItem)
                )

                val today = groupedForecast.keys.firstOrNull()

                if (today != null) {
                    val timezoneOffset = repo.getHourlyTimezoneOffsetSeconds(hourlyForecastItem)
                    val temperatures = repo.getTemperatures(today, groupedForecast)
                    val hourLabels = repo.getHourLabels(today, groupedForecast, timezoneOffset)
                    val icon = groupedForecast[today]?.firstOrNull()?.weather?.firstOrNull()?.icon ?: "01d"

                    // Create a list of HourlyForecastDisplayItem
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
                }
            }
            catch (e: Exception){

            }
        }
    }

    fun fetchCurrentWeather(){
        viewModelScope.launch {
            try {
                val current = repo.fetchCurrentWeather("30.07111902938482", "31.021081747751275", "en", "metric")
                currentForecast.postValue(current.getOrNull())
            }
            catch (e: Exception){

            }
        }
    }
}