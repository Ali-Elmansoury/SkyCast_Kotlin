package com.ities45.skycast.model.repository

import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.local.model.ForecastBundle
import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastResponse

interface IWeatherRepository {
    // Remote data source methods (Current Weather)
    suspend fun fetchCurrentWeather(
        latitude: String,
        longitude: String,
        language: String,
        units: String
    ): Result<CurrentWeatherResponse>

    fun getLatitude(current: CurrentWeatherResponse): Double
    fun getLongitude(current: CurrentWeatherResponse): Double
    fun getCityName(current: CurrentWeatherResponse): String
    fun getCountryCode(current: CurrentWeatherResponse): String
    fun getTimezoneOffsetSeconds(current: CurrentWeatherResponse): Int
    fun getTimestamp(current: CurrentWeatherResponse): String
    fun getSunrise(current: CurrentWeatherResponse): String
    fun getSunset(current: CurrentWeatherResponse): String
    fun getTemperature(current: CurrentWeatherResponse): Double
    fun getFeelsLikeTemperature(current: CurrentWeatherResponse): Double
    fun getMinTemperature(current: CurrentWeatherResponse): Double
    fun getMaxTemperature(current: CurrentWeatherResponse): Double
    fun getPressure(current: CurrentWeatherResponse): Int
    fun getHumidity(current: CurrentWeatherResponse): Int
    fun getSeaLevel(current: CurrentWeatherResponse): Int?
    fun getGroundLevel(current: CurrentWeatherResponse): Int?
    fun getWeatherMain(current: CurrentWeatherResponse): String?
    fun getWeatherDescription(current: CurrentWeatherResponse): String?
    fun getWeatherIcon(current: CurrentWeatherResponse): String?
    fun getWindSpeed(current: CurrentWeatherResponse): Double
    fun getWindDirection(current: CurrentWeatherResponse): Int
    fun getWindGust(current: CurrentWeatherResponse): Double?
    fun getVisibility(current: CurrentWeatherResponse): Int
    fun getCloudCoverage(current: CurrentWeatherResponse): Int

    // Remote data source methods (Hourly Forecast)
    suspend fun fetchHourlyForecast(
        latitude: String,
        longitude: String,
        language: String,
        units: String
    ): Result<HourlyForecastResponse>

    fun groupByDay(
        hourlyList: List<HourlyForecastItem>,
        timezoneOffsetSeconds: Int
    ): Map<String, List<HourlyForecastItem>>

    fun getHourlyCityName(response: HourlyForecastResponse): String
    fun getHourlyTimezoneOffsetSeconds(response: HourlyForecastResponse): Int
    fun getHourlySunrise(response: HourlyForecastResponse): String
    fun getHourlySunset(response: HourlyForecastResponse): String
    fun getTemperatures(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Double>
    fun getFeelsLikeTemps(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Double>
    fun getMinTemps(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Double>
    fun getMaxTemps(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Double>
    fun getPressures(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Int>
    fun getHumidity(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Int>
    fun getWeatherConditions(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<String>
    fun getWeatherDescriptions(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<String>
    fun getWindSpeeds(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Double>
    fun getWindDirections(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Int>
    fun getVisibility(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Int>
    fun getCloudCoverage(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Int>
    fun getHourLabels(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>,
        timezoneOffsetSeconds: Int
    ): List<String>
    fun getNextDaysSummaries(
        grouped: Map<String, List<HourlyForecastItem>>,
        count: Int
    ): List<HourlyForecastItem>
    fun getNextDaysSummariesAtNoon(grouped: Map<String, List<HourlyForecastItem>>): List<HourlyForecastItem>

    // Local data source methods
    suspend fun storeFavoriteLocation(location: FavoriteLocationEntity): Long
    suspend fun storeCurrentWeather(weather: CurrentWeatherResponse)
    suspend fun storeHourlyForecast(forecast: List<HourlyForecastItem>)
    suspend fun deleteFavoriteLocation(location: FavoriteLocationEntity)
    suspend fun getForecastBundleByCity(cityName: String): ForecastBundle?
    suspend fun getAllForecastBundles(): List<ForecastBundle>
}