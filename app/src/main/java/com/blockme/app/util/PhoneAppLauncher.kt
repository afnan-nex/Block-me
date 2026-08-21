package com.blockme.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.TelecomManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Determines the default dialer/phone app and launches it with OEM-safe fallbacks.
 *
 * SPDX-License-Identifier: MIT
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
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            val defaultDialer = telecomManager?.defaultDialerPackage
            if (!defaultDialer.isNullOrBlank()) {
                defaultDialer
            } else {
                val intent = Intent(Intent.ACTION_DIAL)
                val resolveInfo = context.packageManager.resolveActivity(intent, 0)
                resolveInfo?.activityInfo?.packageName
            }
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_DIAL)
            val resolveInfo = context.packageManager.resolveActivity(intent, 0)
            resolveInfo?.activityInfo?.packageName
        }
    }

    /**
     * Open the default phone app's dial screen with robust OEM fallbacks.
     */
    fun launchDefaultPhoneApp(): Boolean {
        val dialerPkg = getDefaultDialerPackage()

        // 1. Try direct package launch intent if available
        if (!dialerPkg.isNullOrBlank()) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(dialerPkg)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Try ACTION_DIAL intent with tel: URI
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            if (dialIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(dialIntent)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Fallback to generic ACTION_DIAL
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * Returns true if [packageName] is the system default dialer or an active in-call UI.
     */
    fun isDefaultDialerPackage(packageName: String): Boolean {
        val defaultPkg = getDefaultDialerPackage()
        if (defaultPkg != null && packageName.equals(defaultPkg, ignoreCase = true)) {
            return true
        }

        // Common OEM in-call and telecom packages
        val dialerPatterns = listOf(
            "com.google.android.dialer",
            "com.samsung.android.incallui",
            "com.samsung.android.dialer",
            "com.android.dialer",
            "com.android.incallui",
            "com.android.phone",
            "com.android.server.telecom"
        )
        return dialerPatterns.any { pattern -> packageName.contains(pattern, ignoreCase = true) }
    }
}
