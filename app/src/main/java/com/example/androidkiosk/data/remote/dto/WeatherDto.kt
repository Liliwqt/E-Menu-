package com.example.androidkiosk.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for the Open-Meteo weather API response.
 *
 * Example response:
 * ```json
 * {
 *   "current": {
 *     "temperature_2m": 30.5
 *   }
 * }
 * ```
 */
@Serializable
data class OpenMeteoResponse(
    @SerialName("current")
    val current: OpenMeteoCurrent? = null
)

@Serializable
data class OpenMeteoCurrent(
    @SerialName("temperature_2m")
    val temperature2m: Double = 0.0
)
