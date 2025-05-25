package com.ities45.skycast.model.local

import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.local.model.ForecastBundle
import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem

class WeatherLocalDataSourceImpl(private val weatherDao: IWeatherDAO) : IWeatherLocalDataSource {

    override suspend fun insertFavoriteLocation(location: FavoriteLocationEntity): Long {
        return weatherDao.insertFavoriteLocation(location)
    }

    override suspend fun insertCurrentWeather(weather: CurrentWeatherResponse) {
        weatherDao.insertCurrentWeather(weather)
    }

    override suspend fun insertHourlyForecast(forecast: List<HourlyForecastItem>) {
        weatherDao.insertHourlyForecast(forecast)
    }

    override suspend fun deleteFavoriteLocation(location: FavoriteLocationEntity) {
        weatherDao.deleteFavoriteLocation(location)
    }

    override suspend fun getForecastBundleByCity(name: String): ForecastBundle? {
        return weatherDao.getForecastBundleByCity(name)
    }

    override suspend fun getAllForecastBundles(): List<ForecastBundle> {
        return weatherDao.getAllForecastBundles()
    }
}