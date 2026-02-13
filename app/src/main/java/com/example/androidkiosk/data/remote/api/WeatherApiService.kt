package com.example.androidkiosk.data.remote.api

import com.example.androidkiosk.data.remote.dto.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double = CEBU_LATITUDE,
        @Query("longitude") longitude: Double = CEBU_LONGITUDE,
        @Query("current") current: String = "temperature_2m",
        @Query("timezone") timezone: String = "Asia/Manila"
    ): OpenMeteoResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
        const val CEBU_LATITUDE = 10.3103
        const val CEBU_LONGITUDE = 123.8938
    }
}
