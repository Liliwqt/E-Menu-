package com.example.androidkiosk.model

/**
 * MenuItem - Data Model for Menu Items
 */
data class MenuItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val available: Boolean = true
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
