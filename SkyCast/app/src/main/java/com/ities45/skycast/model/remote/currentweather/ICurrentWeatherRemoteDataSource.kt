package com.ities45.skycast.model.remote.currentweather

import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse

interface ICurrentWeatherRemoteDataSource {
    suspend fun getCurrentWeatherOverNetwork(latitude: String, longitude: String, language: String, units: String): CurrentWeatherResponse?

    // ---------- COORDINATES ----------
    fun getLatitude(current: CurrentWeatherResponse): Double
    fun getLongitude(current: CurrentWeatherResponse): Double

    // ---------- CITY INFO ----------
    fun getCityName(current: CurrentWeatherResponse): String
    fun getCountryCode(current: CurrentWeatherResponse): String
    fun getTimezoneOffsetSeconds(current: CurrentWeatherResponse): Int
    fun getTimestamp(current: CurrentWeatherResponse): String

    // ---------- SUNRISE & SUNSET ----------
    fun getSunrise(current: CurrentWeatherResponse): String
    fun getSunset(current: CurrentWeatherResponse): String

    // ---------- MAIN WEATHER DATA ----------
    fun getTemperature(current: CurrentWeatherResponse): Double
    fun getFeelsLikeTemperature(current: CurrentWeatherResponse): Double
    fun getMinTemperature(current: CurrentWeatherResponse): Double
    fun getMaxTemperature(current: CurrentWeatherResponse): Double
    fun getPressure(current: CurrentWeatherResponse): Int
    fun getHumidity(current: CurrentWeatherResponse): Int
    fun getSeaLevel(current: CurrentWeatherResponse): Int?
    fun getGroundLevel(current: CurrentWeatherResponse): Int?

    // ---------- WEATHER CONDITIONS ----------
    fun getWeatherMain(current: CurrentWeatherResponse): String?
    fun getWeatherDescription(current: CurrentWeatherResponse): String?
    fun getWeatherIcon(current: CurrentWeatherResponse): String?

    // ---------- WIND ----------
    fun getWindSpeed(current: CurrentWeatherResponse): Double
    fun getWindDirection(current: CurrentWeatherResponse): Int
    fun getWindGust(current: CurrentWeatherResponse): Double?

    // ---------- VISIBILITY & CLOUDS ----------
    fun getVisibility(current: CurrentWeatherResponse): Int
    fun getCloudCoverage(current: CurrentWeatherResponse): Int

}