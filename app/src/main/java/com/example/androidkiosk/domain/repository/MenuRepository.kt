package com.example.androidkiosk.domain.repository

import com.example.androidkiosk.model.CategoryWithItems
import com.example.androidkiosk.model.MenuItem
import kotlinx.coroutines.flow.Flow

interface MenuRepository {

    
    fun observeCategories(): Flow<List<CategoryWithItems>>

    
    fun observeBestSellers(): Flow<List<MenuItem>>

    
    suspend fun refresh()
}
