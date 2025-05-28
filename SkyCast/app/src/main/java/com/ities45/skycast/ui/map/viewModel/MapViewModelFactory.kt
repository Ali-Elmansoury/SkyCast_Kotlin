package com.ities45.skycast.ui.map.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ities45.skycast.model.repository.map.IMapRepository

class MapViewModelFactory(private val repository: IMapRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MapViewModel::class.java)){
            MapViewModel(repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Class not Found")
        }
    }
}