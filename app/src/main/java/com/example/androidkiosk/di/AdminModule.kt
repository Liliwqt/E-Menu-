package com.example.androidkiosk.di

import android.app.admin.DevicePolicyManager
import android.content.Context
import com.example.androidkiosk.admin.KioskManager
import com.example.androidkiosk.admin.PinManager
import com.example.androidkiosk.admin.UnlockAttemptLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused") // All @Provides functions are consumed by Hilt's generated DI code
@Module
@InstallIn(SingletonComponent::class)
object AdminModule {

    @Provides
    @Singleton
    fun provideDevicePolicyManager(
        @ApplicationContext context: Context
    ): DevicePolicyManager {
        return context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    @Provides
    @Singleton
    fun provideKioskManager(
        @ApplicationContext context: Context,
        dpm: DevicePolicyManager
    ): KioskManager {
        return KioskManager(context, dpm)
    }

    @Provides
    @Singleton
    fun providePinManager(
        @ApplicationContext context: Context
    ): PinManager {
        return PinManager(context)
    }

    @Provides
    @Singleton
    fun provideUnlockAttemptLogger(): UnlockAttemptLogger {
        return UnlockAttemptLogger()
    }
}
