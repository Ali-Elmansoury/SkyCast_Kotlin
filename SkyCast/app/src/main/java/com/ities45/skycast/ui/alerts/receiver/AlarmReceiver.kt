package com.ities45.skycast.ui.alerts.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import com.ities45.skycast.R

class AlarmReceiver : BroadcastReceiver() {

    private var ringtone: Ringtone? = null

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_LONG).show()

        // Play alarm sound in a loop
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(context, alarmUri)
        ringtone?.isLooping = true // Set sound to loop
        ringtone?.play()

        // Show overlay UI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            return
        }

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
            gravity = Gravity.CENTER
        }

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayView, params)

        // Set up the "Turn Off" button
        val turnOffButton = overlayView.findViewById<Button>(R.id.btnTurnOffAlarm)
        turnOffButton.setOnClickListener {
            ringtone?.stop()
            windowManager.removeView(overlayView)
        }
    }
}