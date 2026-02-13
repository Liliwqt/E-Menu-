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

enum class WeatherCondition(val label: String) {
    Hot("Hot"),
    Cold("Cool");

    companion object {
        fun fromTemperature(tempC: Double): WeatherCondition {
            return if (tempC >= 28.0) Hot else Cold
        }
    }
}

enum class TimeOfDay(val label: String, @RawRes val videoResId: Int) {
    Day("Day", R.raw.sunny_weather),
    Night("Night", R.raw.sunny_weather);

    companion object {
        fun fromCurrentTime(): TimeOfDay {
            val hour = LocalTime.now().hour
            return if (hour in 6..17) Day else Night
        }
    }
}

fun getWeatherDisplayLabel(condition: WeatherCondition, timeOfDay: TimeOfDay): String {
    return "${condition.label} ${timeOfDay.label}"
}
