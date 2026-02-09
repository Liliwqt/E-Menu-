package com.example.androidkiosk.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the app.
 * Using Kotlin Serialization with Navigation Compose for compile-time safety.
 */
sealed interface Route {

    /** Main menu screen with categories and best sellers. */
    @Serializable
    data object Menu : Route

    /** Shopping cart screen. */
    @Serializable
    data object Cart : Route

    /** Checkout / order confirmation screen. */
    @Serializable
    data object Checkout : Route
}
