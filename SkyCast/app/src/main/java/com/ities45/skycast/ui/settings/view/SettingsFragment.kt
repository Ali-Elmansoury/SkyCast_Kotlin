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

    // Flag to prevent infinite loops when updating radio buttons programmatically
    private var isUpdatingRadioButtons = false

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vmFactory = SettingsViewModelFactory(
            SettingsRepository(
                requireContext().getSharedPreferences(
                    "settings",
                    Context.MODE_PRIVATE
                )
            )
        )

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
        viewModel.loadSettings()
        setupListeners()
    }

    private fun observeViewModel() {
        viewModel.location.observe(viewLifecycleOwner) {
            updateRadioGroup(
                binding.radioGroupLocation,
                if (it == "gps") R.id.radioGps else R.id.radioMap,
                locationCheckedChangeListener
            )
        }

        viewModel.language.observe(viewLifecycleOwner) { lang ->
            updateRadioGroup(
                binding.radioGroupLanguage,
                if (lang == "en") R.id.radioEnglish else R.id.radioArabic,
                languageCheckedChangeListener
            )
        }

        viewModel.temperature.observe(viewLifecycleOwner) {
            val id = when (it) {
                "celsius" -> R.id.radioCelsius
                "kelvin" -> R.id.radioKelvin
                "fahrenheit" -> R.id.radioFahrenheit
                else -> R.id.radioCelsius
            }
            updateRadioGroup(binding.radioGroupTemperature, id, temperatureCheckedChangeListener)
        }

        viewModel.windSpeed.observe(viewLifecycleOwner) {
            updateRadioGroup(
                binding.radioGroupWindSpeed,
                if (it == "meter_sec") R.id.radioMeterSec else R.id.radioMileHour,
                windSpeedCheckedChangeListener
            )
        }

        viewModel.notifications.observe(viewLifecycleOwner) {
            updateRadioGroup(
                binding.radioGroupNotifications,
                if (it == "enable") R.id.radioEnable else R.id.radioDisable,
                notificationsCheckedChangeListener
            )
        }
    }

    private fun updateRadioGroup(
        radioGroup: android.widget.RadioGroup,
        checkedId: Int,
        listener: android.widget.RadioGroup.OnCheckedChangeListener
    ) {
        isUpdatingRadioButtons = true
        radioGroup.setOnCheckedChangeListener(null)
        radioGroup.check(checkedId)
        radioGroup.setOnCheckedChangeListener(listener)
        isUpdatingRadioButtons = false
    }

    private fun setupListeners() {
        binding.radioGroupLocation.setOnCheckedChangeListener(locationCheckedChangeListener)
        binding.radioGroupLanguage.setOnCheckedChangeListener(languageCheckedChangeListener)
        binding.radioGroupTemperature.setOnCheckedChangeListener(temperatureCheckedChangeListener)
        binding.radioGroupWindSpeed.setOnCheckedChangeListener(windSpeedCheckedChangeListener)
        binding.radioGroupNotifications.setOnCheckedChangeListener(notificationsCheckedChangeListener)
    }

    private val locationCheckedChangeListener =
        android.widget.RadioGroup.OnCheckedChangeListener { _, checkedId ->
            if (isUpdatingRadioButtons) return@OnCheckedChangeListener
            val value = if (checkedId == R.id.radioGps) "gps" else "map"
            viewModel.updateSetting(KEY_LOCATION, value)
        }

    private val languageCheckedChangeListener =
        android.widget.RadioGroup.OnCheckedChangeListener { _, checkedId ->
            if (isUpdatingRadioButtons) return@OnCheckedChangeListener

            val newLang = if (checkedId == R.id.radioEnglish) "en" else "ar"

            // Only recreate if language actually changed
            if (viewModel.language.value != newLang) {
                viewModel.updateSetting(KEY_LANGUAGE, newLang)
                requireActivity().recreate()
            }
        }

    private val temperatureCheckedChangeListener =
        android.widget.RadioGroup.OnCheckedChangeListener { _, checkedId ->
            if (isUpdatingRadioButtons) return@OnCheckedChangeListener

            val value = when (checkedId) {
                R.id.radioCelsius -> "celsius"
                R.id.radioKelvin -> "kelvin"
                R.id.radioFahrenheit -> "fahrenheit"
                else -> "celsius"
            }
            viewModel.updateSetting(KEY_TEMPERATURE, value)
        }

    private val windSpeedCheckedChangeListener =
        android.widget.RadioGroup.OnCheckedChangeListener { _, checkedId ->
            if (isUpdatingRadioButtons) return@OnCheckedChangeListener

            val value = if (checkedId == R.id.radioMeterSec) "meter_sec" else "mile_hour"
            viewModel.updateSetting(KEY_WIND_SPEED, value)
        }

    private val notificationsCheckedChangeListener =
        android.widget.RadioGroup.OnCheckedChangeListener { _, checkedId ->
            if (isUpdatingRadioButtons) return@OnCheckedChangeListener

            val value = if (checkedId == R.id.radioEnable) "enable" else "disable"
            viewModel.updateSetting(KEY_NOTIFICATIONS, value)
        }
}


