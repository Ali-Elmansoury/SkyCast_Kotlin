package com.ities45.skycast.ui.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ities45.skycast.databinding.ItemForecastBinding
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NextForecastListAdapter : ListAdapter<HourlyForecastItem, NextForecastListAdapter.NextForecastViewHolder>(NextForecastDiffCallback()){

    class NextForecastViewHolder(val binding: ItemForecastBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NextForecastListAdapter.NextForecastViewHolder {
        val binding = ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NextForecastViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NextForecastListAdapter.NextForecastViewHolder,
        position: Int
    ) {
        val nextDay = getItem(position)
        val formattedDate = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            .format(Date(nextDay.dt * 1000))
        holder.binding.tvDate.text = formattedDate
        holder.binding.tvTemperature.text = "${nextDay.main.temp.toInt()}°"
        holder.binding.tvDescription.text = nextDay.weather.firstOrNull()?.description ?: ""
        val icon = nextDay.weather.firstOrNull()?.icon ?: "01d"
        Glide.with(holder.binding.ivIcon.context)
            .load("https://openweathermap.org/img/wn/$icon@2x.png")
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.binding.ivIcon)
    }

    class NextForecastDiffCallback: DiffUtil.ItemCallback<HourlyForecastItem>(){
        override fun areItemsTheSame(
            oldItem: HourlyForecastItem,
            newItem: HourlyForecastItem
        ): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(
            oldItem: HourlyForecastItem,
            newItem: HourlyForecastItem
        ): Boolean {
            return oldItem == newItem
        }

    }
}