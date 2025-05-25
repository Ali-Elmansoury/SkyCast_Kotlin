package com.ities45.skycast.model.local

import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.local.model.ForecastBundle
import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem

interface IWeatherLocalDataSource {
    suspend fun insertFavoriteLocation(location: FavoriteLocationEntity): Long
    suspend fun insertCurrentWeather(weather: CurrentWeatherResponse)
    suspend fun insertHourlyForecast(forecast: List<HourlyForecastItem>)
    suspend fun deleteFavoriteLocation(location: FavoriteLocationEntity)
    suspend fun getForecastBundleByCity(name: String): ForecastBundle?
    suspend fun getAllForecastBundles(): List<ForecastBundle>
}