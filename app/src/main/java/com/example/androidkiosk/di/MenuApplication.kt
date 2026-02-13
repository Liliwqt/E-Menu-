package com.example.androidkiosk.di

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MenuApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        runCatching {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        }.onFailure { error ->
            Timber.w(error, "Firebase persistence init failed")
        }
    }
}
