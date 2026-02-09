package com.example.androidkiosk.domain.repository

import com.example.androidkiosk.model.WeatherUiState

/**
 * Repository interface for weather data.
 * Abstracts the weather API so ViewModel doesn't know about Retrofit.
 */
interface WeatherRepository {

    /** Fetches the current weather for Cebu City. */
    suspend fun getCurrentWeather(): WeatherUiState
}
