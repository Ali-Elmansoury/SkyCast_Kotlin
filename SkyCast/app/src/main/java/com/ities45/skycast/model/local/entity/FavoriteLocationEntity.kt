package com.ities45.skycast.model.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_location")
data class FavoriteLocationEntity(
    @PrimaryKey(autoGenerate = true) val locationId: Int = 0,
    val name: String,
    val lat: Double,
    val lon: Double
)