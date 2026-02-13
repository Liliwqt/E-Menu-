package com.example.androidkiosk.data.repository

import com.example.androidkiosk.data.remote.api.WeatherApiService
import com.example.androidkiosk.domain.repository.WeatherRepository
import com.example.androidkiosk.model.TimeOfDay
import com.example.androidkiosk.model.WeatherCondition
import com.example.androidkiosk.model.WeatherUiState
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService
) : WeatherRepository {

    override suspend fun getCurrentWeather(): WeatherUiState {
        return try {
            val response = weatherApiService.getCurrentWeather()
            val current = response.current
                ?: return WeatherUiState.Error("No weather data available")

            val condition = WeatherCondition.fromTemperature(current.temperature2m)
            val timeOfDay = TimeOfDay.fromCurrentTime()

            WeatherUiState.Available(
                temperatureC = current.temperature2m,
                condition = condition,
                timeOfDay = timeOfDay
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch weather data")
            WeatherUiState.Error("Weather unavailable")
        }
    }
}
