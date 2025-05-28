package com.ities45.skycast.ui.alerts.view

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ities45.skycast.R
import com.ities45.skycast.databinding.FragmentAlertsBinding
import com.ities45.skycast.model.pojo.Alert
import com.ities45.skycast.ui.alertdialoge.view.AlertDialogFragment
import com.ities45.skycast.ui.alerts.receiver.AlarmReceiver
import com.ities45.skycast.ui.alerts.viewModel.AlertsViewModel
import com.ities45.skycast.ui.alerts.viewModel.AlertsViewModelFactory
import com.ities45.skycast.ui.alerts.worker.NotificationWorker
import java.util.Date
import java.util.concurrent.TimeUnit

class AlertsFragment : Fragment() {

    companion object {
        fun newInstance() = AlertsFragment()
    }

    private lateinit var binding: FragmentAlertsBinding
    private val viewModel: AlertsViewModel by viewModels{ AlertsViewModelFactory() }
    private lateinit var adapter: AlertsListAdapter

    // Permission launchers
    private val alarmPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Alarm permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private val overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request permissions
        requestPermissions()

        // Setup RecyclerView
        adapter = AlertsListAdapter{ alert ->
            // Handle delete action
            viewModel.removeAlert(alert)
            cancelAlarmAndNotification(alert)
        }

        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }

        binding.rvAlerts.adapter = adapter

        // Observe alerts and update UI
        viewModel.alerts.observe(viewLifecycleOwner) { alerts ->
            adapter.submitList(alerts)
            binding.layoutNoAlerts.visibility = if (alerts.isEmpty()) View.VISIBLE else View.GONE
        }

        // Setup FAB to show dialog
        binding.fabAdd.setOnClickListener {
            val dialogFragment = AlertDialogFragment.newInstance()
            dialogFragment.setOnSaveListener { fromTime, fromDate, toTime, toDate, isAlarmEnabled, isNotificationEnabled ->
                // Add alert to ViewModel
                viewModel.addAlert(fromTime, fromDate, toTime, toDate, isAlarmEnabled, isNotificationEnabled)

                // Set alarm if enabled
                val latestAlert = viewModel.alerts.value?.lastOrNull()
                if (isAlarmEnabled && latestAlert != null) {
                    setAlarm(latestAlert.id, fromTime)
                }

                if (isNotificationEnabled && latestAlert != null) {
                    scheduleNotification(latestAlert.id, toTime)
                }
            }
            dialogFragment.show(childFragmentManager, "AlertDialog")
        }
    }

    private fun requestPermissions() {
        // Request SCHEDULE_EXACT_ALARM permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                alarmPermissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM)
            }
        }

        // Request POST_NOTIFICATIONS permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Request SYSTEM_ALERT_WINDOW permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(requireContext())) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
                overlayPermissionLauncher.launch(intent)
            }
        }
    }

    private fun setAlarm(alertId: Int, triggerTime: Date) {
        val currentTime = System.currentTimeMillis()
        if (triggerTime.time < currentTime) {
            Toast.makeText(context, "Cannot set alarm for past time: ${triggerTime}", Toast.LENGTH_LONG).show()
            return
        }

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent("com.ities45.skycast.ALARM_TRIGGERED").apply {
            setClass(requireContext(), AlarmReceiver::class.java)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            alertId, // Use alert ID as request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            triggerTime.time,
            pendingIntent
        )
        Toast.makeText(context, "Alarm set for: ${triggerTime}", Toast.LENGTH_SHORT).show()
    }

    private fun scheduleNotification(alertId: Int, triggerTime: Date) {
        val currentTime = System.currentTimeMillis()
        val delay = triggerTime.time - currentTime

        if (delay > 0) {
            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("notification_$alertId") // Tag with alert ID
                .build()

            WorkManager.getInstance(requireContext()).enqueue(workRequest)
        } else {
            Toast.makeText(context, "Cannot schedule notification for past time: ${triggerTime}", Toast.LENGTH_LONG).show()
        }
    }

    private fun cancelAlarmAndNotification(alert: Alert) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent("com.ities45.skycast.ALARM_TRIGGERED").apply {
            setClass(requireContext(), AlarmReceiver::class.java)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            alert.id, // Use alert ID to cancel the correct alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        WorkManager.getInstance(requireContext()).cancelAllWorkByTag("notification_${alert.id}")
    }
}