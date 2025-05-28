package com.ities45.skycast.model.remote.currentweather

import android.util.Log
import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CurrentWeatherRemoteDataSourceImpl(private val currentWeatherApiService: ICurrentWeatherService) : ICurrentWeatherRemoteDataSource {
    override suspend fun getCurrentWeatherOverNetwork(
        latitude: String,
        longitude: String,
        language: String,
        units: String
    ): CurrentWeatherResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = currentWeatherApiService.getCurrentWeather(latitude, longitude, language, units)
                if (response?.isSuccessful == true && response.body() != null) {
                    response.body()
                } else {
                    Log.e("CurrentWeatherRemoteDataSourceImpl", "Failed to fetch weather: HTTP ${response?.code()}, message: ${response?.message()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("CurrentWeatherRemoteDataSourceImpl", "getCurrentWeatherOverNetwork: ${e.message}", e)
                null
            }
        }
    }

    override fun getLatitude(current: CurrentWeatherResponse): Double = current.coord.lat

    override fun getLongitude(current: CurrentWeatherResponse): Double = current.coord.lon

    override fun getCityName(current: CurrentWeatherResponse): String = current.name

    override fun getCountryCode(current: CurrentWeatherResponse): String = current.sys.country

    override fun getTimezoneOffsetSeconds(current: CurrentWeatherResponse): Int = current.timezone

    override fun getTimestamp(current: CurrentWeatherResponse): String =
        formatUnixTimeWithOffset(current.dt, getTimezoneOffsetSeconds(current), "yyyy-MM-dd hh:mm a")

    override fun getSunrise(current: CurrentWeatherResponse): String =
        formatUnixTimeWithOffset(current.sys.sunrise, getTimezoneOffsetSeconds(current))

    override fun getSunset(current: CurrentWeatherResponse): String =
        formatUnixTimeWithOffset(current.sys.sunset, getTimezoneOffsetSeconds(current))

    override fun getTemperature(current: CurrentWeatherResponse): Double = current.main.temp

    override fun getFeelsLikeTemperature(current: CurrentWeatherResponse): Double = current.main.feels_like

    override fun getMinTemperature(current: CurrentWeatherResponse): Double = current.main.temp_min

    override fun getMaxTemperature(current: CurrentWeatherResponse): Double = current.main.temp_max

    override fun getPressure(current: CurrentWeatherResponse): Int = current.main.pressure

    override fun getHumidity(current: CurrentWeatherResponse): Int = current.main.humidity

    override fun getSeaLevel(current: CurrentWeatherResponse): Int? = current.main.sea_level

    override fun getGroundLevel(current: CurrentWeatherResponse): Int? = current.main.grnd_level

    override fun getWeatherMain(current: CurrentWeatherResponse): String? =
        current.weather.firstOrNull()?.main

    override fun getWeatherDescription(current: CurrentWeatherResponse): String? =
        current.weather.firstOrNull()?.description

    override fun getWeatherIcon(current: CurrentWeatherResponse): String? =
        current.weather.firstOrNull()?.icon

    override fun getWindSpeed(current: CurrentWeatherResponse): Double = current.wind.speed

    override fun getWindDirection(current: CurrentWeatherResponse): Int = current.wind.deg

    override fun getWindGust(current: CurrentWeatherResponse): Double? = current.wind.gust

    override fun getVisibility(current: CurrentWeatherResponse): Int = current.visibility

    override fun getCloudCoverage(current: CurrentWeatherResponse): Int = current.clouds.all

    fun formatUnixTimeWithOffset(
        unixTimeSeconds: Int,
        timezoneOffsetSeconds: Int,
        pattern: String = "hh:mm a"
    ): String {
        val date = Date((unixTimeSeconds + timezoneOffsetSeconds).toLong() * 1000L)
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("GMT")
        return format.format(date)
    }
}