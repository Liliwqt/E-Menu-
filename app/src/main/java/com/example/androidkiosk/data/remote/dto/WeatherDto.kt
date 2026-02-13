package com.example.androidkiosk.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
