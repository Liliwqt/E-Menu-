package com.example.androidkiosk.admin

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Firebase Anonymous Authentication for the kiosk app.
 *
 * The kiosk uses anonymous auth so that Firebase Security Rules can enforce
 * `auth != null` on reads — blocking unauthenticated access from anyone
 * who extracts the API key from the APK.
 *
 * Write access is reserved for web admin users who sign in with
 * email/password (enforced via `auth.token.email != null` in the rules).
 */
@Singleton
class AuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    private val _isAuthenticated = MutableStateFlow(firebaseAuth.currentUser != null)

    /** Emits `true` once anonymous sign-in succeeds; `false` until then or on sign-out. */
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        // Listen for auth state changes (e.g., token refresh, sign-out)
        firebaseAuth.addAuthStateListener { auth ->
            val signedIn = auth.currentUser != null
            _isAuthenticated.value = signedIn
            Timber.d("Auth state changed — signed in: %s", signedIn)
        }
    }

    /**
     * Sign in anonymously. Safe to call multiple times — skips if already authenticated.
     * Should be called during [android.app.Application.onCreate].
     */
    suspend fun ensureSignedIn() {
        if (firebaseAuth.currentUser != null) {
            Timber.d("Already signed in (uid=%s)", firebaseAuth.currentUser?.uid)
            return
        }
        try {
            val result = firebaseAuth.signInAnonymously().await()
            Timber.i("Anonymous sign-in succeeded (uid=%s)", result.user?.uid)
        } catch (e: Exception) {
            Timber.e(e, "Anonymous sign-in failed — Firebase reads will be denied by security rules")
        }
    }
}
