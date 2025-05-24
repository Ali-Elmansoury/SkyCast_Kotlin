package com.ities45.skycast.model.remote

import android.content.Context
import com.ities45.skycast.model.remote.currentweather.ICurrentWeatherService
import com.ities45.skycast.model.remote.hourlyforecast.IHourlyForecastService
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import kotlin.reflect.KClass

object RetrofitClient {
    // Define API base URLs as static strings
    private object ApiUrls {
        const val HOURLY_FORECAST = "https://pro.openweathermap.org/data/2.5/forecast/"
        const val CURRENT_WEATHER = "https://api.openweathermap.org/data/2.5/"
    }

    // Cache of Retrofit instances for different base URLs
    private val retrofitCache = mutableMapOf<String, Retrofit>()

    private fun createRetrofit(context: Context, baseUrl: String): Retrofit {
        val cacheSize = 10 * 1024 * 1024 // 10 MB cache
        val cache = Cache(
            context.cacheDir ?: File(context.cacheDir, "cache"),
            cacheSize.toLong()
        )

        val okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=${60 * 5}") // 5 minutes cache
                    .build()
            }
            .addInterceptor { chain ->
                var request = chain.request()
                if (!isNetworkAvailable(context)) {
                    request = request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=${60 * 60 * 24 * 4}") // 4 days stale
                        .build()
                }
                chain.proceed(request)
            }
            .addInterceptor(ApiKeyInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // Generic method to create service instances
    fun <T : Any> createService(context: Context, serviceClass: KClass<T>, apiType: String): T {
        val baseUrl = when (apiType) {
            "HOURLY_FORECAST" -> ApiUrls.HOURLY_FORECAST
            "CURRENT_WEATHER" -> ApiUrls.CURRENT_WEATHER
            else -> throw IllegalArgumentException("Unknown API type: $apiType")
        }

        val retrofit = retrofitCache.getOrPut(baseUrl) {
            createRetrofit(context, baseUrl)
        }

        return retrofit.create(serviceClass.java)
    }

    // Example usage for specific services
    fun getHourlyForecastService(context: Context): IHourlyForecastService {
        return createService(context, IHourlyForecastService::class, "HOURLY_FORECAST")
    }

     fun getCurrentWeatherService(context: Context): ICurrentWeatherService {
         return createService(context, ICurrentWeatherService::class, "CURRENT_WEATHER")
     }
}