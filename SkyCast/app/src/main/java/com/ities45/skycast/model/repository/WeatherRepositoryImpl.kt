package com.ities45.skycast.model.repository

import android.util.Log
import com.ities45.skycast.model.local.IWeatherLocalDataSource
import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.local.model.ForecastBundle
import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastResponse
import com.ities45.skycast.model.remote.currentweather.ICurrentWeatherRemoteDataSource
import com.ities45.skycast.model.remote.hourlyforecast.IHourlyForecastRemoteDataSource

class WeatherRepositoryImpl(
    private val localDataSource: IWeatherLocalDataSource,
    private val currentWeatherRemoteDataSource: ICurrentWeatherRemoteDataSource,
    private val hourlyForecastRemoteDataSource: IHourlyForecastRemoteDataSource
) : IWeatherRepository {

    // Remote data source methods (Current Weather)
    override suspend fun fetchCurrentWeather(
        latitude: String,
        longitude: String,
        language: String,
        units: String
    ): Result<CurrentWeatherResponse> {
        return try {
            val response = currentWeatherRemoteDataSource.getCurrentWeatherOverNetwork(
                latitude, longitude, language, units
            )
            if (response != null) {
                Result.success(response)
            } else {
                Result.failure(Exception("Failed to fetch current weather"))
            }
        } catch (e: Exception) {
            Log.e("WeatherRepositoryImpl", "fetchCurrentWeather: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getLatitude(current: CurrentWeatherResponse): Double =
        currentWeatherRemoteDataSource.getLatitude(current)

    override fun getLongitude(current: CurrentWeatherResponse): Double =
        currentWeatherRemoteDataSource.getLongitude(current)

    override fun getCityName(current: CurrentWeatherResponse): String =
        currentWeatherRemoteDataSource.getCityName(current)

    override fun getCountryCode(current: CurrentWeatherResponse): String =
        currentWeatherRemoteDataSource.getCountryCode(current)

    override fun getTimezoneOffsetSeconds(current: CurrentWeatherResponse): Int =
        currentWeatherRemoteDataSource.getTimezoneOffsetSeconds(current)

    override fun getTimestamp(current: CurrentWeatherResponse): String =
        currentWeatherRemoteDataSource.getTimestamp(current)

    override fun getSunrise(current: CurrentWeatherResponse): String =
        currentWeatherRemoteDataSource.getSunrise(current)

    override fun getSunset(current: CurrentWeatherResponse): String =
        currentWeatherRemoteDataSource.getSunset(current)

    override fun getTemperature(current: CurrentWeatherResponse): Double =
        currentWeatherRemoteDataSource.getTemperature(current)

    override fun getFeelsLikeTemperature(current: CurrentWeatherResponse): Double =
        currentWeatherRemoteDataSource.getFeelsLikeTemperature(current)

    override fun getMinTemperature(current: CurrentWeatherResponse): Double =
        currentWeatherRemoteDataSource.getMinTemperature(current)

    override fun getMaxTemperature(current: CurrentWeatherResponse): Double =
        currentWeatherRemoteDataSource.getMaxTemperature(current)

    override fun getPressure(current: CurrentWeatherResponse): Int =
        currentWeatherRemoteDataSource.getPressure(current)

    override fun getHumidity(current: CurrentWeatherResponse): Int =
        currentWeatherRemoteDataSource.getHumidity(current)

    override fun getSeaLevel(current: CurrentWeatherResponse): Int? =
        currentWeatherRemoteDataSource.getSeaLevel(current)

    override fun getGroundLevel(current: CurrentWeatherResponse): Int? =
        currentWeatherRemoteDataSource.getGroundLevel(current)

    override fun getWeatherMain(current: CurrentWeatherResponse): String? =
        currentWeatherRemoteDataSource.getWeatherMain(current)

    override fun getWeatherDescription(current: CurrentWeatherResponse): String? =
        currentWeatherRemoteDataSource.getWeatherDescription(current)

    override fun getWeatherIcon(current: CurrentWeatherResponse): String? =
        currentWeatherRemoteDataSource.getWeatherIcon(current)

    override fun getWindSpeed(current: CurrentWeatherResponse): Double =
        currentWeatherRemoteDataSource.getWindSpeed(current)

    override fun getWindDirection(current: CurrentWeatherResponse): Int =
        currentWeatherRemoteDataSource.getWindDirection(current)

    override fun getWindGust(current: CurrentWeatherResponse): Double? =
        currentWeatherRemoteDataSource.getWindGust(current)

    override fun getVisibility(current: CurrentWeatherResponse): Int =
        currentWeatherRemoteDataSource.getVisibility(current)

    override fun getCloudCoverage(current: CurrentWeatherResponse): Int =
        currentWeatherRemoteDataSource.getCloudCoverage(current)

    // Remote data source methods (Hourly Forecast)
    override suspend fun fetchHourlyForecast(
        latitude: String,
        longitude: String,
        language: String,
        units: String
    ): Result<HourlyForecastResponse> {
        return try {
            val response = hourlyForecastRemoteDataSource.getWeatherHourly96DataOverNetwork(
                latitude, longitude, language, units
            )
            if (response != null) {
                Result.success(response)
            } else {
                Result.failure(Exception("Failed to fetch hourly forecast"))
            }
        } catch (e: Exception) {
            Log.e("WeatherRepositoryImpl", "fetchHourlyForecast: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun groupByDay(
        hourlyList: List<HourlyForecastItem>,
        timezoneOffsetSeconds: Int
    ): Map<String, List<HourlyForecastItem>> =
        hourlyForecastRemoteDataSource.groupByDay(hourlyList, timezoneOffsetSeconds)

    override fun getHourlyCityName(response: HourlyForecastResponse): String =
        hourlyForecastRemoteDataSource.getCityName(response)

    override fun getHourlyTimezoneOffsetSeconds(response: HourlyForecastResponse): Int =
        hourlyForecastRemoteDataSource.getTimezoneOffsetSeconds(response)

    override fun getHourlySunrise(response: HourlyForecastResponse): String =
        hourlyForecastRemoteDataSource.getSunrise(response)

    override fun getHourlySunset(response: HourlyForecastResponse): String =
        hourlyForecastRemoteDataSource.getSunset(response)

    override fun getTemperatures(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Double> = hourlyForecastRemoteDataSource.getTemperatures(day, grouped)

    override fun getFeelsLikeTemps(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Double> = hourlyForecastRemoteDataSource.getFeelsLikeTemps(day, grouped)

    override fun getMinTemps(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Double> = hourlyForecastRemoteDataSource.getMinTemps(day, grouped)

    override fun getMaxTemps(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Double> = hourlyForecastRemoteDataSource.getMaxTemps(day, grouped)

    override fun getPressures(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Int> = hourlyForecastRemoteDataSource.getPressures(day, grouped)

    override fun getHumidity(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Int> = hourlyForecastRemoteDataSource.getHumidity(day, grouped)

    override fun getWeatherConditions(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<String> = hourlyForecastRemoteDataSource.getWeatherConditions(day, grouped)

    override fun getWeatherDescriptions(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<String> = hourlyForecastRemoteDataSource.getWeatherDescriptions(day, grouped)

    override fun getWindSpeeds(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Double> = hourlyForecastRemoteDataSource.getWindSpeeds(day, grouped)

    override fun getWindDirections(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Int> = hourlyForecastRemoteDataSource.getWindDirections(day, grouped)

    override fun getVisibility(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Int> = hourlyForecastRemoteDataSource.getVisibility(day, grouped)

    override fun getCloudCoverage(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Int> = hourlyForecastRemoteDataSource.getCloudCoverage(day, grouped)

    override fun getHourLabels(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>,
        timezoneOffsetSeconds: Int
    ): List<String> = hourlyForecastRemoteDataSource.getHourLabels(day, grouped, timezoneOffsetSeconds)

    override fun getNextDaysSummaries(
        grouped: Map<String, List<HourlyForecastItem>>,
        count: Int
    ): List<HourlyForecastItem> = hourlyForecastRemoteDataSource.getNextDaysSummaries(grouped, count)

    override fun getNextDaysSummariesAtNoon(grouped: Map<String, List<HourlyForecastItem>>): List<HourlyForecastItem> =
        hourlyForecastRemoteDataSource.getNextDaysSummariesAtNoon(grouped)

    // Local data source methods
    override suspend fun storeFavoriteLocation(location: FavoriteLocationEntity): Long {
        return try {
            localDataSource.insertFavoriteLocation(location)
        } catch (e: Exception) {
            Log.e("WeatherRepositoryImpl", "storeFavoriteLocation: ${e.message}", e)
            throw e
        }
    }

    override suspend fun storeCurrentWeather(weather: CurrentWeatherResponse) {
        try {
            localDataSource.insertCurrentWeather(weather)
        } catch (e: Exception) {
            Log.e("WeatherRepositoryImpl", "storeCurrentWeather: ${e.message}", e)
            throw e
        }
    }

    override suspend fun storeHourlyForecast(forecast: List<HourlyForecastItem>) {
        try {
            localDataSource.insertHourlyForecast(forecast)
        } catch (e: Exception) {
            Log.e("WeatherRepositoryImpl", "storeHourlyForecast: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteFavoriteLocation(location: FavoriteLocationEntity) {
        try {
            localDataSource.deleteFavoriteLocation(location)
        } catch (e: Exception) {
            Log.e("WeatherRepositoryImpl", "deleteFavoriteLocation: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getForecastBundleByCity(cityName: String): ForecastBundle? {
        return try {
            localDataSource.getForecastBundleByCity(cityName)
        } catch (e: Exception) {
            Log.e("WeatherRepositoryImpl", "getForecastBundleByCity: ${e.message}", e)
            null
        }
    }

    override suspend fun getAllForecastBundles(): List<ForecastBundle> {
        return try {
            localDataSource.getAllForecastBundles()
        } catch (e: Exception) {
            Log.e("WeatherRepositoryImpl", "getAllForecastBundles: ${e.message}", e)
            emptyList()
        }
    }
}