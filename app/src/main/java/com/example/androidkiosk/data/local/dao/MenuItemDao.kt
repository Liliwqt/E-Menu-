package com.example.androidkiosk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.androidkiosk.data.local.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {

    @Query("SELECT * FROM menu_items ORDER BY categoryName, name")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE isBestSeller = 1")
    fun getBestSellers(): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MenuItemEntity>)

    @Query("DELETE FROM menu_items")
    suspend fun clearAll()

    /** Atomically replaces all rows in a single transaction. */
    @Transaction
    suspend fun replaceAll(items: List<MenuItemEntity>) {
        clearAll()
        insertAll(items)
    }
}
