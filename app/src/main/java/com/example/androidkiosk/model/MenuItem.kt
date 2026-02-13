package com.example.androidkiosk.model

import com.google.firebase.database.PropertyName

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val available: Boolean = true,
    @get:PropertyName("isBestSeller")
    var isBestSeller: Boolean = false
)

data class CategoryWithItems(
    val categoryName: String,
    val items: List<MenuItem>
)

data class CartItem(
    val menuItem: MenuItem,
    val name: String,
    val price: Double,
    var quantity: Int
)
