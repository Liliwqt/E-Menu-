package com.example.androidkiosk.domain.repository

import com.example.androidkiosk.model.Order

/**
 * Writes completed orders to Firebase for receipt / logging purposes.
 */
interface OrderRepository {
    /** Log the given [order] to `branch2/logs/{orderNumber}`. */
    suspend fun logOrder(order: Order): Result<Unit>
}
