package com.ities45.skycast.ui.settings.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ities45.skycast.R
import com.ities45.skycast.databinding.FragmentSettingsBinding
import com.ities45.skycast.model.repository.settings.SettingsRepository
import com.ities45.skycast.ui.settings.viewModel.SettingsViewModel
import com.ities45.skycast.ui.settings.viewModel.SettingsViewModelFactory
import com.ities45.skycast.model.repository.settings.SettingsRepository.Companion.KEY_LANGUAGE
import com.ities45.skycast.model.repository.settings.SettingsRepository.Companion.KEY_LOCATION
import com.ities45.skycast.model.repository.settings.SettingsRepository.Companion.KEY_NOTIFICATIONS
import com.ities45.skycast.model.repository.settings.SettingsRepository.Companion.KEY_TEMPERATURE
import com.ities45.skycast.model.repository.settings.SettingsRepository.Companion.KEY_WIND_SPEED

class SettingsFragment : Fragment() {

    lateinit var binding: FragmentSettingsBinding
    lateinit var vmFactory: SettingsViewModelFactory
    lateinit var viewModel: SettingsViewModel

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vmFactory = SettingsViewModelFactory(SettingsRepository(requireContext().getSharedPreferences("settings",
            Context.MODE_PRIVATE)))

        viewModel = ViewModelProvider(this, vmFactory).get(SettingsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupListeners()
        viewModel.loadSettings()
    }

    private fun observeViewModel() {
        viewModel.location.observe(viewLifecycleOwner) {
            binding.radioGroupLocation.check(
                if (it == "gps") R.id.radioGps else R.id.radioMap
            )
        }

        viewModel.language.observe(viewLifecycleOwner) {
            binding.radioGroupLanguage.check(
                if (it == "en") R.id.radioEnglish else R.id.radioArabic
            )
        }

        viewModel.temperature.observe(viewLifecycleOwner) {
            val id = when (it) {
                "celsius" -> R.id.radioCelsius
                "kelvin" -> R.id.radioKelvin
                "fahrenheit" -> R.id.radioFahrenheit
                else -> R.id.radioCelsius
            }
            binding.radioGroupTemperature.check(id)
        }

        viewModel.windSpeed.observe(viewLifecycleOwner) {
            binding.radioGroupWindSpeed.check(
                if (it == "meter_sec") R.id.radioMeterSec else R.id.radioMileHour
            )
        }

        viewModel.notifications.observe(viewLifecycleOwner) {
            binding.radioGroupNotifications.check(
                if (it == "enable") R.id.radioEnable else R.id.radioDisable
            )
        }
    }

    private fun setupListeners() {
        binding.radioGroupLocation.setOnCheckedChangeListener { _, checkedId ->
            val value = if (checkedId == R.id.radioGps) "gps" else "map"
            viewModel.updateSetting(KEY_LOCATION, value)
        }

        binding.radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            val value = if (checkedId == R.id.radioEnglish) "en" else "ar"
            viewModel.updateSetting(KEY_LANGUAGE, value)
        }

        binding.radioGroupTemperature.setOnCheckedChangeListener { _, checkedId ->
            val value = when (checkedId) {
                R.id.radioCelsius -> "celsius"
                R.id.radioKelvin -> "kelvin"
                R.id.radioFahrenheit -> "fahrenheit"
                else -> "celsius"
            }
            viewModel.updateSetting(KEY_TEMPERATURE, value)
        }

        binding.radioGroupWindSpeed.setOnCheckedChangeListener { _, checkedId ->
            val value = if (checkedId == R.id.radioMeterSec) "meter_sec" else "mile_hour"
            viewModel.updateSetting(KEY_WIND_SPEED, value)
        }

        binding.radioGroupNotifications.setOnCheckedChangeListener { _, checkedId ->
            val value = if (checkedId == R.id.radioEnable) "enable" else "disable"
            viewModel.updateSetting(KEY_NOTIFICATIONS, value)
        }
    }
}