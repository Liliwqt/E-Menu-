package com.example.androidkiosk.di

import com.example.androidkiosk.data.repository.MenuRepositoryImpl
import com.example.androidkiosk.data.repository.WeatherRepositoryImpl
import com.example.androidkiosk.domain.repository.MenuRepository
import com.example.androidkiosk.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository interfaces to their implementations.
 * This enables swapping implementations for testing.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMenuRepository(
        impl: MenuRepositoryImpl
    ): MenuRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository
}
