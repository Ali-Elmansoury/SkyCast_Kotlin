package com.ities45.skycast.model.pojo.currentweather

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.pojo.common.Clouds
import com.ities45.skycast.model.pojo.common.Coord
import com.ities45.skycast.model.pojo.common.Weather
import com.ities45.skycast.model.pojo.common.Wind

@Entity(
    tableName = "current_weather",
    foreignKeys = [ForeignKey(
        entity = FavoriteLocationEntity::class,
        parentColumns = ["locationId"],
        childColumns = ["locationOwnerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class CurrentWeatherResponse(
    @PrimaryKey val id: Int,
    val locationOwnerId: Int = 0, // Added for foreign key
    val base: String,
    val clouds: Clouds,
    val cod: Int,
    val coord: Coord,
    val dt: Int,
    val main: Main,
    val name: String,
    val sys: Sys,
    val timezone: Int,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind
)