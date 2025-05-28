package com.ities45.skycast.model.remote.hourlyforecast

import android.util.Log
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HourlyForecastRemoteDataSourceImpl(private val hourlyForecastApiService: IHourlyForecastService) : IHourlyForecastRemoteDataSource {
    override suspend fun getWeatherHourly96DataOverNetwork(
        latitude: String,
        longitude: String,
        language: String,
        units: String
    ): HourlyForecastResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = hourlyForecastApiService.getWeatherHourly96Data(latitude, longitude, language, units)
                if (response?.isSuccessful == true && response.body() != null) {
                    response.body()
                } else {
                    Log.e("HourlyForecastRemoteDataSourceImpl", "Failed to fetch hourly forecast: HTTP ${response?.code()}, message: ${response?.message()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("HourlyForecastRemoteDataSourceImpl", "getWeatherHourly96DataOverNetwork: ${e.message}", e)
                null
            }
        }
    }

    override fun groupByDay(
        hourlyList: List<HourlyForecastItem>,
        timezoneOffsetSeconds: Int
    ): Map<String, List<HourlyForecastItem>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return hourlyList.groupBy { forecast ->
            val date = Date((forecast.dt * 1000L) + (timezoneOffsetSeconds * 1000L))
            dateFormat.format(date)
        }
    }

    override fun getCityName(response: HourlyForecastResponse): String = response.city.name

    override fun getTimezoneOffsetSeconds(response: HourlyForecastResponse): Int = response.city.timezone

    override fun getSunrise(response: HourlyForecastResponse): String {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("GMT")
        val date = Date(response.city.sunrise * 1000L)
        val offsetMillis = getTimezoneOffsetSeconds(response) * 1000
        val adjustedDate = Date(date.time + offsetMillis)
        return format.format(adjustedDate)
    }

    override fun getSunset(response: HourlyForecastResponse): String {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("GMT")
        val date = Date(response.city.sunset * 1000L)
        val offsetMillis = response.city.timezone * 1000
        val adjustedDate = Date(date.time + offsetMillis)
        return format.format(adjustedDate)
    }

    override fun getTemperatures(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Double> = grouped[day]?.map { it.main.temp } ?: emptyList()

    override fun getFeelsLikeTemps(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Double> = grouped[day]?.map { it.main.feels_like } ?: emptyList()

    override fun getMinTemps(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Double> = grouped[day]?.map { it.main.temp_min } ?: emptyList()

    override fun getMaxTemps(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Double> = grouped[day]?.map { it.main.temp_max } ?: emptyList()

    override fun getPressures(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Int> = grouped[day]?.map { it.main.pressure } ?: emptyList()

    override fun getHumidity(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Int> = grouped[day]?.map { it.main.humidity } ?: emptyList()

    override fun getWeatherConditions(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<String> = grouped[day]?.mapNotNull { it.weather.firstOrNull()?.main } ?: emptyList()

    override fun getWeatherDescriptions(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<String> = grouped[day]?.mapNotNull { it.weather.firstOrNull()?.description } ?: emptyList()

    override fun getWindSpeeds(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Double> = grouped[day]?.map { it.wind.speed } ?: emptyList()

    override fun getWindDirections(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Int> = grouped[day]?.map { it.wind.deg } ?: emptyList()

    override fun getVisibility(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Int> = grouped[day]?.map { it.visibility } ?: emptyList()

    override fun getCloudCoverage(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>
    ): List<Int> = grouped[day]?.map { it.clouds.all } ?: emptyList()

    override fun getHourLabels(
        day: String,
        grouped: Map<String, List<HourlyForecastItem>>,
        timezoneOffsetSeconds: Int
    ): List<String> =
        grouped[day]?.map {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("GMT")
            val date = Date((it.dt * 1000L) + (timezoneOffsetSeconds * 1000L))
            format.format(date)
        } ?: emptyList()

    override fun getNextDaysSummaries(
        grouped: Map<String, List<HourlyForecastItem>>,
        count: Int
    ): List<HourlyForecastItem> = grouped.entries.drop(1).take(count).mapNotNull { it.value.getOrNull(4) }

    override fun getNextDaysSummariesAtNoon(grouped: Map<String, List<HourlyForecastItem>>): List<HourlyForecastItem> {
        return grouped.entries.drop(1)
            .mapNotNull { (_, forecasts) ->
                forecasts.firstOrNull { forecast ->
                    forecast.dt_txt.contains("12:00:00")
                }
            }
    }
}