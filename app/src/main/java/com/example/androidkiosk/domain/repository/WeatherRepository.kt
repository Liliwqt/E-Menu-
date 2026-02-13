package com.example.androidkiosk.domain.repository

import com.example.androidkiosk.model.WeatherUiState

interface WeatherRepository {

    
    suspend fun getCurrentWeather(): WeatherUiState
}
