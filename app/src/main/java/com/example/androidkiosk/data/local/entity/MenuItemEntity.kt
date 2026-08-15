package com.example.androidkiosk.data.local.entity

import androidx.room.Entity

// Composite primary key: a single item (same `id`) can appear in multiple
// categories and each (id, categoryName) pair is stored as its own row.
@Entity(
    tableName = "menu_items",
    primaryKeys = ["id", "categoryName"]
)
data class MenuItemEntity(
    val id: String,
    val categoryName: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val available: Boolean,
    val isBestSeller: Boolean,
    /** Sizes serialized as JSON objects containing `priceModifier`; `{}` when none. */
    val sizesJson: String = "{}"
)
