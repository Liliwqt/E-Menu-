package com.example.androidkiosk.domain.repository

import com.example.androidkiosk.model.CategoryWithItems
import com.example.androidkiosk.model.MenuItem
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun observeCategories(): Flow<List<CategoryWithItems>>
    fun observeBestSellers(): Flow<List<MenuItem>>

    /** Outer key: "category/itemId", inner key: size name (e.g. "Medium"), value: stock count. */
    fun observeInventoryStock(): Flow<Map<String, Map<String, Int>>>

    suspend fun refresh()
}
