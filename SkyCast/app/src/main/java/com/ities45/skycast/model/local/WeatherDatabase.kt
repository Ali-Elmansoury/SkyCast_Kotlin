package com.ities45.skycast.model.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ities45.skycast.model.local.converter.WeatherTypeConverters
import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.pojo.currentweather.CurrentWeatherResponse
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem

@Database(
    entities = [
        FavoriteLocationEntity::class,
        CurrentWeatherResponse::class,
        HourlyForecastItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(WeatherTypeConverters::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun getWeatherDao(): IWeatherDAO

    companion object {
        @Volatile
        private var instance: WeatherDatabase? = null
        @Synchronized
        fun getInstance(context: Context): WeatherDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_db"
                ).build()
            }
            return instance!!
        }
    }
}