package com.ities45.skycast.ui.map.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.ities45.skycast.model.pojo.Place
import com.ities45.skycast.model.repository.map.IMapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.osmdroid.util.GeoPoint
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(MockitoJUnitRunner::class)
class MapViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: IMapRepository

    @Mock
    private lateinit var selectedCoordinatesObserver: Observer<GeoPoint>

    @Mock
    private lateinit var currentLocationObserver: Observer<GeoPoint>

    @Mock
    private lateinit var searchedPlaceObserver: Observer<Place?>

    private lateinit var viewModel: MapViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Fake data
    private val fakePlace = Place(
        lat = "40.7128",
        lon = "-74.0060",
        display_name = "New York City",
    )
    private val fakeGeoPoint = GeoPoint(40.7128, -74.0060)
    private val currentLocationPoint = GeoPoint(37.7749, -122.4194)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MapViewModel(repository)

        // Register observers
        viewModel.selectedCoordinates.observeForever(selectedCoordinatesObserver)
        viewModel.currentLocation.observeForever(currentLocationObserver)
        viewModel.searchedPlace.observeForever(searchedPlaceObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        // Remove observers to prevent leaks
        viewModel.selectedCoordinates.removeObserver(selectedCoordinatesObserver)
        viewModel.currentLocation.removeObserver(currentLocationObserver)
        viewModel.searchedPlace.removeObserver(searchedPlaceObserver)
    }

    @Test
    fun `searchPlace updates LiveData when place is found`() = runTest {
        // Arrange
        val query = "New York"
        `when`(repository.searchPlace(query)).thenReturn(fakePlace)

        // Act
        viewModel.searchPlace(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        verify(searchedPlaceObserver).onChanged(fakePlace)
        verify(selectedCoordinatesObserver).onChanged(fakeGeoPoint)

        // Verify LiveData values directly
        assertEquals(fakePlace, viewModel.searchedPlace.value)
        assertEquals(fakeGeoPoint, viewModel.selectedCoordinates.value)
    }

    @Test
    fun `searchPlace updates LiveData when place is not found`() = runTest {
        // Arrange
        val query = "Invalid Location"
        `when`(repository.searchPlace(query)).thenReturn(null)

        // Act
        viewModel.searchPlace(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        verify(searchedPlaceObserver).onChanged(null)
        // Should not update coordinates when place is null
        assertNull(viewModel.selectedCoordinates.value)
    }

    @Test
    fun `setSelectedCoordinates updates LiveData`() {
        // Arrange
        val testPoint = GeoPoint(51.5074, -0.1278)

        // Act
        viewModel.setSelectedCoordinates(testPoint)

        // Assert
        verify(selectedCoordinatesObserver).onChanged(testPoint)
        assertEquals(testPoint, viewModel.selectedCoordinates.value)
    }

    @Test
    fun `setCurrentLocation updates LiveData`() {
        // Arrange
        val testPoint = GeoPoint(34.0522, -118.2437)

        // Act
        viewModel.setCurrentLocation(testPoint.latitude, testPoint.longitude)

        // Assert
        verify(currentLocationObserver).onChanged(testPoint)
        assertEquals(testPoint, viewModel.currentLocation.value)
    }

    @Test
    fun `coordinates update when place is found`() = runTest {
        // Arrange
        val query = "Paris"
        val parisPlace = Place(
            lat = "48.8566",
            lon = "2.3522",
            display_name = "Paris, France"
        )
        val parisGeoPoint = GeoPoint(48.8566, 2.3522)
        `when`(repository.searchPlace(query)).thenReturn(parisPlace)

        // Act
        viewModel.searchPlace(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(parisPlace, viewModel.searchedPlace.value)
        assertEquals(parisGeoPoint, viewModel.selectedCoordinates.value)
    }

    @Test
    fun `current location persists after place search`() = runTest {
        // Arrange
        viewModel.setCurrentLocation(currentLocationPoint.latitude, currentLocationPoint.longitude)
        `when`(repository.searchPlace("Tokyo")).thenReturn(
            Place(
                lat = "35.6762",
                lon = "139.6503",
                display_name = "Tokyo",
            )
        )

        // Act
        viewModel.searchPlace("Tokyo")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(currentLocationPoint, viewModel.currentLocation.value)
    }
}