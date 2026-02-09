package com.example.androidkiosk.model

import androidx.annotation.RawRes
import com.example.androidkiosk.R
import java.time.LocalTime

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Available(
        val temperatureC: Double,
        val condition: WeatherCondition,
        val timeOfDay: TimeOfDay
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

/**
 * Temperature-based weather condition
 * Hot: >= 28°C (typical Cebu warm weather)
 * Cold: < 28°C (cooler, especially during rainy season or night)
 */
enum class WeatherCondition(val label: String) {
    Hot("Hot"),
    Cold("Cool");

    companion object {
        fun fromTemperature(tempC: Double): WeatherCondition {
            return if (tempC >= 28.0) Hot else Cold
        }
    }
}

/**
 * Time-based condition
 * Day: 6:00 AM - 5:59 PM
 * Night: 6:00 PM - 5:59 AM
 */
enum class TimeOfDay(val label: String, @RawRes val videoResId: Int) {
    Day("Day", R.raw.sunny_weather),      // Use your day video
    Night("Night", R.raw.sunny_weather);  // Use your night video (replace when you have one)

    companion object {
        fun fromCurrentTime(): TimeOfDay {
            val hour = LocalTime.now().hour
            return if (hour in 6..17) Day else Night
        }
    }
}

/**
 * Combined display label
 */
fun getWeatherDisplayLabel(condition: WeatherCondition, timeOfDay: TimeOfDay): String {
    return "${condition.label} ${timeOfDay.label}"
    // Examples: "Hot Day", "Cool Night", "Hot Night", "Cool Day"
}