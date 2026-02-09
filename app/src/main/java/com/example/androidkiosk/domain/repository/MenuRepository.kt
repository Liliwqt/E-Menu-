package com.example.androidkiosk.domain.repository

import com.example.androidkiosk.model.CategoryWithItems
import com.example.androidkiosk.model.MenuItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for menu data.
 *
 * Abstracts Firebase + Room so the ViewModel never knows
 * where the data comes from. Enables easy testing with fakes.
 */
interface MenuRepository {

    /** Real-time stream of all categories with their items. */
    fun observeCategories(): Flow<List<CategoryWithItems>>

    /** Real-time stream of best-seller items. */
    fun observeBestSellers(): Flow<List<MenuItem>>

    /** Force-refresh from the remote source. */
    suspend fun refresh()
}
