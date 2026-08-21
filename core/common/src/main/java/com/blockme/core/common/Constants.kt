package com.blockme.core.common

/**
 * App-wide constants.
 * SPDX-License-Identifier: MIT
 */
object Constants {

    // ── Timer ────────────────────────────────────────────────────────────────
    const val MAX_SESSION_DURATION_MS = 3 * 60 * 60 * 1000L   // 3 hours in ms
    const val MAX_SESSION_DURATION_SECONDS = 3 * 60 * 60       // 3 hours in seconds

    // Preset durations in minutes
    val PRESET_DURATIONS_MINUTES = listOf(15, 30, 45, 60, 120, 180)

    // ── DataStore keys ────────────────────────────────────────────────────────
    const val DATASTORE_NAME = "blockme_prefs"
    const val KEY_IS_SESSION_ACTIVE = "is_session_active"
    const val KEY_SESSION_END_TIME = "session_end_time"
    const val KEY_SESSION_START_TIME = "session_start_time"
    const val KEY_SESSION_GOAL = "session_goal"
    const val KEY_SESSION_DURATION_MS = "session_duration_ms"
    const val KEY_LAST_DURATION_MS = "last_duration_ms"
    const val KEY_HAPTIC_ENABLED = "haptic_enabled"
    const val KEY_AMOLED_MODE = "amoled_mode"
    const val KEY_UNLOCK_CHALLENGE_ENABLED = "unlock_challenge_enabled"
    const val KEY_UNLOCK_CHALLENGE_TYPE = "unlock_challenge_type"
    const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    const val KEY_PHONE_APP_OPEN = "phone_app_open"
    const val KEY_TOTAL_TEMPTATIONS = "total_temptations"
    const val KEY_CURRENT_SESSION_ID = "current_session_id"

    // ── Intent Actions ────────────────────────────────────────────────────────
    const val ACTION_SESSION_COMPLETE = "com.blockme.app.ACTION_SESSION_COMPLETE"
    const val ACTION_SCHEDULED_SESSION = "com.blockme.app.ACTION_SCHEDULED_SESSION"
    const val ACTION_SESSION_REMINDER = "com.blockme.app.ACTION_SESSION_REMINDER"
    const val ACTION_SHOW_OVERLAY = "com.blockme.app.ACTION_SHOW_OVERLAY"
    const val ACTION_HIDE_OVERLAY = "com.blockme.app.ACTION_HIDE_OVERLAY"
    const val ACTION_TICK = "com.blockme.app.ACTION_TICK"
    const val ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON"
    const val ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT"

    // ── Notification ──────────────────────────────────────────────────────────
    const val NOTIFICATION_CHANNEL_SESSION = "blockme_session"
    const val NOTIFICATION_CHANNEL_REMINDER = "blockme_reminder"
    const val NOTIFICATION_ID_SESSION = 1001
    const val NOTIFICATION_ID_REMINDER = 1002

    // ── Extras ────────────────────────────────────────────────────────────────
    const val EXTRA_REMAINING_MS = "remaining_ms"
    const val EXTRA_DURATION_MS = "duration_ms"
    const val EXTRA_GOAL = "goal"
    const val EXTRA_SCHEDULE_ID = "schedule_id"
    const val EXTRA_SESSION_END_TIME = "session_end_time"

    // ── Alarm Request Codes ───────────────────────────────────────────────────
    const val REQUEST_CODE_SCHEDULE_BASE = 2000
    const val REQUEST_CODE_REMINDER_BASE = 3000

    // ── Packages ─────────────────────────────────────────────────────────────
    const val PACKAGE_SETTINGS = "com.android.settings"
    const val PACKAGE_ANDROID = "android"

    // ── Settings page class name fragments ───────────────────────────────────
    const val CLASS_ACCESSIBILITY_SETTINGS = "AccessibilitySettings"
    const val CLASS_DEVICE_ADMIN = "DeviceAdmin"
    const val CLASS_APP_INFO = "AppInfoDashboard"
    const val CLASS_MANAGE_APPS = "ManageApplications"

    // ── Quick Settings panels (multi-OEM) ────────────────────────────────────
    val QUICK_SETTINGS_PACKAGES = setOf(
        "com.android.systemui",
        "com.samsung.android.systemui",
        "miui.systemui",
        "com.oneplus.systemui",
    )
    val QUICK_SETTINGS_CLASS_FRAGMENTS = setOf(
        "QSPanel", "QuickSettings", "QuickStatusBar", "ShadeQS"
    )

    // ── Database ──────────────────────────────────────────────────────────────
    const val DATABASE_NAME = "blockme_db"
    const val DATABASE_VERSION = 1
}
