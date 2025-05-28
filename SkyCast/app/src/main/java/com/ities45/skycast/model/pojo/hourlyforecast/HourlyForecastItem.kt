package com.ities45.skycast.model.pojo.hourlyforecast

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.pojo.common.Clouds
import com.ities45.skycast.model.pojo.common.Weather
import com.ities45.skycast.model.pojo.common.Wind

@Entity(
    tableName = "hourly_forecast",
    foreignKeys = [ForeignKey(
        entity = FavoriteLocationEntity::class,
        parentColumns = ["locationId"],
        childColumns = ["locationOwnerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class HourlyForecastItem(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val locationOwnerId: Int = 0,
    val clouds: Clouds,
    val dt: Long,
    val dt_txt: String,
    val main: Main,
    val pop: Double,
    val sys: Sys,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind
)