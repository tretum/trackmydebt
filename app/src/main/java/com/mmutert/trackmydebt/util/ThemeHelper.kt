package com.mmutert.trackmydebt.util

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.mmutert.trackmydebt.R

object ThemeHelper {
    fun applyTheme(preferenceValue: String, context: Context) {
        val lightModeKey = context.getString(R.string.theme_value_light)
        val darkModeKey = context.getString(R.string.theme_value_dark)
        when {
            lightModeKey == preferenceValue -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO)
            darkModeKey == preferenceValue -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
    }
}