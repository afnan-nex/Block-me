package com.blockme.app.util

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import com.blockme.app.service.LockdownDeviceAdminReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [DevicePolicyManager.lockNow] with vibration feedback.
 *
 * Called by AccessibilityService on blocked action detection.
 * SPDX-License-Identifier: MIT
 */
@Singleton
class ScreenLockManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dpm: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponent = ComponentName(context, LockdownDeviceAdminReceiver::class.java)

    val isAdminActive: Boolean
        get() = dpm.isAdminActive(adminComponent)

    /**
     * Lock the screen immediately and vibrate to signal the blocked action.
     */
    fun lockNow() {
        vibrate()
        if (isAdminActive) {
            try {
                dpm.lockNow()
            } catch (e: SecurityException) {
                // Device admin may have been revoked — log only
                e.printStackTrace()
            }
        }
    }

    private fun vibrate() {
        try {
            val effect = VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(effect)
            }
        } catch (e: Exception) {
            // Non-critical — swallow exceptions from vibration
        }
    }
}
