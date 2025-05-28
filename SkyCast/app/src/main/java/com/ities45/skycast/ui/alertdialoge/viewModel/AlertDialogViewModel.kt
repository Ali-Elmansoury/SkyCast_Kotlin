package com.ities45.skycast.ui.alertdialoge.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlertDialogViewModel : ViewModel() {
    val fromTime = MutableLiveData<String>("0:00")
    val fromDate = MutableLiveData<String>("DD MMM YYYY")
    val toTime = MutableLiveData<String>("0:00")
    val toDate = MutableLiveData<String>("DD MMM YYYY")
    val isAlarmEnabled = MutableLiveData<Boolean>(false)
    val isNotificationEnabled = MutableLiveData<Boolean>(true)

    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.US)
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

    fun saveTimeRange(onSave: (Date, Date, Date, Date, Boolean, Boolean) -> Unit) {
        val fromTimeDate = timeFormat.parse(fromTime.value ?: "0:00") ?: Date()
        val fromDateDate = dateFormat.parse(fromDate.value ?: "DD MMM YYYY") ?: Date()
        val toTimeDate = timeFormat.parse(toTime.value ?: "0:00") ?: Date()
        val toDateDate = dateFormat.parse(toDate.value ?: "DD MMM YYYY") ?: Date()

        // Combine date and time into a single Date object
        val fromCalendar = Calendar.getInstance().apply {
            time = fromDateDate
            val timeCalendar = Calendar.getInstance().apply { time = fromTimeDate }
            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        }

        val toCalendar = Calendar.getInstance().apply {
            time = toDateDate
            val timeCalendar = Calendar.getInstance().apply { time = toTimeDate }
            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        }

        onSave(
            fromCalendar.time,
            fromDateDate,
            toCalendar.time,
            toDateDate,
            isAlarmEnabled.value ?: false,
            isNotificationEnabled.value ?: true
        )
    }
}