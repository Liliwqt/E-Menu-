package com.example.androidkiosk.di

import android.content.Context
import androidx.room.Room
import com.example.androidkiosk.data.local.MenuDatabase
import com.example.androidkiosk.data.local.dao.MenuItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMenuDatabase(
        @ApplicationContext context: Context
    ): MenuDatabase {
        return Room.databaseBuilder(
            context,
            MenuDatabase::class.java,
            MenuDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideMenuItemDao(database: MenuDatabase): MenuItemDao {
        return database.menuItemDao()
    }
}
