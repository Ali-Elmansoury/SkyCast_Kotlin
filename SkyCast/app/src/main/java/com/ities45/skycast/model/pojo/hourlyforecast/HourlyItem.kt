package com.ities45.skycast.model.pojo.hourlyforecast

import java.util.UUID

data class HourlyItem (val temp: Double,
                       val hour: String,
                       val icon: String,
                       val uid: String = UUID.randomUUID().toString())