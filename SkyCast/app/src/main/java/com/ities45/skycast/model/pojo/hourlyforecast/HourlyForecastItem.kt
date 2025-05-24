package com.ities45.skycast.model.pojo.hourlyforecast

import com.ities45.skycast.model.pojo.Clouds
import com.ities45.skycast.model.pojo.Weather
import com.ities45.skycast.model.pojo.Wind

data class HourlyForecastItem(
    val clouds: Clouds,
    val dt: Long,
    val dt_txt: String,
    val main: Main,
    val pop: Int,
    val sys: Sys,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind
)