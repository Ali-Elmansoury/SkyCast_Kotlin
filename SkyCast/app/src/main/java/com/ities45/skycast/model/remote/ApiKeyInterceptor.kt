package com.ities45.skycast.model.remote

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit
import com.ities45.skycast.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.crypto.AEADBadTagException

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
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPreferences = EncryptedSharedPreferences.create(
                "secure_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val storedApiKey = sharedPreferences.getString("OPENWEATHER_API_KEY", null)
            if (storedApiKey != null && storedApiKey.isNotEmpty()) {
                return storedApiKey
            }

            // If no valid key is stored, save the key from BuildConfig
            val apiKey = BuildConfig.OPENWEATHER_API_KEY
            if (apiKey.isNotEmpty()) {
                sharedPreferences.edit { putString("OPENWEATHER_API_KEY", apiKey) }
                return apiKey
            }
        } catch (e: AEADBadTagException) {
            Log.e("ApiKeyInterceptor", "Decryption failed, clearing SharedPreferences and retrying", e)
            // Clear corrupted SharedPreferences and retry
            context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE).edit().clear().apply()
            try {
                val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                val sharedPreferences = EncryptedSharedPreferences.create(
                    "secure_prefs",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                val apiKey = BuildConfig.OPENWEATHER_API_KEY
                if (apiKey.isNotEmpty()) {
                    sharedPreferences.edit { putString("OPENWEATHER_API_KEY", apiKey) }
                    return apiKey
                }
            } catch (e: Exception) {
                Log.e("ApiKeyInterceptor", "Failed to recreate SharedPreferences", e)
            }
        } catch (e: Exception) {
            Log.e("ApiKeyInterceptor", "Error accessing SharedPreferences", e)
        }
        return "" // Return empty string to trigger IllegalStateException
    }
}