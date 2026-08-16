package com.blockme.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.TelecomManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Determines the default dialer/phone app and launches it.
 *
 * This is the ONLY permitted way to leave the overlay during an active session.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Singleton
class PhoneAppLauncher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * @return Package name of the default phone/dialer app, or null if unavailable.
     */
    fun getDefaultDialerPackage(): String? {
        return try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.defaultDialerPackage
        } catch (e: Exception) {
            // Fallback: resolve the DIAL intent
            val intent = Intent(Intent.ACTION_DIAL)
            val resolveInfo = context.packageManager.resolveActivity(intent, 0)
            resolveInfo?.activityInfo?.packageName
        }
    }

    /**
     * Open the default phone app's dial screen.
     */
    fun launchDefaultPhoneApp() {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }

    /**
     * Returns true if [packageName] is the system default dialer.
     */
    fun isDefaultDialerPackage(packageName: String): Boolean {
        return packageName == getDefaultDialerPackage()
    }
}
