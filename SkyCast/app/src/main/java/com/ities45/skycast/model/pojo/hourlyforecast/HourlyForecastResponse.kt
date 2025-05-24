package com.ities45.skycast.model.pojo.hourlyforecast

data class HourlyForecastResponse(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<HourlyForecastItem>,
    val message: Int
)