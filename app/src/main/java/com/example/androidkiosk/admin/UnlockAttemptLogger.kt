package com.example.androidkiosk.admin

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Logs admin unlock attempts for audit purposes. */
@Singleton
class UnlockAttemptLogger @Inject constructor() {

    private val _attempts = CopyOnWriteArrayList<UnlockAttempt>()

    /** Read-only view of recent unlock attempts (most recent first). */
    @Suppress("unused") // Public API for admin dashboard / fleet management
    val recentAttempts: List<UnlockAttempt>
        get() = _attempts.sortedByDescending { it.timestamp }

    /** Log an unlock attempt. */
    fun logAttempt(method: UnlockMethod, success: Boolean) {
        val attempt = UnlockAttempt(
            timestamp = System.currentTimeMillis(),
            method = method,
            success = success
        )

        _attempts.add(attempt)

        // Keep only the most recent MAX_STORED_ATTEMPTS
        while (_attempts.size > MAX_STORED_ATTEMPTS) {
            _attempts.removeAt(0)
        }

        val dateStr = DATE_FORMAT.format(Date(attempt.timestamp))
        val statusStr = if (success) "SUCCESS" else "FAILED"

        Timber.i(
            "Unlock attempt [%s] via %s at %s",
            statusStr,
            method.name,
            dateStr
        )
    }

    /** Clear all stored attempts. */
    @Suppress("unused") // Public API for admin dashboard / fleet management
    fun clearHistory() {
        _attempts.clear()
        Timber.i("Unlock attempt history cleared")
    }

    companion object {
        private const val MAX_STORED_ATTEMPTS = 100
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}

/** How the admin PIN dialog was triggered. */
enum class UnlockMethod {
    /** Volume Up long press (3+ seconds). */
    VOLUME_BUTTON,

    /** Secret corner tap (5 taps in top-right corner). */
    CORNER_TAP
}

/** A single unlock attempt record. */
data class UnlockAttempt(
    val timestamp: Long,
    val method: UnlockMethod,
    val success: Boolean
)
