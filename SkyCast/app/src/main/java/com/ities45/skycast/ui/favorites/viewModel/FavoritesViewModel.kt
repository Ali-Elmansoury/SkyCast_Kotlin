package com.ities45.skycast.ui.favorites.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.repository.weather.IWeatherRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: IWeatherRepository) : ViewModel() {
    private val _favorites = MutableLiveData<List<FavoriteLocationEntity>>()
    val favorites: LiveData<List<FavoriteLocationEntity>> get() = _favorites

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                val bundles: List<com.ities45.skycast.model.local.model.ForecastBundle> = repository.getAllForecastBundles()
                val favoriteLocations = bundles.map { bundle ->
                    FavoriteLocationEntity(
                        locationId = bundle.favorite.locationId,
                        name = bundle.favorite.name,
                        lat = bundle.favorite.lat,
                        lon = bundle.favorite.lon
                    )
                }
                _favorites.postValue(favoriteLocations)
            } catch (e: Exception) {
                // Handle error (e.g., empty list or notify UI)
                _favorites.postValue(emptyList())
            }
        }
    }

    fun addFavorite(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val favorite = FavoriteLocationEntity(name = name, lat = lat, lon = lon)
                repository.storeFavoriteLocation(favorite)
                // Fetch and store weather data for the new favorite
                fetchWeatherForFavorite(favorite)
                loadFavorites()
            } catch (e: Exception) {
                // Handle error (e.g., show a toast in the UI)
            }
        }
    }

    fun deleteFavorite(favorite: FavoriteLocationEntity) {
        viewModelScope.launch {
            try {
                repository.deleteFavoriteLocation(favorite)
                loadFavorites()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private suspend fun fetchWeatherForFavorite(favorite: FavoriteLocationEntity) {
        try {
            // Step 1: Insert favorite location and get locationId
            val locationId = repository.storeFavoriteLocation(favorite).toInt()

            val language = "en"
            val units = "metric"
            val weatherResult = repository.fetchCurrentWeather(
                latitude = favorite.lat.toString(),
                longitude = favorite.lon.toString(),
                language = language,
                units = units
            )
            weatherResult.getOrNull()?.let { weather ->
                // Step 2: Set locationOwnerId
                val weatherWithLocation = weather.copy(locationOwnerId = locationId.toInt())
                repository.storeCurrentWeather(weatherWithLocation)
            }

            val forecastResult = repository.fetchHourlyForecast(
                latitude = favorite.lat.toString(),
                longitude = favorite.lon.toString(),
                language = language,
                units = units
            )
            forecastResult.getOrNull()?.let { forecast ->
                // Step 3: Set locationOwnerId for each forecast item
                val forecastItems = forecast.list.map { it.copy(locationOwnerId = locationId.toInt()) }
                repository.storeHourlyForecast(forecastItems)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}