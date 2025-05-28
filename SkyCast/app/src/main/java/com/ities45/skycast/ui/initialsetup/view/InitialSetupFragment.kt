package com.ities45.skycast.ui.initialsetup.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ities45.skycast.R
import com.ities45.skycast.databinding.FragmentInitialSetupBinding
import com.ities45.skycast.model.repository.settings.SettingsRepository
import com.ities45.skycast.ui.initialsetup.viewModel.InitialSetupViewModel
import com.ities45.skycast.ui.initialsetup.viewModel.InitialSetupViewModelFactory

class InitialSetupFragment : Fragment() {
    private lateinit var binding: FragmentInitialSetupBinding
    private val viewModel: InitialSetupViewModel by viewModels {
        val settingsRepo = SettingsRepository(requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE))
        InitialSetupViewModelFactory(settingsRepo)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInitialSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Skip setup if already completed
        if (!viewModel.isFirstRun()) {
            navigateToHome()
            return
        }

        // Setup switch mutual exclusion
        binding.gpsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.mapSwitch.isChecked = false
        }

        binding.mapSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.gpsSwitch.isChecked = false
        }

        // Set default states
        binding.gpsSwitch.isChecked = true
        binding.notificationsSwitch.isChecked = true

        binding.okButton.setOnClickListener {
            val locationMethod = if (binding.gpsSwitch.isChecked) "gps" else "map"

            val notificationsEnabled = binding.notificationsSwitch.isChecked

            // Save notification setting
            viewModel.saveSettings(
                locationMethod,
                notificationsEnabled
            )

            // Enable/disable notifications based on setting
            if (notificationsEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {

            }

            viewModel.saveSettings(
                locationMethod,
                binding.notificationsSwitch.isChecked
            )

            // Navigate based on selection
            if (locationMethod == "gps") {
                // For GPS, go directly to home
                findNavController().navigate(R.id.action_initialSetup_to_home)
            } else {
                // For map, go to map selection
                findNavController().navigate(R.id.action_initialSetup_to_map)
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_initialSetup_to_home)
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(requireContext(), "Notifications disabled", Toast.LENGTH_SHORT).show()            }
        }
}