package com.ities45.skycast.model.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem

data class ForecastBundle(
    @Embedded val favorite: FavoriteLocationEntity,

    @Relation(
        parentColumn = "locationId",
        entityColumn = "locationOwnerId"
    )
    val currentWeather: CurrentWeatherResponse?,

    @Relation(
        parentColumn = "locationId",
        entityColumn = "locationOwnerId"
    )
    val hourlyForecast: List<HourlyForecastItem>
)