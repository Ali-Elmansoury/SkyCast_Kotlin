package com.ities45.skycast.model.remote.hourlyforecast

import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastResponse

interface IHourlyForecastRemoteDataSource {
    suspend fun getWeatherHourly96DataOverNetwork(latitude: String, longitude: String, language: String, units: String): HourlyForecastResponse?

    /** ---------------- GROUPING ---------------- */
    fun groupByDay(hourlyList: List<HourlyForecastItem>, timezoneOffsetSeconds: Int): Map<String, List<HourlyForecastItem>>

    /** ---------------- CITY INFO ---------------- */
    fun getCityName(response: HourlyForecastResponse): String
    fun getTimezoneOffsetSeconds(response: HourlyForecastResponse): Int
    fun getSunrise(response: HourlyForecastResponse): String
    fun getSunset(response: HourlyForecastResponse): String

    /** ---------------- MAIN WEATHER DATA ---------------- */
    fun getTemperatures(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Double>
    fun getFeelsLikeTemps(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Double>
    fun getMinTemps(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Double>
    fun getMaxTemps(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Double>
    fun getPressures(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Int>
    fun getHumidity(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Int>

    /** ---------------- WEATHER CONDITIONS ---------------- */
    fun getWeatherConditions(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<String>
    fun getWeatherDescriptions(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<String>

    /** ---------------- WIND ---------------- */
    fun getWindSpeeds(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Double>
    fun getWindDirections(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Int>

    /** ---------------- VISIBILITY & CLOUDS ---------------- */
    fun getVisibility(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Int>
    fun getCloudCoverage(day: String, grouped: Map<String, List<HourlyForecastItem>>): List<Int>

    /** ---------------- TIME OF DAY ---------------- */
    fun getHourLabels(day: String, grouped: Map<String, List<HourlyForecastItem>>, timezoneOffsetSeconds: Int): List<String>

    /** ---------------- DAILY SUMMARY FOR NEXT DAYS ---------------- */
    fun getNextDaysSummaries(grouped: Map<String, List<HourlyForecastItem>>, count: Int = 4): List<HourlyForecastItem>

}