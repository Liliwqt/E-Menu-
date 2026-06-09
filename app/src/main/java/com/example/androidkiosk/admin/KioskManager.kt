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
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** Manages Device Owner kiosk mode enforcement. */
@Singleton
class KioskManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dpm: DevicePolicyManager
) {
    private val componentName: ComponentName =
        KioskDeviceAdminReceiver.getComponentName(context)

    private val packageName: String = context.packageName

    /** Whether this app is the Device Owner. */
    val isDeviceOwner: Boolean
        get() = dpm.isDeviceOwnerApp(packageName)

    /** Enable full kiosk mode: Lock Task with all escape routes disabled. */
    fun enableKioskMode(activity: Activity) {
        if (!isDeviceOwner) {
            Timber.w("Not device owner — kiosk mode skipped (development mode)")
            return
        }

        try {
            // 1. Whitelist this package for Lock Task Mode
            dpm.setLockTaskPackages(componentName, arrayOf(packageName))

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

            Timber.i("Kiosk mode ENABLED successfully")
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to enable kiosk mode — security exception")
        } catch (e: Exception) {
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

            Timber.i("Kiosk mode DISABLED by admin")
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to disable kiosk mode — security exception")
        } catch (e: Exception) {
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
            val restrictions = mutableListOf(
                UserManager.DISALLOW_FACTORY_RESET,
                UserManager.DISALLOW_SAFE_BOOT,
                UserManager.DISALLOW_ADD_USER,
                UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
                UserManager.DISALLOW_ADJUST_VOLUME
            )

            // API 31+ restrictions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                @Suppress("NewApi")
                restrictions.add(UserManager.DISALLOW_USB_FILE_TRANSFER)
            }

            for (restriction in restrictions) {
                dpm.addUserRestriction(componentName, restriction)
            }

            Timber.i("User restrictions applied: %s", restrictions)
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to apply user restrictions")
        }
    }

    /** Remove user restrictions — called when admin needs full device access. */
    fun removeUserRestrictions() {
        if (!isDeviceOwner) return

        try {
            val restrictions = mutableListOf(
                UserManager.DISALLOW_FACTORY_RESET,
                UserManager.DISALLOW_SAFE_BOOT,
                UserManager.DISALLOW_ADD_USER,
                UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
                UserManager.DISALLOW_ADJUST_VOLUME
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                @Suppress("NewApi")
                restrictions.add(UserManager.DISALLOW_USB_FILE_TRANSFER)
            }

            for (restriction in restrictions) {
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

        try {
            val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            dpm.addPersistentPreferredActivity(
                componentName,
                intentFilter,
                ComponentName(packageName, "com.example.androidkiosk.ui.main.MainActivity")
            )
            Timber.i("Set as persistent preferred home activity")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set as home app")
        }
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
}
