package com.ities45.skycast.ui.favorites.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ities45.skycast.R
import com.ities45.skycast.databinding.FragmentHomeBinding
import com.ities45.skycast.model.local.WeatherDatabase
import com.ities45.skycast.model.local.WeatherLocalDataSourceImpl
import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.local.model.ForecastBundle
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastResponse
import com.ities45.skycast.model.remote.RetrofitClient
import com.ities45.skycast.model.remote.currentweather.CurrentWeatherRemoteDataSourceImpl
import com.ities45.skycast.model.remote.hourlyforecast.HourlyForecastRemoteDataSourceImpl
import com.ities45.skycast.model.repository.settings.SettingsRepository
import com.ities45.skycast.model.repository.weather.WeatherRepositoryImpl
import com.ities45.skycast.navigation.SharedViewModel
import com.ities45.skycast.ui.home.view.DateUtils
import com.ities45.skycast.ui.home.view.HourlyForecastListAdapter
import com.ities45.skycast.ui.home.view.NextForecastListAdapter
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyItem
import com.ities45.skycast.ui.settings.viewModel.SettingsViewModel
import com.ities45.skycast.ui.settings.viewModel.SettingsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteDetailFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var hourlyForecastListAdapter: HourlyForecastListAdapter
    private lateinit var nextForecastListAdapter: NextForecastListAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var repository: WeatherRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = WeatherRepositoryImpl.getInstance(
            WeatherLocalDataSourceImpl(WeatherDatabase.getInstance(requireContext()).getWeatherDao()),
            CurrentWeatherRemoteDataSourceImpl(RetrofitClient.getCurrentWeatherService(requireContext())),
            HourlyForecastRemoteDataSourceImpl(RetrofitClient.getHourlyForecastService(requireContext()))
        )

        val settingsFactory = SettingsViewModelFactory(
            SettingsRepository(requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE))
        )
        settingsViewModel = viewModels<SettingsViewModel> { settingsFactory }.value

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
            reverseLayout = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        }
        binding.rvNextForecast.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }

        settingsViewModel.language.observe(viewLifecycleOwner) {
            updateLocaleDependentUI()
        }

        sharedViewModel.coordinates.observe(viewLifecycleOwner) { geoPoint ->
            sharedViewModel.placeName.observe(viewLifecycleOwner) { placeName ->
                fetchWeatherData(placeName, geoPoint.latitude, geoPoint.longitude)
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            sharedViewModel.coordinates.value?.let { geoPoint ->
                sharedViewModel.placeName.value?.let { placeName ->
                    fetchWeatherData(placeName, geoPoint.latitude, geoPoint.longitude)
                }
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun fetchWeatherData(cityName: String, lat: Double, lon: Double) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Step 1: Check if location exists, insert if not
                var forecastBundle = repository.getForecastBundleByCity(cityName)
                val locationId: Long = if (forecastBundle == null) {
                    val favorite = FavoriteLocationEntity(name = cityName, lat = lat, lon = lon)
                    repository.storeFavoriteLocation(favorite)
                } else {
                    forecastBundle.favorite.locationId.toLong()
                }

                // Fetch from network
                val language = settingsViewModel.language.value ?: "en"
                val units = "metric"
                val weatherResult = repository.fetchCurrentWeather(
                    latitude = lat.toString(),
                    longitude = lon.toString(),
                    language = language,
                    units = units
                )
                weatherResult.getOrNull()?.let { weather ->
                    // Step 2: Set locationOwnerId
                    val weatherWithLocation = weather.copy(locationOwnerId = locationId.toInt())
                    repository.storeCurrentWeather(weatherWithLocation)
                    forecastBundle = repository.getForecastBundleByCity(cityName)
                    forecastBundle?.let { updateUIWithForecastBundle(it) }
                }

                val forecastResult = repository.fetchHourlyForecast(
                    latitude = lat.toString(),
                    longitude = lon.toString(),
                    language = language,
                    units = units
                )
                forecastResult.getOrNull()?.let { forecast ->
                    // Step 3: Set locationOwnerId for each forecast item
                    val forecastItems = forecast.list.map { it.copy(locationOwnerId = locationId.toInt()) }
                    repository.storeHourlyForecast(forecastItems)
                }

                if (weatherResult.isFailure) {
                    Toast.makeText(requireContext(), "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUIWithForecastBundle(bundle: ForecastBundle) {
        binding.progressBar.visibility = View.GONE
        val current = bundle.currentWeather
        binding.tvLocation.text = current?.name
        Glide.with(binding.ivWeatherIcon.context)
            .load("https://openweathermap.org/img/wn/${current?.weather?.firstOrNull()?.icon ?: "01d"}@2x.png")
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivWeatherIcon)
        binding.tvCondition.text = current?.weather?.firstOrNull()?.description ?: "N/A"
        binding.tvHumidity.text = "Humidity | ${current?.main?.humidity}%"
        binding.tvPressure.text = "Pressure | ${current?.main?.pressure} hPa"
        binding.tvCloud.text = "Cloudiness | ${current?.clouds?.all}%"
        binding.tvVisibility.text = "Visibility | ${current?.visibility} meters"

        // FIX: Get timezone from current weather instead of hourly forecast
        val timezoneOffset = current?.timezone ?: 0

        val groupedForecast = repository.groupByDay(
            bundle.hourlyForecast,
            timezoneOffset
        )
        val today = groupedForecast.keys.firstOrNull()
        if (today != null) {
            val temperatures = repository.getTemperatures(today, groupedForecast)
            val hourLabels = repository.getHourLabels(today, groupedForecast, timezoneOffset)
            val icon = groupedForecast[today]?.firstOrNull()?.weather?.firstOrNull()?.icon ?: "01d"
            val displayItems = temperatures.mapIndexed { index, temp ->
                HourlyItem(
                    temp = temp,
                    hour = hourLabels.getOrNull(index) ?: "",
                    icon = icon
                )
            }
            binding.rvHourlyForecast.adapter = hourlyForecastListAdapter
            hourlyForecastListAdapter.submitList(displayItems)

            val nextDays = repository.getNextDaysSummariesAtNoon(groupedForecast)
            binding.rvNextForecast.adapter = nextForecastListAdapter
            nextForecastListAdapter.submitList(nextDays)
        }

        updateLocaleDependentUI()
    }

    private fun updateLocaleDependentUI() {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val tempUnit = prefs.getString("temperature", "celsius") ?: "celsius"
        val windUnit = prefs.getString("wind_speed", "meter_sec") ?: "meter_sec"

        CoroutineScope(Dispatchers.Main).launch {
            val cityName = sharedViewModel.placeName.value ?: return@launch
            val bundle = repository.getForecastBundleByCity(cityName) ?: return@launch
            val current = bundle.currentWeather
            binding.tvDate.text = "${getString(R.string.today)}, ${DateUtils.formatDate(current?.dt?.times(
                1000L
            ) ?: 0, "EEE MMM d")}"
            binding.tvShortDate.text = DateUtils.formatDate(current?.dt?.times(1000L) ?: 0, "EEE, MMM d")
            binding.tvTemperature.text = convertTemperatureFromCelsius(current?.main?.temp ?: 0.0, tempUnit)
            binding.tvWind.text = "${getString(R.string.wind_label)} | ${convertWindSpeed(current?.wind?.speed ?: 0.0, windUnit)}"
            binding.tvHumidity.text = "${getString(R.string.humidity_label)} | ${current?.main?.humidity}%"
            binding.tvPressure.text = "${getString(R.string.pressure_label)} | ${current?.main?.pressure} hPa"
            binding.tvCloud.text = "${getString(R.string.cloudiness_label)} | ${current?.clouds?.all}%"
            binding.tvVisibility.text = "${getString(R.string.visibility_label)} | ${current?.visibility} meters"
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