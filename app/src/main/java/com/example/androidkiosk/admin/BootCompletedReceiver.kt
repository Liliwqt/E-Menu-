package com.example.androidkiosk.admin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

/** Receiver for BOOT_COMPLETED to auto-launch the kiosk app after device reboot. */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            Timber.i("Boot completed — launching kiosk app")

            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

            if (launchIntent != null) {
                try {
                    context.startActivity(launchIntent)
                    Timber.i("Kiosk app launched successfully after boot")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to launch kiosk app after boot")
                }
            } else {
                Timber.e("Failed to get launch intent for kiosk app")
            }
        }
    }
}
