package com.example.androidkiosk.di

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class annotated with @HiltAndroidApp to enable Hilt dependency injection.
 * Initializes Firebase persistence and Timber logging.
 */
@HiltAndroidApp
class MenuApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging (replaces android.util.Log)
        Timber.plant(Timber.DebugTree())

        // Enable Firebase offline persistence
        runCatching {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        }.onFailure { error ->
            Timber.w(error, "Firebase persistence init failed")
        }
    }
}