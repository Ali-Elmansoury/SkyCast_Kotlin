package com.ities45.skycast.ui.alerts.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ities45.skycast.R

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    companion object {
        const val CHANNEL_ID = "alert_notification_channel"
    }

    override fun doWork(): Result {
        showNotification()
        showOverlay()
        return Result.success()
    }

    private fun showNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0 (Oreo) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alert Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            .apply {
                setSound(Uri.parse("android.resource://com.ities45.skycast/${R.raw.skycast_noti}"), null)
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_without_bg) // Replace with your notification icon
            .setContentTitle("Weather Alert Notification")
            .setContentText("Your scheduled alert has triggered!")
            .setSound(Uri.parse("android.resource://com.ities45.skycast/${R.raw.skycast_noti}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(applicationContext)) {
            // Permission not granted, skip overlay
            return
        }

        val inflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val overlayView = inflater.inflate(R.layout.overlay_alarm, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 100 // Adjust the Y position as needed
        }

        val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayView, params)

        // Auto-dismiss after 5 seconds (adjust as needed)
        overlayView.postDelayed({
            windowManager.removeView(overlayView)
        }, 5000)
    }
}