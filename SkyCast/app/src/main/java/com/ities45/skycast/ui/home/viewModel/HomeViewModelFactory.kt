package com.ities45.skycast.ui.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ities45.skycast.model.repository.IWeatherRepository

class HomeViewModelFactory(private val repo: IWeatherRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(HomeViewModel::class.java)){
            HomeViewModel(repo) as T
        } else {
            throw IllegalArgumentException("ViewModel Class not Found")
        }
    }
}