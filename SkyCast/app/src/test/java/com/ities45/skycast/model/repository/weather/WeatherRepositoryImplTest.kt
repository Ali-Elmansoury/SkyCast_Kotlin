package com.ities45.skycast.model.repository.weather

import com.ities45.skycast.model.local.IWeatherLocalDataSource
import com.ities45.skycast.model.pojo.common.Clouds
import com.ities45.skycast.model.pojo.common.Coord
import com.ities45.skycast.model.pojo.common.Weather
import com.ities45.skycast.model.pojo.common.Wind
import com.ities45.skycast.model.pojo.currentweather.*
import com.ities45.skycast.model.pojo.hourlyforecast.HourlyForecastItem
import com.ities45.skycast.model.remote.currentweather.ICurrentWeatherRemoteDataSource
import com.ities45.skycast.model.remote.hourlyforecast.IHourlyForecastRemoteDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeatherRepositoryImplTest {

    @Mock
    private lateinit var localDataSource: IWeatherLocalDataSource

    @Mock
    private lateinit var currentWeatherRemoteDataSource: ICurrentWeatherRemoteDataSource

    @Mock
    private lateinit var hourlyForecastRemoteDataSource: IHourlyForecastRemoteDataSource

    private lateinit var weatherRepository: WeatherRepositoryImpl

    // Fake data for current weather
    private val fakeCurrentWeatherResponse = CurrentWeatherResponse(
        coord = Coord(lat = 40.7128, lon = -74.0060),
        weather = listOf(Weather(id = 800, main = "Clear", description = "clear sky", icon = "01d")),
        main = Main(
            temp = 25.0,
            feels_like = 26.0,
            temp_min = 23.0,
            temp_max = 27.0,
            pressure = 1012,
            humidity = 60,
            sea_level = 1012,
            grnd_level = 1008
        ),
        wind = Wind(speed = 5.0, deg = 180, gust = 7.0),
        clouds = Clouds(all = 0),
        visibility = 10000,
        dt = 1634567890,
        sys = Sys(country = "US", sunrise = 1634547890, sunset = 1634591890),
        timezone = -14400,
        name = "New York",
        id = 123456,
        cod = 200,
        base = "stations"
    )

    // Fake data for hourly forecast
    private val fakeHourlyForecastItem = HourlyForecastItem(
        dt = 1634567890,
        main = com.ities45.skycast.model.pojo.hourlyforecast.Main(
            temp = 25.0,
            feels_like = 26.0,
            temp_min = 23.0,
            temp_max = 27.0,
            pressure = 1012,
            humidity = 60,
            grnd_level = 1003,
            sea_level = 1014,
            temp_kf = 0.30
        ),
        weather = listOf(Weather(id = 800, main = "Clear", description = "clear sky", icon = "01d")),
        clouds = Clouds(all = 0),
        wind = Wind(speed = 5.0, deg = 180, gust = 7.0),
        visibility = 10000,
        dt_txt = "2021-10-18 12:00:00",
        pop = 0.0,
        sys = com.ities45.skycast.model.pojo.hourlyforecast.Sys("")
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        weatherRepository = WeatherRepositoryImpl(
            localDataSource,
            currentWeatherRemoteDataSource,
            hourlyForecastRemoteDataSource
        )
    }

    @Test
    fun `fetchCurrentWeather returns success with valid data`() = runBlocking {
        // Arrange
        val latitude = "40.7128"
        val longitude = "-74.0060"
        val language = "en"
        val units = "metric"

        `when`(currentWeatherRemoteDataSource.getCurrentWeatherOverNetwork(latitude, longitude, language, units))
            .thenReturn(fakeCurrentWeatherResponse)

        // Act
        val result = weatherRepository.fetchCurrentWeather(latitude, longitude, language, units)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(fakeCurrentWeatherResponse, result.getOrNull())
        assertEquals("New York", result.getOrNull()?.name)
        assertEquals(25.0, result.getOrNull()?.main?.temp)
    }

    @Test
    fun `fetchCurrentWeather returns failure on null response`() = runBlocking {
        // Arrange
        val latitude = "40.7128"
        val longitude = "-74.0060"
        val language = "en"
        val units = "metric"

        `when`(currentWeatherRemoteDataSource.getCurrentWeatherOverNetwork(latitude, longitude, language, units))
            .thenReturn(null)

        // Act
        val result = weatherRepository.fetchCurrentWeather(latitude, longitude, language, units)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Failed to fetch current weather", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getTemperatures returns correct temperatures for given day`() {
        // Arrange
        val day = "2021-10-18"
        val groupedData = mapOf(
            day to listOf(fakeHourlyForecastItem, fakeHourlyForecastItem.copy(main = fakeHourlyForecastItem.main.copy(temp = 26.0)))
        )

        `when`(hourlyForecastRemoteDataSource.getTemperatures(day, groupedData))
            .thenReturn(listOf(25.0, 26.0))

        // Act
        val temperatures = weatherRepository.getTemperatures(day, groupedData)

        // Assert
        assertEquals(listOf(25.0, 26.0), temperatures)
        assertEquals(2, temperatures.size)
    }

    @Test
    fun `getTemperatures returns empty list for non-existent day`() {
        // Arrange
        val day = "2021-10-19"
        val groupedData = mapOf("2021-10-18" to listOf(fakeHourlyForecastItem))

        `when`(hourlyForecastRemoteDataSource.getTemperatures(day, groupedData))
            .thenReturn(emptyList())

        // Act
        val temperatures = weatherRepository.getTemperatures(day, groupedData)

        // Assert
        assertTrue(temperatures.isEmpty())
    }
}