package com.ities45.skycast.ui.alerts.view

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ities45.skycast.R
import com.ities45.skycast.databinding.ItemAlertBinding
import com.ities45.skycast.model.pojo.Alert
import java.text.SimpleDateFormat
import java.util.Locale

class AlertsListAdapter(private val onDeleteClick: (Alert) -> Unit) : ListAdapter<Alert, AlertsListAdapter.AlertViewHolder>(AlertDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlertViewHolder(private val binding: ItemAlertBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.US)
        private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

        fun bind(alert: Alert) {
            with(binding) {
                tvFromTime.text = timeFormat.format(alert.fromTime)
                tvFromDate.text = dateFormat.format(alert.fromDate)
                tvToTime.text = timeFormat.format(alert.toTime)
                tvToDate.text = dateFormat.format(alert.toDate)
                ivAlarm.visibility = if (alert.isAlarmEnabled) View.VISIBLE else View.GONE
                ivNotification.visibility = if (alert.isNotificationEnabled) View.VISIBLE else View.GONE
                ivMore.setOnClickListener { view ->
                    showPopupMenu(view, alert)
                }
            }
        }

        private fun showPopupMenu(view: View, alert: Alert) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.alert_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete -> {
                        onDeleteClick(alert)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    class AlertDiffCallback : DiffUtil.ItemCallback<Alert>() {
        override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem == newItem
        }
    }
}