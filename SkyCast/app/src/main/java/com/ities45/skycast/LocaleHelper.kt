package com.ities45.skycast

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    fun setAppLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        return context
    }
}