package com.blockme.app.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Device Admin receiver — required for [android.app.admin.DevicePolicyManager.lockNow].
 *
 * Device Admin is REQUIRED for Block me's core lockdown feature.
 * Without it, the app cannot lock the screen when blocked actions are detected.
 *
 * SPDX-License-Identifier: MIT
 */
class LockdownDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // Device Admin has been granted — core lock feature now works
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        // Device Admin has been revoked — warn user
        Toast.makeText(
            context,
            "⚠ Device Admin disabled. Block me cannot lock your screen.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Block me requires Device Admin to lock your screen during focus sessions. " +
                "Disabling it will break the core lockdown feature."
    }
}
