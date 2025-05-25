package com.ities45.skycast.model.pojo.hourlyforecast

import com.ities45.skycast.model.pojo.common.Coord

data class City(
    val coord: Coord,
    val country: String,
    val id: Int,
    val name: String,
    val population: Int,
    val sunrise: Int,
    val sunset: Int,
    val timezone: Int
)