package com.ities45.skycast.ui.map.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ities45.skycast.model.pojo.Place
import com.ities45.skycast.model.repository.map.IMapRepository
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MapViewModel(private val repository: IMapRepository) : ViewModel() {
    private val _selectedCoordinates = MutableLiveData<GeoPoint>()
    val selectedCoordinates: LiveData<GeoPoint> get() = _selectedCoordinates

    private val _currentLocation = MutableLiveData<GeoPoint>()
    val currentLocation: LiveData<GeoPoint> get() = _currentLocation

    private val _searchedPlace = MutableLiveData<Place?>()
    val searchedPlace: LiveData<Place?> get() = _searchedPlace

    fun searchPlace(query: String) {
        viewModelScope.launch {
            val place = repository.searchPlace(query)
            _searchedPlace.postValue(place)
            place?.let {
                _selectedCoordinates.postValue(GeoPoint(it.lat.toDouble(), it.lon.toDouble()))
            }
        }
    }

    fun setSelectedCoordinates(point: GeoPoint) {
        _selectedCoordinates.postValue(point)
    }

    fun setCurrentLocation(latitude: Double, longitude: Double) {
        _currentLocation.postValue(GeoPoint(latitude, longitude))
    }
}