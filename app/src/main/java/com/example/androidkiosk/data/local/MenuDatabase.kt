package com.example.androidkiosk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.androidkiosk.data.local.dao.MenuItemDao
import com.example.androidkiosk.data.local.entity.MenuItemEntity

/**
 * Room database for local caching of menu data.
 * Provides offline-first capability for the kiosk app.
 */
@Database(
    entities = [MenuItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MenuDatabase : RoomDatabase() {
    abstract fun menuItemDao(): MenuItemDao

    companion object {
        const val DATABASE_NAME = "menu_database"
    }
}
