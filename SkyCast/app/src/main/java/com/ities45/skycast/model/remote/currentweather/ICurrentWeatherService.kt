package com.ities45.skycast.model.remote.currentweather

import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ICurrentWeatherService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("lang") language: String,
        @Query("units") units: String
    ): Response<CurrentWeatherResponse?>?
}