package com.ities45.skycast.model.pojo

import java.util.Date

data class Alert(
    val id: Int, // Add unique ID for each alert
    val fromTime: Date,
    val fromDate: Date,
    val toTime: Date,
    val toDate: Date,
    val isAlarmEnabled: Boolean,
    val isNotificationEnabled: Boolean
)