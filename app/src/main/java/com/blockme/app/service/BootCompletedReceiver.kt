package com.blockme.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blockme.core.common.Constants
import com.blockme.core.common.dataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Receives BOOT_COMPLETED and restarts the overlay + timer services if a session is active.
 *
 * Uses the stored session end-time (wall-clock timestamp) to determine if the session
 * is still valid. If the timer expired while the device was off, clears the session state.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val prefs = context.dataStore.data.first()
            val isSessionActive = prefs[booleanPreferencesKey(Constants.KEY_IS_SESSION_ACTIVE)] ?: false
            val endTimeMs = prefs[longPreferencesKey(Constants.KEY_SESSION_END_TIME)] ?: 0L
            val now = System.currentTimeMillis()

            if (isSessionActive && endTimeMs > now) {
                // Session still active — restart overlay and timer
                context.startForegroundService(
                    Intent(context, LockdownOverlayService::class.java)
                )
                context.startForegroundService(
                    Intent(context, TimerForegroundService::class.java)
                )
            } else if (isSessionActive && endTimeMs <= now) {
                // Session expired while device was off — mark complete
                context.dataStore.updateData { current ->
                    current.toMutablePreferences().apply {
                        set(booleanPreferencesKey(Constants.KEY_IS_SESSION_ACTIVE), false)
                        set(longPreferencesKey(Constants.KEY_SESSION_END_TIME), 0L)
                    }
                }
            }
        }
    }
}
