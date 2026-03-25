package com.example.androidkiosk.admin

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Manages admin PIN validation for kiosk unlock.
 *
 * The PIN hash is persisted in [EncryptedSharedPreferences] backed by
 * the Android Keystore, so it survives app restarts and is protected
 * from extraction on non-rooted devices.
 *
 * The default PIN is "1234". On first launch, flagged via [isPinChanged]
 * so the admin can be prompted to change it.
 */
@Singleton
class PinManager @Inject constructor(
    context: Context
) {

    private val prefs: SharedPreferences = createEncryptedPrefs(context)

    /**
     * SHA-256 hash of the current admin PIN, loaded from encrypted storage.
     * Falls back to DEFAULT_PIN_HASH on first launch.
     */
    private var pinHash: String
        get() = prefs.getString(KEY_PIN_HASH, DEFAULT_PIN_HASH) ?: DEFAULT_PIN_HASH
        set(value) {
            prefs.edit { putString(KEY_PIN_HASH, value) }
        }

    /** Whether the admin has changed the default PIN at least once. */
    val isPinChanged: Boolean
        get() = prefs.getBoolean(KEY_PIN_CHANGED, false)

    /** Number of consecutive failed attempts in the current session. */
    @Volatile
    var failedAttempts: Int = 0
        private set

    /** Timestamp when lockout expires (0 = not locked out). */
    @Volatile
    var lockoutUntil: Long = 0L
        private set

    /**
     * Validate the entered PIN against the stored hash.
     *
     * @return true if PIN matches, false otherwise.
     */
    fun validatePin(input: String): Boolean {
        // Check lockout
        if (isLockedOut()) {
            Timber.w("PIN validation rejected — lockout active")
            return false
        }

        val inputHash = sha256(input)
        val isValid = inputHash == pinHash

        if (isValid) {
            failedAttempts = 0
            lockoutUntil = 0L
            Timber.i("PIN validated successfully")
        } else {
            failedAttempts++
            Timber.w("Invalid PIN attempt")

            // Exponential lockout: 60s → 120s → 240s after MAX_ATTEMPTS consecutive failures
            if (failedAttempts >= MAX_ATTEMPTS) {
                val multiplier = (failedAttempts / MAX_ATTEMPTS).coerceAtMost(3)
                val duration = LOCKOUT_DURATION_MS * (1L shl (multiplier - 1))
                lockoutUntil = System.currentTimeMillis() + duration
                Timber.w("Lockout activated for %d seconds", duration / 1000)
            }
        }

        return isValid
    }

    /** Whether the PIN entry is currently locked out due to too many failed attempts. */
    fun isLockedOut(): Boolean {
        if (lockoutUntil == 0L) return false
        if (System.currentTimeMillis() >= lockoutUntil) {
            // Lockout expired — reset
            lockoutUntil = 0L
            failedAttempts = 0
            return false
        }
        return true
    }

    /** Remaining lockout time in milliseconds, or 0 if not locked out. */
    fun remainingLockoutMs(): Long {
        if (!isLockedOut()) return 0L
        return (lockoutUntil - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    /** Reset the failed attempt counter (e.g., after a successful unlock or dialog dismiss). */
    fun resetAttempts() {
        failedAttempts = 0
        lockoutUntil = 0L
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        /** Default PIN: "1234" */
        private const val DEFAULT_PIN_HASH =
            "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"

        /** Maximum failed attempts before lockout. */
        const val MAX_ATTEMPTS = 3

        /** Base lockout duration: 60 seconds (doubles for each subsequent lockout). */
        const val LOCKOUT_DURATION_MS = 60_000L

        private const val PREFS_FILE = "kiosk_secure_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_CHANGED = "pin_changed"

        private fun createEncryptedPrefs(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}
