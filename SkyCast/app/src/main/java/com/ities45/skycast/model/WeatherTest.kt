package com.ities45.skycast.model

import android.content.Context
import android.util.Log
import com.ities45.skycast.model.local.WeatherDatabase
import com.ities45.skycast.model.local.IWeatherLocalDataSource
import com.ities45.skycast.model.local.WeatherLocalDataSourceImpl
import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.remote.currentweather.CurrentWeatherRemoteDataSourceImpl
import com.ities45.skycast.model.remote.currentweather.ICurrentWeatherRemoteDataSource
import com.ities45.skycast.model.remote.currentweather.ICurrentWeatherService
import com.ities45.skycast.model.remote.hourlyforecast.HourlyForecastRemoteDataSourceImpl
import com.ities45.skycast.model.remote.hourlyforecast.IHourlyForecastRemoteDataSource
import com.ities45.skycast.model.remote.hourlyforecast.IHourlyForecastService
import com.ities45.skycast.model.repository.weather.IWeatherRepository
import com.ities45.skycast.model.repository.weather.WeatherRepositoryImpl
import kotlinx.coroutines.runBlocking

fun testWeatherDataFlow(
    context: Context,
    currentWeatherApiService: ICurrentWeatherService,
    hourlyForecastApiService: IHourlyForecastService
) {
    runBlocking {
        // Initialize dependencies
        val database = WeatherDatabase.getInstance(context)
        val weatherDao = database.getWeatherDao()
        val localDataSource: IWeatherLocalDataSource = WeatherLocalDataSourceImpl(weatherDao)
        val currentWeatherRemoteDataSource: ICurrentWeatherRemoteDataSource =
            CurrentWeatherRemoteDataSourceImpl(currentWeatherApiService)
        val hourlyForecastRemoteDataSource: IHourlyForecastRemoteDataSource =
            HourlyForecastRemoteDataSourceImpl(hourlyForecastApiService)
        val repository: IWeatherRepository = WeatherRepositoryImpl(
            localDataSource,
            currentWeatherRemoteDataSource,
            hourlyForecastRemoteDataSource
        )

        // Test parameters (e.g., New York City from map or autocomplete search)
        val latitude = "30.07111902938482"
        val longitude = "31.021081747751275"
        val language = "en"
        val units = "metric"
        //val cityName = "New York"

        // Step 1: Fetch current weather
        val currentWeatherResult = repository.fetchCurrentWeather(latitude, longitude, language, units)
        if (currentWeatherResult.isFailure) {
            Log.e("WeatherTest", "Failed to fetch current weather: ${currentWeatherResult.exceptionOrNull()?.message}")
            return@runBlocking
        }
        val currentWeather = currentWeatherResult.getOrNull() ?: return@runBlocking

        // Step 2: Fetch hourly forecast
        val hourlyForecastResult = repository.fetchHourlyForecast(latitude, longitude, language, units)
        if (hourlyForecastResult.isFailure) {
            Log.e("WeatherTest", "Failed to fetch hourly forecast: ${hourlyForecastResult.exceptionOrNull()?.message}")
            return@runBlocking
        }
        val hourlyForecast = hourlyForecastResult.getOrNull() ?: return@runBlocking

        // Step 3: Store favorite location
        val location = FavoriteLocationEntity(
            name = repository.getCityName(currentWeather),
            lat = repository.getLatitude(currentWeather),
            lon = repository.getLongitude(currentWeather)
        )
        val locationId = repository.storeFavoriteLocation(location)
        Log.i("WeatherTest", "Stored favorite location with ID: $locationId")

        // Step 4: Store current weather
        val currentWeatherWithLocation = currentWeather.copy(locationOwnerId = locationId.toInt())
        repository.storeCurrentWeather(currentWeatherWithLocation)
        Log.i("WeatherTest", "Stored current weather for ${currentWeatherWithLocation.name}")

        // Step 5: Store hourly forecast
        val hourlyForecastItems = hourlyForecast.list.map { it.copy(locationOwnerId = locationId.toInt()) }
        repository.storeHourlyForecast(hourlyForecastItems)
        Log.i("WeatherTest", "Stored ${hourlyForecastItems.size} hourly forecast entries")

        // Step 6: Retrieve and log data

        val cityName = repository.getCityName(currentWeather)

        val forecastBundle = repository.getForecastBundleByCity(cityName)
        if (forecastBundle != null) {
            Log.i("WeatherTest", "Forecast Bundle for ${forecastBundle.favorite.name}:")
            Log.i("WeatherTest", "Latitude: ${forecastBundle.favorite.lat}, Longitude: ${forecastBundle.favorite.lon}")

            forecastBundle.currentWeather?.let { current ->
                Log.i("WeatherTest", "Current Weather:")
                Log.i("WeatherTest", "Temperature: ${current.main.temp}°C")
                Log.i("WeatherTest", "Weather: ${current.weather.firstOrNull()?.main}")
                Log.i("WeatherTest", "Description: ${current.weather.firstOrNull()?.description}")
                Log.i("WeatherTest", "Visibility: ${current.visibility}m")
            }

            forecastBundle.hourlyForecast.forEachIndexed { index, hourly ->
                Log.i("WeatherTest", "Hourly Forecast #$index (${hourly.dt_txt}):")
                Log.i("WeatherTest", "Temperature: ${hourly.main.temp}°C")
                Log.i("WeatherTest", "Weather: ${hourly.weather.firstOrNull()?.main}")
                Log.i("WeatherTest", "Visibility: ${hourly.visibility}m")
                Log.i("WeatherTest", "Probability of Precipitation: ${hourly.pop}%")
            }

            // Step 7: Test additional repository methods (e.g., for UI display)
            val groupedForecast = repository.groupByDay(
                hourlyForecast.list,
                repository.getHourlyTimezoneOffsetSeconds(hourlyForecast)
            )
            val today = groupedForecast.keys.firstOrNull()
            if (today != null) {
                Log.i("WeatherTest", "Today's Hourly Temperatures: ${repository.getTemperatures(today, groupedForecast)}")
                Log.i("WeatherTest", "Today's Hour Labels: ${repository.getHourLabels(today, groupedForecast, repository.getHourlyTimezoneOffsetSeconds(hourlyForecast))}")
            }

            val nextDaysSummaries = repository.getNextDaysSummariesAtNoon(groupedForecast)
            nextDaysSummaries.forEachIndexed { index, summary ->
                Log.i("WeatherTest", "Day ${index + 1} Summary at Noon: Temperature ${summary.main.temp}°C, Weather: ${summary.weather.firstOrNull()?.main}, POP: ${summary.pop}%")
            }
        } else {
            Log.e("WeatherTest", "No forecast bundle found for $cityName")
        }

        // Step 8: Test retrieving all favorite locations
        val allBundles = repository.getAllForecastBundles()
        Log.i("WeatherTest", "All Favorite Locations (${allBundles.size}):")
        allBundles.forEach { bundle ->
            Log.i("WeatherTest", "Location: ${bundle.favorite.name}, Lat: ${bundle.favorite.lat}, Lon: ${bundle.favorite.lon}")
        }
    }
}