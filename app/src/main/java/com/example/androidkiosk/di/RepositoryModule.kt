package com.example.androidkiosk.di

import com.example.androidkiosk.data.repository.AppSettingsRepositoryImpl
import com.example.androidkiosk.data.repository.MenuRepositoryImpl
import com.example.androidkiosk.data.repository.OrderRepositoryImpl
import com.example.androidkiosk.domain.repository.AppSettingsRepository
import com.example.androidkiosk.domain.repository.MenuRepository
import com.example.androidkiosk.domain.repository.OrderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused") // Used by Hilt at compile time
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMenuRepository(
        impl: MenuRepositoryImpl
    ): MenuRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        impl: AppSettingsRepositoryImpl
    ): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        impl: OrderRepositoryImpl
    ): OrderRepository
}
