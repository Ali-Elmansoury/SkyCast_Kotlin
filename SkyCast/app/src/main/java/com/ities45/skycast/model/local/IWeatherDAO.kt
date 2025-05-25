package com.ities45.skycast.model.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.local.model.ForecastBundle
import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem

@Dao
interface IWeatherDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteLocation(location: FavoriteLocationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(weather: CurrentWeatherResponse)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecast(forecast: List<HourlyForecastItem>)

    @Delete
    suspend fun deleteFavoriteLocation(location: FavoriteLocationEntity)

    @Transaction
    @Query("SELECT * FROM favorite_location WHERE name = :name")
    suspend fun getForecastBundleByCity(name: String): ForecastBundle?

    @Transaction
    @Query("SELECT * FROM favorite_location")
    suspend fun getAllForecastBundles(): List<ForecastBundle>
}