package com.ities45.skycast.model.remote

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit
import com.ities45.skycast.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            throw IllegalStateException("OpenWeatherMap API key is missing. Please set OPENWEATHER_API_KEY in local.properties.")
        }
        val originalRequest = chain.request()
        val newUrl = originalRequest.url.newBuilder()
            .addQueryParameter("appid", apiKey)
            .build()
        val newRequest = originalRequest.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }

    private fun getApiKey(): String {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("OPENWEATHER_API_KEY", null) ?: run {
            val apiKey = BuildConfig.OPENWEATHER_API_KEY
            sharedPreferences.edit { putString("OPENWEATHER_API_KEY", apiKey) }
            apiKey
        }
    }
}