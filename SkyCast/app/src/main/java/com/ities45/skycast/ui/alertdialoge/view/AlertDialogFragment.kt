package com.ities45.skycast.ui.alertdialoge.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.ities45.skycast.databinding.FragmentAlertDialogeBinding
import com.ities45.skycast.ui.alertdialoge.viewModel.AlertDialogViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class AlertDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentAlertDialogeBinding
    private lateinit var viewModel: AlertDialogViewModel
    private var onSaveListener: ((Date, Date, Date, Date, Boolean, Boolean) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentAlertDialogeBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(AlertDialogViewModel::class.java)

        // Setup dialog
        setupDialog()

        return dialog
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Observe LiveData using the Fragment's lifecycle (not viewLifecycleOwner)
        binding.let { binding ->
            viewModel.fromTime.observe(this) { binding.tvFromTime.text = it }
            viewModel.fromDate.observe(this) { binding.tvFromDate.text = it }
            viewModel.toTime.observe(this) { binding.tvToTime.text = it }
            viewModel.toDate.observe(this) { binding.tvToDate.text = it }
            viewModel.isAlarmEnabled.observe(this) { binding.switchAlarm.isChecked = it }
            viewModel.isNotificationEnabled.observe(this) { binding.switchNotification.isChecked = it }
        }
    }

    private fun setupDialog() {
        with(binding) {
//            viewModel.fromTime.observe(viewLifecycleOwner) { tvFromTime.text = it }
//            viewModel.fromDate.observe(viewLifecycleOwner) { tvFromDate.text = it }
//            viewModel.toTime.observe(viewLifecycleOwner) { tvToTime.text = it }
//            viewModel.toDate.observe(viewLifecycleOwner) { tvToDate.text = it }
//            viewModel.isAlarmEnabled.observe(viewLifecycleOwner) { switchAlarm.isChecked = it }
//            viewModel.isNotificationEnabled.observe(viewLifecycleOwner) { switchNotification.isChecked = it }

            val calendar = Calendar.getInstance()

            tvFromTime.setOnClickListener {
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                    val isPM = selectedHour >= 12
                    val hour12 = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                    val time = String.format("%d:%02d %s", hour12, selectedMinute, if (isPM) "PM" else "AM")
                    viewModel.fromTime.value = time
                }, hour, minute, false).show()
            }

            tvFromDate.setOnClickListener {
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                    val date = String.format("%d %s %d", selectedDay, SimpleDateFormat("MMM").format(
                        calendar.apply { set(selectedYear, selectedMonth, selectedDay) }.time), selectedYear)
                    viewModel.fromDate.value = date
                }, year, month, day).show()
            }

            tvToTime.setOnClickListener {
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                    val isPM = selectedHour >= 12
                    val hour12 = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                    val time = String.format("%d:%02d %s", hour12, selectedMinute, if (isPM) "PM" else "AM")
                    viewModel.toTime.value = time
                }, hour, minute, false).show()
            }

            tvToDate.setOnClickListener {
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                    val date = String.format("%d %s %d", selectedDay, SimpleDateFormat("MMM").format(
                        calendar.apply { set(selectedYear, selectedMonth, selectedDay) }.time), selectedYear)
                    viewModel.toDate.value = date
                }, year, month, day).show()
            }

            switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                viewModel.isAlarmEnabled.value = isChecked
            }
            switchNotification.setOnCheckedChangeListener { _, isChecked ->
                viewModel.isNotificationEnabled.value = isChecked
            }

            btnSave.setOnClickListener {
                val fromTime = viewModel.fromTime.value
                val fromDate = viewModel.fromDate.value
                val toTime = viewModel.toTime.value
                val toDate = viewModel.toDate.value

                // Validate that fields are not at default values or null
                when {
                    fromTime == "0:00" || fromDate == "DD MMM YYYY" -> {
                        Toast.makeText(context, "Please select From time and date", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    toTime == "0:00" || toDate == "DD MMM YYYY" -> {
                        Toast.makeText(context, "Please select To time and date", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }

                viewModel.saveTimeRange { fromTimeDate, fromDateDate, toTimeDate, toDateDate, isAlarmEnabled, isNotificationEnabled ->
                    onSaveListener?.invoke(fromTimeDate, fromDateDate, toTimeDate, toDateDate, isAlarmEnabled, isNotificationEnabled)
                    dismiss()
                }
            }
        }
    }

    fun setOnSaveListener(listener: (Date, Date, Date, Date, Boolean, Boolean) -> Unit) {
        onSaveListener = listener
    }

    companion object {
        fun newInstance() = AlertDialogFragment()
    }
}