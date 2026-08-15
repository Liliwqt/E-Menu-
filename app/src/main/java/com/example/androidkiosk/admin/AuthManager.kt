package com.example.androidkiosk.admin

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

enum class KioskRegistrationStatus {
    AUTHENTICATING,
    PENDING_REGISTRATION,
    AUTHORIZED,
    ERROR
}

data class KioskAuthorizationState(
    val uid: String? = null,
    val status: KioskRegistrationStatus = KioskRegistrationStatus.AUTHENTICATING,
    val errorMessage: String? = null
) {
    val isAuthorized: Boolean
        get() = status == KioskRegistrationStatus.AUTHORIZED
}

/**
 * Signs the device in anonymously and probes a protected branch to confirm that the UID has been
 * manually added to the deployed Realtime Database rules. Authentication alone is not treated as
 * authorization.
 */
@Singleton
class AuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    private val _authorizationState = MutableStateFlow(KioskAuthorizationState())
    val authorizationState: StateFlow<KioskAuthorizationState> = _authorizationState.asStateFlow()

    val userId: String?
        get() = firebaseAuth.currentUser?.uid

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        observeRegistration(auth.currentUser)
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    /** Sign in anonymously. Safe to call repeatedly. */
    suspend fun ensureSignedIn() {
        if (firebaseAuth.currentUser != null) {
            probeAuthorization(firebaseAuth.currentUser!!.uid)
            return
        }

        _authorizationState.value = KioskAuthorizationState()
        try {
            firebaseAuth.signInAnonymously().await()
        } catch (error: Exception) {
            _authorizationState.value = KioskAuthorizationState(
                status = KioskRegistrationStatus.ERROR,
                errorMessage = "Unable to authenticate this kiosk. Check the network and retry."
            )
            Timber.e(error, "Anonymous kiosk authentication failed")
        }
    }

    /** Re-checks the protected branch after an operator manually adds the displayed UID to rules. */
    suspend fun refreshAuthorization() {
        ensureSignedIn()
        val user = firebaseAuth.currentUser ?: return
        probeAuthorization(user.uid)
    }

    private fun observeRegistration(user: FirebaseUser?) {
        if (user == null) {
            _authorizationState.value = KioskAuthorizationState()
            return
        }

        probeAuthorization(user.uid)
    }

    private fun probeAuthorization(uid: String) {
        _authorizationState.value = KioskAuthorizationState(
            uid = uid,
            status = KioskRegistrationStatus.AUTHENTICATING
        )
        val reference = database.getReference(AUTHORIZATION_PROBE_PATH)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _authorizationState.value = KioskAuthorizationState(
                    uid = uid,
                    status = KioskRegistrationStatus.AUTHORIZED
                )
            }

            override fun onCancelled(error: DatabaseError) {
                publishProbeFailure(uid, error)
                Timber.e(error.toException(), "Kiosk registration listener cancelled")
            }
        }
        reference.addListenerForSingleValueEvent(listener)
    }

    /** Called when a protected listener or write is rejected after a UID was previously allowed. */
    fun reportAuthorizationDenied() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        _authorizationState.value = KioskAuthorizationState(
            uid = uid,
            status = KioskRegistrationStatus.PENDING_REGISTRATION
        )
    }

    private fun publishProbeFailure(uid: String, error: DatabaseError) {
        _authorizationState.value = if (error.code == DatabaseError.PERMISSION_DENIED) {
            KioskAuthorizationState(uid, KioskRegistrationStatus.PENDING_REGISTRATION)
        } else {
            KioskAuthorizationState(
                uid = uid,
                status = KioskRegistrationStatus.ERROR,
                errorMessage = "Unable to verify kiosk authorization. Check the network and retry."
            )
        }
    }

    private companion object {
        const val AUTHORIZATION_PROBE_PATH = "branch2/appSettings"
    }
}
