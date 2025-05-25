package com.ities45.skycast.model.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ities45.skycast.model.pojo.common.Clouds
import com.ities45.skycast.model.pojo.common.Coord
import com.ities45.skycast.model.pojo.common.Weather
import com.ities45.skycast.model.pojo.common.Wind
import com.ities45.skycast.model.pojo.currentweather.Main
import com.ities45.skycast.model.pojo.currentweather.Sys
import com.ities45.skycast.model.pojo.hourlyforecast.Main as HMain
import com.ities45.skycast.model.pojo.hourlyforecast.Sys as HSys

class WeatherTypeConverters {

    private val gson = Gson()

    @TypeConverter fun fromCoord(value: Coord) = gson.toJson(value)
    @TypeConverter fun toCoord(value: String) = gson.fromJson(value, Coord::class.java)

    @TypeConverter fun fromClouds(value: Clouds) = gson.toJson(value)
    @TypeConverter fun toClouds(value: String) = gson.fromJson(value, Clouds::class.java)

    @TypeConverter fun fromWind(value: Wind) = gson.toJson(value)
    @TypeConverter fun toWind(value: String) = gson.fromJson(value, Wind::class.java)

    @TypeConverter fun fromWeatherList(value: List<Weather>) = gson.toJson(value)
    @TypeConverter fun toWeatherList(value: String): List<Weather> =
        gson.fromJson(value, object : TypeToken<List<Weather>>() {}.type)

    @TypeConverter fun fromCurrentMain(value: Main) = gson.toJson(value)
    @TypeConverter fun toCurrentMain(value: String) = gson.fromJson(value, Main::class.java)

    @TypeConverter fun fromCurrentSys(value: Sys) = gson.toJson(value)
    @TypeConverter fun toCurrentSys(value: String) = gson.fromJson(value, Sys::class.java)

    @TypeConverter fun fromForecastMain(value: HMain) = gson.toJson(value)
    @TypeConverter fun toForecastMain(value: String) = gson.fromJson(value, HMain::class.java)

    @TypeConverter fun fromForecastSys(value: HSys) = gson.toJson(value)
    @TypeConverter fun toForecastSys(value: String) = gson.fromJson(value, HSys::class.java)
}