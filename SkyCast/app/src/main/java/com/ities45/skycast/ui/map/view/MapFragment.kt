package com.ities45.skycast.ui.map.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ities45.skycast.R
import com.ities45.skycast.databinding.FragmentMapBinding
import com.ities45.skycast.model.remote.RetrofitClient
import com.ities45.skycast.model.remote.map.MapRemoteDataSourceImpl
import com.ities45.skycast.model.repository.map.MapRepositoryImpl
import com.ities45.skycast.navigation.SharedViewModel
import com.ities45.skycast.ui.map.viewModel.MapViewModel
import com.ities45.skycast.ui.map.viewModel.MapViewModelFactory
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.events.MapEventsReceiver

class MapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var searchEditText: EditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var vmFactory: MapViewModelFactory
    private lateinit var viewModel: MapViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vmFactory = MapViewModelFactory(MapRepositoryImpl.getInstance(MapRemoteDataSourceImpl(
            RetrofitClient.getMapService(requireContext()))))

        viewModel = ViewModelProvider(this, vmFactory).get(MapViewModel::class.java)

        Configuration.getInstance().userAgentValue = requireContext().packageName
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView
        searchEditText = binding.searchEditText
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        setupMap()
        setupSearch()
        setupObservers()
        //requestLocation()
    }

    private fun setupMap() {
        if (!isNetworkAvailable()) {
            Toast.makeText(requireContext(), "No internet connection. Map tiles may not load.", Toast.LENGTH_LONG).show()
        }
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(true)
        mapView.controller.setZoom(1.0)
        mapView.controller.setCenter(GeoPoint(30.0444, 31.2357)) // Default Cairo
        mapView.isTilesScaledToDpi = true
        mapView.setUseDataConnection(isNetworkAvailable())

        mapView.controller.setCenter(GeoPoint(0.0, 0.0))

        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    viewModel.setSelectedCoordinates(it)
                    addMarker(it)
                    sharedViewModel.setCoordinates(it.latitude, it.longitude, "Tapped Location")
                    navigateToFavoritesFragment()
                }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        })
        mapView.overlays.add(mapEventsOverlay)
        mapView.invalidate()
    }

    private fun setupSearch() {
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN)) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    Log.d("MapFragment", "Searching for: $query")
                    viewModel.searchPlace(query)
                } else {
                    Toast.makeText(requireContext(), "Enter a location to search", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupObservers() {
        viewModel.searchedPlace.observe(viewLifecycleOwner) { place ->
            if (place != null) {
                val lat = place.lat.toDoubleOrNull()
                val lon = place.lon.toDoubleOrNull()
                if (lat != null && lon != null) {
                    Log.d("MapFragment", "Search result: lat=${place.lat}, lon=${place.lon}, name=${place.display_name}")
                    val geoPoint = GeoPoint(lat, lon)
                    mapView.controller.setCenter(geoPoint)
                    addMarker(geoPoint)
                    sharedViewModel.setCoordinates(geoPoint.latitude, geoPoint.longitude, place.display_name)
                    navigateToFavoritesFragment()
                } else {
                    Log.e("MapFragment", "Invalid lat/lon: lat=${place.lat}, lon=${place.lon}")
                    Toast.makeText(requireContext(), "Invalid location data", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("MapFragment", "Search returned null place")
                Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.currentLocation.observe(viewLifecycleOwner) { location ->
            Log.d("MapFragment", "Current location: lat=${location.latitude}, lon=${location.longitude}")
            mapView.controller.setCenter(location)
            addMarker(location)
            sharedViewModel.setCoordinates(location.latitude, location.longitude, "Current Location")
            navigateToFavoritesFragment()
        }
    }

    private fun requestLocation() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissionsToRequest, 100)
        } else {
            //fetchLocation()
        }
    }

    private fun fetchLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("MapFragment", "Location permission not granted")
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    Log.d("MapFragment", "Fetched location: lat=${location.latitude}, lon=${location.longitude}")
                    viewModel.setCurrentLocation(location.latitude, location.longitude)
                } else {
                    Log.w("MapFragment", "Location is null")
                    Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("MapFragment", "Location fetch failed: ${e.message}")
                Toast.makeText(requireContext(), "Location fetch failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("MapFragment", "All permissions granted")
                fetchLocation()
            } else {
                Log.w("MapFragment", "Permissions denied: ${permissions.joinToString()}")
                Toast.makeText(requireContext(), "Required permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMarker(point: GeoPoint) {
        marker?.remove(mapView)
        marker = Marker(mapView)
        marker?.position = point
        marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    private fun navigateToFavoritesFragment() {
        findNavController().navigate(R.id.action_mapFragment_to_favoritesFragment)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}