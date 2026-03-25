package com.example.androidkiosk.domain.repository

import com.example.androidkiosk.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {

    fun observeAppSettings(): Flow<AppSettings>
}
