package com.ities45.skycast.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint

class SharedViewModel : ViewModel() {
    private val _coordinates = MutableLiveData<GeoPoint>()
    val coordinates: LiveData<GeoPoint> get() = _coordinates

    private val _placeName = MutableLiveData<String>()
    val placeName: LiveData<String> get() = _placeName

    fun setCoordinates(latitude: Double, longitude: Double, placeName: String = "Unknown") {
        _coordinates.postValue(GeoPoint(latitude, longitude))
        _placeName.postValue(placeName)
    }
}