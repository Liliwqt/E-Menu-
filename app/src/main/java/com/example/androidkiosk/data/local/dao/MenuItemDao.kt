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

    @Query("SELECT * FROM menu_items WHERE available = 1 ORDER BY categoryName, name")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE isBestSeller = 1 AND available = 1")
    fun getBestSellers(): Flow<List<MenuItemEntity>>

    @Query("SELECT DISTINCT categoryName FROM menu_items WHERE available = 1")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT * FROM menu_items WHERE categoryName = :category AND available = 1")
    fun getItemsByCategory(category: String): Flow<List<MenuItemEntity>>

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
