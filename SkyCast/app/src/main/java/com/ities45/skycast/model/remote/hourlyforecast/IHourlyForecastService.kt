package com.ities45.skycast.model.remote.hourlyforecast

import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface IHourlyForecastService {
    @GET("hourly")
    suspend fun getWeatherHourly96Data(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("lang") language: String,
        @Query("units") units: String
    ): Response<HourlyForecastResponse?>?
}