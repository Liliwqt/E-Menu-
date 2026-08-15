package com.example.androidkiosk.admin

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.UserManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

enum class KioskEnforcementStatus {
    INITIALIZING,
    ACTIVE,
    ADMIN_UNLOCKED,
    NOT_DEVICE_OWNER,
    ERROR
}

object KioskReleaseGate {
    fun isOrderingBlocked(
        isDebuggable: Boolean,
        isAdminUnlocked: Boolean,
        status: KioskEnforcementStatus
    ): Boolean = !isDebuggable && !isAdminUnlocked && status != KioskEnforcementStatus.ACTIVE
}

/** Manages Device Owner kiosk mode enforcement. */
@Singleton
class KioskManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dpm: DevicePolicyManager
) {
    private val _enforcementStatus = MutableStateFlow(KioskEnforcementStatus.INITIALIZING)
    val enforcementStatus: StateFlow<KioskEnforcementStatus> = _enforcementStatus.asStateFlow()

    private val componentName: ComponentName =
        KioskDeviceAdminReceiver.getComponentName(context)

    private val packageName: String = context.packageName

    /** Whether this app is the Device Owner. */
    val isDeviceOwner: Boolean
        get() = dpm.isDeviceOwnerApp(packageName)

    /** Enable full kiosk mode: Lock Task with all escape routes disabled. */
    fun enableKioskMode(activity: Activity) {
        if (!isDeviceOwner) {
            _enforcementStatus.value = KioskEnforcementStatus.NOT_DEVICE_OWNER
            Timber.w("Not device owner — kiosk mode skipped (development mode)")
            return
        }

        try {
            // 1. Whitelist this package for Lock Task Mode
            dpm.setLockTaskPackages(componentName, arrayOf(packageName))
            check(dpm.isLockTaskPermitted(packageName)) { "Package was not allowlisted for lock task" }

            // 2. Set lock task features — disable everything
            dpm.setLockTaskFeatures(
                componentName,
                DevicePolicyManager.LOCK_TASK_FEATURE_NONE
            )

            // 3. Disable keyguard (lock screen)
            dpm.setKeyguardDisabled(componentName, true)

            // 4. Disable status bar
            dpm.setStatusBarDisabled(componentName, true)

            // 5. Enter Lock Task Mode
            activity.startLockTask()

            // 6. Set as preferred home activity
            setAsHomeApp()

            _enforcementStatus.value = KioskEnforcementStatus.ACTIVE

            Timber.i("Kiosk mode ENABLED successfully")
        } catch (e: SecurityException) {
            _enforcementStatus.value = KioskEnforcementStatus.ERROR
            Timber.e(e, "Failed to enable kiosk mode — security exception")
        } catch (e: Exception) {
            _enforcementStatus.value = KioskEnforcementStatus.ERROR
            Timber.e(e, "Failed to enable kiosk mode")
        }
    }

    /** Disable kiosk mode — called after successful admin PIN authentication. */
    fun disableKioskMode(activity: Activity) {
        if (!isDeviceOwner) {
            Timber.w("Not device owner — disable kiosk skipped")
            return
        }

        try {
            // 1. Exit Lock Task Mode
            activity.stopLockTask()

            // 2. Re-enable status bar
            dpm.setStatusBarDisabled(componentName, false)

            // 3. Re-enable keyguard
            dpm.setKeyguardDisabled(componentName, false)

            _enforcementStatus.value = KioskEnforcementStatus.ADMIN_UNLOCKED

            Timber.i("Kiosk mode DISABLED by admin")
        } catch (e: SecurityException) {
            _enforcementStatus.value = KioskEnforcementStatus.ERROR
            Timber.e(e, "Failed to disable kiosk mode — security exception")
        } catch (e: Exception) {
            _enforcementStatus.value = KioskEnforcementStatus.ERROR
            Timber.e(e, "Failed to disable kiosk mode")
        }
    }

    /** Re-engage kiosk mode (e.g., admin relocking, or automatic re-lock on resume). */
    fun relockKioskMode(activity: Activity) {
        enableKioskMode(activity)
    }

    /** Apply device-wide user restrictions to prevent escape routes. */
    fun applyUserRestrictions() {
        if (!isDeviceOwner) return

        try {
            for (restriction in KIOSK_USER_RESTRICTIONS) {
                dpm.addUserRestriction(componentName, restriction)
            }

            Timber.i("User restrictions applied: %s", KIOSK_USER_RESTRICTIONS)
        } catch (e: SecurityException) {
            _enforcementStatus.value = KioskEnforcementStatus.ERROR
            Timber.e(e, "Failed to apply user restrictions")
        }
    }

    /** Remove user restrictions — called when admin needs full device access. */
    fun removeUserRestrictions() {
        if (!isDeviceOwner) return

        try {
            for (restriction in KIOSK_USER_RESTRICTIONS) {
                dpm.clearUserRestriction(componentName, restriction)
            }

            Timber.i("User restrictions removed")
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to remove user restrictions")
        }
    }

    /** Set this app as the persistent preferred home activity. */
    private fun setAsHomeApp() {
        if (!isDeviceOwner) return

        val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        dpm.addPersistentPreferredActivity(
            componentName,
            intentFilter,
            ComponentName(packageName, "${packageName}.ui.main.MainActivity")
        )
        Timber.i("Set as persistent preferred home activity")
    }

    /** Clear the persistent preferred home activity (for admin unlock). */
    fun clearHomeApp() {
        if (!isDeviceOwner) return

        try {
            dpm.clearPackagePersistentPreferredActivities(
                componentName,
                packageName
            )
            Timber.i("Cleared persistent preferred home activity")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear home app")
        }
    }

    companion object {
        /** User restrictions applied in kiosk mode to prevent escape routes. */
        private val KIOSK_USER_RESTRICTIONS: List<String> = buildList {
            add(UserManager.DISALLOW_FACTORY_RESET)
            add(UserManager.DISALLOW_SAFE_BOOT)
            add(UserManager.DISALLOW_ADD_USER)
            add(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA)
            add(UserManager.DISALLOW_ADJUST_VOLUME)
            add(UserManager.DISALLOW_CREATE_WINDOWS)
            add(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                @Suppress("NewApi")
                add(UserManager.DISALLOW_USB_FILE_TRANSFER)
            }
        }
    }
}
