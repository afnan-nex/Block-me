package com.blockme.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blockme.core.common.Constants

/**
 * Receives the session complete broadcast and signals MainActivity to show celebration.
 * SPDX-License-Identifier: MIT
 */
class SessionCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Constants.ACTION_SESSION_COMPLETE) {
            // Launch MainActivity to show the celebration screen
            context.startActivity(
                Intent(context, com.blockme.app.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("session_complete", true)
                }
            )
        }
    }
}
