package com.ities45.skycast.model.pojo.currentweather

import com.ities45.skycast.model.pojo.Clouds
import com.ities45.skycast.model.pojo.Coord
import com.ities45.skycast.model.pojo.Weather
import com.ities45.skycast.model.pojo.Wind

data class CurrentWeatherResponse(
    val base: String,
    val clouds: Clouds,
    val cod: Int,
    val coord: Coord,
    val dt: Int,
    val id: Int,
    val main: Main,
    val name: String,
    val sys: Sys,
    val timezone: Int,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind
)