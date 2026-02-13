package com.example.androidkiosk.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    
    @Serializable
    data object Menu : Route

    
    @Serializable
    data object Cart : Route

    
    @Serializable
    data object Checkout : Route
}
