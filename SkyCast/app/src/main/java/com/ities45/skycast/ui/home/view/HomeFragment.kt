package com.ities45.skycast.ui.home.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ities45.skycast.R
import com.ities45.skycast.databinding.FragmentHomeBinding
import com.ities45.skycast.model.local.WeatherDatabase
import com.ities45.skycast.model.local.WeatherLocalDataSourceImpl
import com.ities45.skycast.model.remote.RetrofitClient
import com.ities45.skycast.model.remote.currentweather.CurrentWeatherRemoteDataSourceImpl
import com.ities45.skycast.model.remote.hourlyforecast.HourlyForecastRemoteDataSourceImpl
import com.ities45.skycast.model.repository.settings.SettingsRepository
import com.ities45.skycast.model.repository.weather.WeatherRepositoryImpl
import com.ities45.skycast.navigation.SharedViewModel
import com.ities45.skycast.ui.home.viewModel.HomeViewModel
import com.ities45.skycast.ui.home.viewModel.HomeViewModelFactory
import com.ities45.skycast.ui.home.viewModel.WeatherUiState
import com.ities45.skycast.ui.settings.viewModel.SettingsViewModel
import com.ities45.skycast.ui.settings.viewModel.SettingsViewModelFactory
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDate(timestamp: Long, pattern: String, locale: Locale = Locale.getDefault()): String {
        return SimpleDateFormat(pattern, locale).format(Date(timestamp))
    }
}

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var hourlyForecastListAdapter: HourlyForecastListAdapter
    lateinit var nextForecastListAdapter: NextForecastListAdapter
    lateinit var vmFactory: HomeViewModelFactory
    lateinit var viewModel: HomeViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vmFactory = HomeViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                WeatherLocalDataSourceImpl(WeatherDatabase.getInstance(requireContext()).getWeatherDao()),
                CurrentWeatherRemoteDataSourceImpl(RetrofitClient.getCurrentWeatherService(requireContext())),
                HourlyForecastRemoteDataSourceImpl(RetrofitClient.getHourlyForecastService(requireContext()))
            ), requireContext()
        )

        viewModel = ViewModelProvider(this, vmFactory).get(HomeViewModel::class.java)

        // Initialize SettingsViewModel with factory
        val settingsFactory = SettingsViewModelFactory(
            SettingsRepository(
                requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            )
        )
        settingsViewModel = ViewModelProvider(requireActivity(), settingsFactory).get(SettingsViewModel::class.java)

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

        // Setup RecyclerViews
        binding.rvHourlyForecast.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.HORIZONTAL
            reverseLayout = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        }
        binding.rvNextForecast.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }

        // Observe language changes
        settingsViewModel.language.observe(viewLifecycleOwner) {
            updateLocaleDependentUI()
        }

        // Get location method from preferences
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val locationMethod = prefs.getString("location", "gps")

        // Handle location based on method
        when (locationMethod) {
            "gps" -> requestLocation()
            "map" -> {
                val lat = prefs.getString("map_latitude", "0.0")?.toDoubleOrNull() ?: 0.0
                val lon = prefs.getString("map_longitude", "0.0")?.toDoubleOrNull() ?: 0.0
                viewModel.setCoordinates(lat, lon)
            }
            else -> viewModel.setCoordinates(30.0444, 31.2357) // Default to Cairo
        }

        // Observe SharedViewModel coordinates
        sharedViewModel.coordinates.observe(viewLifecycleOwner) { geoPoint: GeoPoint ->
            viewModel.setCoordinates(geoPoint.latitude, geoPoint.longitude)
        }

        // Observe forecast data
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
                        .load("https://openweathermap.org/img/wn/${current.weather.firstOrNull()?.icon ?: "01d"}@2x.png")
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivWeatherIcon)
                    binding.tvCondition.text = current.weather.firstOrNull()?.description ?: "N/A"
                    binding.tvHumidity.text = "Humidity | ${current.main.humidity}%"
                    binding.tvPressure.text = "Pressure | ${current.main.pressure} hPa"
                    binding.tvCloud.text = "Cloudiness | ${current.clouds.all}%"
                    binding.tvVisibility.text = "Visibility | ${current.visibility} meters"
                    updateLocaleDependentUI() // Update locale-dependent UI
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

    private fun updateLocaleDependentUI() {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val tempUnit = prefs.getString("temperature", "celsius") ?: "celsius"
        val windUnit = prefs.getString("wind_speed", "meter_sec") ?: "meter_sec"

        viewModel.onlineCurrentForecastState.value?.let { state ->
            if (state is WeatherUiState.Success) {
                val current = state.data
                binding.tvDate.text = "${getString(R.string.today)}, ${DateUtils.formatDate(current.dt * 1000L, "EEE MMM d")}"
                binding.tvShortDate.text = DateUtils.formatDate(current.dt * 1000L, "EEE, MMM d")
                binding.tvTemperature.text = convertTemperatureFromCelsius(current.main.temp, tempUnit)
                binding.tvWind.text = "${getString(R.string.wind_label)} | ${convertWindSpeed(current.wind.speed, windUnit)}"
                binding.tvHumidity.text = "${getString(R.string.humidity_label)} | ${current.main.humidity}%"
                binding.tvPressure.text = "${getString(R.string.pressure_label)} | ${current.main.pressure} hPa"
                binding.tvCloud.text = "${getString(R.string.cloudiness_label)} | ${current.clouds.all}%"
                binding.tvVisibility.text = "${getString(R.string.visibility_label)} | ${current.visibility} meters"
            }
        }
    }

    private fun requestLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.setCoordinates(location.latitude, location.longitude)
                    } else {
                        viewModel.setCoordinates(30.0444, 31.2357) // Default to Cairo
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeFragment", "Location fetch failed", e)
                    viewModel.setCoordinates(30.0444, 31.2357)
                }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocation()
        } else {
            viewModel.setCoordinates(30.0444, 31.2357)
        }
    }

    private fun convertTemperatureFromCelsius(celsius: Double, unit: String): String {
        return when (unit.lowercase()) {
            "kelvin" -> "${(celsius + 273.15).toInt()}K"
            "fahrenheit" -> "${((celsius * 9 / 5) + 32).toInt()}°F"
            else -> "${celsius.toInt()}°C"
        }
    }

    private fun convertWindSpeed(mps: Double, unit: String): String {
        return when (unit) {
            "mile_hour" -> String.format("%.1f mph", mps * 2.23694)
            else -> String.format("%.1f m/s", mps)
        }
    }
}