package com.example.androidkiosk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val available: Boolean,
    val categoryName: String,
    val isBestSeller: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)
