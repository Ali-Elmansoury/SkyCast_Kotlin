package com.ities45.skycast.ui.home.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ities45.skycast.databinding.FragmentHomeBinding
import com.ities45.skycast.model.local.WeatherDatabase
import com.ities45.skycast.model.local.WeatherLocalDataSourceImpl
import com.ities45.skycast.model.remote.RetrofitClient
import com.ities45.skycast.model.remote.currentweather.CurrentWeatherRemoteDataSourceImpl
import com.ities45.skycast.model.remote.hourlyforecast.HourlyForecastRemoteDataSourceImpl
import com.ities45.skycast.model.repository.weather.WeatherRepositoryImpl
import com.ities45.skycast.ui.home.viewModel.HomeViewModel
import com.ities45.skycast.ui.home.viewModel.HomeViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var hourlyForecastListAdapter: HourlyForecastListAdapter
    lateinit var nextForecastListAdapter: NextForecastListAdapter
    lateinit var vmFactory: HomeViewModelFactory
    lateinit var viewModel: HomeViewModel

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vmFactory = HomeViewModelFactory(WeatherRepositoryImpl.getInstance(
            WeatherLocalDataSourceImpl(WeatherDatabase.getInstance(requireContext()).getWeatherDao()),
            CurrentWeatherRemoteDataSourceImpl(RetrofitClient.getCurrentWeatherService(requireContext())),
            HourlyForecastRemoteDataSourceImpl(RetrofitClient.getHourlyForecastService(requireContext()))
        ))

        viewModel = ViewModelProvider(this, vmFactory).get(HomeViewModel::class.java)

        hourlyForecastListAdapter = HourlyForecastListAdapter()
        nextForecastListAdapter = NextForecastListAdapter()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvHourlyForecast.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        binding.rvNextForecast.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }

        viewModel.onlineHourlyForecastList.observe(requireActivity()) { hours ->
            binding.rvHourlyForecast.adapter = hourlyForecastListAdapter
            hourlyForecastListAdapter.submitList(hours)
        }

        viewModel.onlineNext4Days.observe(requireActivity()) { days ->
            binding.rvNextForecast.adapter = nextForecastListAdapter
            nextForecastListAdapter.submitList(days)
        }

        viewModel.onlineCurrentForecast.observe(requireActivity()) { current ->
            binding.tvLocation.text = current.name
            Glide.with(binding.ivWeatherIcon.context).load("https://openweathermap.org/img/wn/" + current.weather.firstOrNull()?.icon + "@2x.png").diskCacheStrategy(
                DiskCacheStrategy.ALL).into(binding.ivWeatherIcon)
            binding.tvDate.text = "Today, " + SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(Date((current.dt * 1000L)))
            binding.tvTemperature.text = current.main.temp.toInt().toString() + "°"
            binding.tvCondition.text = current.weather.firstOrNull()?.description
            binding.tvWind.text = "Wind | " + current.wind.speed.toString() + " m/s"
            binding.tvHumidity.text = "Humidity | " + current.main.humidity.toString() + "%"
            binding.tvPressure.text = "Pressure | " + current.main.pressure.toString() + " hPa"
            binding.tvCloud.text = "Cloudiness | " + current.clouds.all + "%"
            binding.tvVisibility.text = "Visibility | " + current.visibility.toString() + " meters"
            binding.tvShortDate.text = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date((current.dt * 1000L)))
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchCurrentWeather()
            viewModel.fetchHourlyForecast()

            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}