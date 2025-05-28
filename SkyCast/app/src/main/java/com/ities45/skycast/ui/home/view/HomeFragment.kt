package com.ities45.skycast.ui.home.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.ities45.skycast.ui.home.viewModel.WeatherUiState
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

        viewModel.onlineHourlyForecastList.observe(viewLifecycleOwner) { hours ->
            binding.rvHourlyForecast.adapter = hourlyForecastListAdapter
            hourlyForecastListAdapter.submitList(hours)
        }

        viewModel.onlineNext4Days.observe(viewLifecycleOwner) { days ->
            binding.rvNextForecast.adapter = nextForecastListAdapter
            nextForecastListAdapter.submitList(days)
        }

        viewModel.onlineCurrentForecastState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WeatherUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvLocation.text = "Loading..."
                }
                is WeatherUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val current = state.data
                    binding.tvLocation.text = current.name
                    Glide.with(binding.ivWeatherIcon.context)
                        .load("https://openweathermap.org/img/wn/" + (current.weather.firstOrNull()?.icon ?: "01d") + "@2x.png")
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivWeatherIcon)
                    binding.tvDate.text = "Today, " + SimpleDateFormat("EEE MMM d", Locale.getDefault()).format(Date((current.dt * 1000L)))
                    binding.tvTemperature.text = "${current.main.temp.toInt()}°"
                    binding.tvCondition.text = current.weather.firstOrNull()?.description ?: "N/A"
                    binding.tvWind.text = "Wind | ${current.wind.speed} m/s"
                    binding.tvHumidity.text = "Humidity | ${current.main.humidity}%"
                    binding.tvPressure.text = "Pressure | ${current.main.pressure} hPa"
                    binding.tvCloud.text = "Cloudiness | ${current.clouds.all}%"
                    binding.tvVisibility.text = "Visibility | ${current.visibility} meters"
                    binding.tvShortDate.text = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date((current.dt * 1000L)))
                }
                is WeatherUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvLocation.text = "Error loading weather"
                    Toast.makeText(requireContext(), "Failed to load weather: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchCurrentWeather()
            viewModel.fetchHourlyForecast()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}