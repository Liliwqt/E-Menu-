package com.example.androidkiosk.data.repository

import com.example.androidkiosk.admin.AuthManager
import com.example.androidkiosk.domain.repository.OrderRepository
import com.example.androidkiosk.model.Order
import com.example.androidkiosk.model.OrderLogEntry
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val authManager: AuthManager
) : OrderRepository {

    override suspend fun logOrder(order: Order): Result<Unit> {
        return try {
            // Wait until anonymous auth is confirmed before writing.
            // Without this, the write races against sign-in and gets PERMISSION_DENIED.
            if (!authManager.isAuthenticated.value) {
                Timber.d("Waiting for auth before logging order %s", order.orderNumber)
                authManager.isAuthenticated.first { it }
            }
            val entry = OrderLogEntry.fromOrder(order)
            database.getReference("branch2/logs/${order.orderNumber}")
                .setValue(entry.toMap())
                .await()
            Timber.i("Order %s logged successfully for %s", order.orderNumber, order.customerName)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to log order %s", order.orderNumber)
            Result.failure(e)
        }
    }
}
