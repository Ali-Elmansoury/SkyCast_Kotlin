package com.ities45.skycast.ui.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ities45.skycast.databinding.ItemHourlyForecastBinding
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyItem

class HourlyForecastListAdapter : ListAdapter<HourlyItem, HourlyForecastListAdapter.HourlyForecastViewHolder>(HourlyWeatherDiffCallback()) {

    class HourlyForecastViewHolder(
        val binding: ItemHourlyForecastBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastViewHolder {
        val binding = ItemHourlyForecastBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HourlyForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        val weather = getItem(position)
        holder.binding.tvHourTime.text = weather.hour
        holder.binding.tvHourTemp.text = weather.temp.toInt().toString() + "°"
        Glide.with(holder.binding.ivHourIcon.context).load("https://openweathermap.org/img/wn/" + weather.icon + "@2x.png").diskCacheStrategy(
            DiskCacheStrategy.ALL).into(holder.binding.ivHourIcon)

    }

    class HourlyWeatherDiffCallback : DiffUtil.ItemCallback<HourlyItem>() {
        override fun areItemsTheSame(oldItem: HourlyItem, newItem: HourlyItem): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: HourlyItem, newItem: HourlyItem): Boolean {
            return oldItem == newItem
        }
    }
}