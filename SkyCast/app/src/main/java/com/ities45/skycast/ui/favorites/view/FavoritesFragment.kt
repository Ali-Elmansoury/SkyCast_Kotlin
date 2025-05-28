package com.ities45.skycast.ui.favorites.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ities45.skycast.R
import com.ities45.skycast.databinding.FragmentFavoritesBinding
import com.ities45.skycast.model.local.WeatherDatabase
import com.ities45.skycast.model.local.WeatherLocalDataSourceImpl
import com.ities45.skycast.model.local.entity.FavoriteLocationEntity
import com.ities45.skycast.model.remote.RetrofitClient
import com.ities45.skycast.model.remote.currentweather.CurrentWeatherRemoteDataSourceImpl
import com.ities45.skycast.model.remote.hourlyforecast.HourlyForecastRemoteDataSourceImpl
import com.ities45.skycast.model.repository.weather.WeatherRepositoryImpl
import com.ities45.skycast.navigation.SharedViewModel
import com.ities45.skycast.ui.favorites.viewModel.FavoritesViewModel
import com.ities45.skycast.ui.favorites.viewModel.FavoritesViewModelFactory
import org.osmdroid.util.GeoPoint

class FavoritesFragment : Fragment() {

    companion object {
        fun newInstance() = FavoritesFragment()
    }

    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                WeatherLocalDataSourceImpl(WeatherDatabase.getInstance(requireContext()).getWeatherDao()),
                CurrentWeatherRemoteDataSourceImpl(RetrofitClient.getCurrentWeatherService(requireContext())),
                HourlyForecastRemoteDataSourceImpl(RetrofitClient.getHourlyForecastService(requireContext()))
            )
        )
    }
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var adapter: FavoritesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FavoritesListAdapter(
            onItemClick = { favorite ->
                sharedViewModel.setCoordinates(favorite.lat, favorite.lon, favorite.name)
                findNavController().navigate(R.id.action_favoritesFragment_to_favoriteDetailFragment)
            },
            onDeleteClick = { favorite ->
                showDeleteConfirmationDialog(favorite)
            }
        )

        binding.rvFavorites.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FavoritesFragment.adapter
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_favoritesFragment_to_mapFragment)
        }

        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            adapter.submitList(favorites)

            if (favorites.isEmpty()) {
                binding.favIcon.visibility = View.VISIBLE
                binding.tvNoPlaces.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
            } else {
                binding.favIcon.visibility = View.GONE
                binding.tvNoPlaces.visibility = View.GONE
                binding.rvFavorites.visibility = View.VISIBLE
            }
        }

        sharedViewModel.coordinates.observe(viewLifecycleOwner) { geoPoint: GeoPoint ->
            sharedViewModel.placeName.observe(viewLifecycleOwner) { placeName ->
                viewModel.addFavorite(placeName, geoPoint.latitude, geoPoint.longitude)
            }
        }
    }

    private fun showDeleteConfirmationDialog(favorite: FavoriteLocationEntity) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Favorite")
            .setMessage("Are you sure you want to delete ${favorite.name} from favorites?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteFavorite(favorite)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}