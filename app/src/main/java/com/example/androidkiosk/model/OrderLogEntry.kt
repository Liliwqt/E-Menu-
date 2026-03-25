package com.example.androidkiosk.model

import com.google.firebase.database.ServerValue

/**
 * Flattened representation of an order for Firebase logging at `branch2/logs/{orderNumber}`.
 */
data class OrderLogEntry(
    val orderNumber: String,
    val customerName: String,
    val items: List<OrderLogItem>,
    val total: Double,
    val paymentMethod: String,
    val timestamp: Any  // ServerValue.TIMESTAMP when writing
) {
    /** Converts to a plain Map for Firebase `setValue()`. */
    fun toMap(): Map<String, Any?> = mapOf(
        "orderNumber" to orderNumber,
        "customerName" to customerName,
        "items" to items.map { it.toMap() },
        "total" to total,
        "paymentMethod" to paymentMethod,
        "timestamp" to timestamp
    )

    companion object {
        fun fromOrder(order: Order): OrderLogEntry = OrderLogEntry(
            orderNumber = order.orderNumber,
            customerName = order.customerName,
            items = order.items.map { OrderLogItem.fromCartItem(it) },
            total = order.total,
            paymentMethod = order.paymentMethod?.name ?: "UNKNOWN",
            timestamp = ServerValue.TIMESTAMP
        )
    }
}

/**
 * Single line-item in the order log — a flat DTO (no nested MenuItem).
 */
data class OrderLogItem(
    val name: String,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
) {
    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "quantity" to quantity,
        "price" to price,
        "subtotal" to subtotal
    )

    companion object {
        fun fromCartItem(cartItem: CartItem): OrderLogItem = OrderLogItem(
            name = cartItem.menuItem.name,
            quantity = cartItem.quantity,
            price = cartItem.menuItem.price,
            subtotal = cartItem.menuItem.price * cartItem.quantity
        )
    }
}
