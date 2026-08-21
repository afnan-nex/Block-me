package com.blockme.core.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.blockme.core.common.Constants
import com.blockme.core.common.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed user preferences.
 *
 * All session state is stored here so that it survives app restarts and reboots.
 * SPDX-License-Identifier: MIT
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ── Keys ─────────────────────────────────────────────────────────────────
    private val KEY_IS_SESSION_ACTIVE   = booleanPreferencesKey(Constants.KEY_IS_SESSION_ACTIVE)
    private val KEY_SESSION_END_TIME    = longPreferencesKey(Constants.KEY_SESSION_END_TIME)
    private val KEY_SESSION_START_TIME  = longPreferencesKey(Constants.KEY_SESSION_START_TIME)
    private val KEY_SESSION_GOAL        = stringPreferencesKey(Constants.KEY_SESSION_GOAL)
    private val KEY_SESSION_DURATION_MS = longPreferencesKey(Constants.KEY_SESSION_DURATION_MS)
    private val KEY_LAST_DURATION_MS    = longPreferencesKey(Constants.KEY_LAST_DURATION_MS)
    private val KEY_HAPTIC_ENABLED      = booleanPreferencesKey(Constants.KEY_HAPTIC_ENABLED)
    private val KEY_AMOLED_MODE         = booleanPreferencesKey(Constants.KEY_AMOLED_MODE)
    private val KEY_UNLOCK_CHALLENGE    = booleanPreferencesKey(Constants.KEY_UNLOCK_CHALLENGE_ENABLED)
    private val KEY_UNLOCK_CHALLENGE_TYPE = stringPreferencesKey(Constants.KEY_UNLOCK_CHALLENGE_TYPE)
    private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey(Constants.KEY_ONBOARDING_COMPLETE)
    private val KEY_PHONE_APP_OPEN      = booleanPreferencesKey(Constants.KEY_PHONE_APP_OPEN)
    private val KEY_CURRENT_SESSION_ID  = longPreferencesKey(Constants.KEY_CURRENT_SESSION_ID)

    // ── Flows ─────────────────────────────────────────────────────────────────
    val isSessionActive: Flow<Boolean>  = context.dataStore.data.map { it[KEY_IS_SESSION_ACTIVE] ?: false }
    val sessionEndTime: Flow<Long>      = context.dataStore.data.map { it[KEY_SESSION_END_TIME] ?: 0L }
    val sessionStartTime: Flow<Long>    = context.dataStore.data.map { it[KEY_SESSION_START_TIME] ?: 0L }
    val sessionGoal: Flow<String>       = context.dataStore.data.map { it[KEY_SESSION_GOAL] ?: "" }
    val sessionDurationMs: Flow<Long>   = context.dataStore.data.map { it[KEY_SESSION_DURATION_MS] ?: 0L }
    val lastDurationMs: Flow<Long>      = context.dataStore.data.map { it[KEY_LAST_DURATION_MS] ?: (30 * 60 * 1000L) }
    val hapticEnabled: Flow<Boolean>    = context.dataStore.data.map { it[KEY_HAPTIC_ENABLED] ?: false }
    val amoledMode: Flow<Boolean>       = context.dataStore.data.map { it[KEY_AMOLED_MODE] ?: false }
    val unlockChallengeEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_UNLOCK_CHALLENGE] ?: true }
    val unlockChallengeType: Flow<String> = context.dataStore.data.map { it[KEY_UNLOCK_CHALLENGE_TYPE] ?: "MATH" }
    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_COMPLETE] ?: false }
    val isPhoneAppOpen: Flow<Boolean>   = context.dataStore.data.map { it[KEY_PHONE_APP_OPEN] ?: false }
    val currentSessionId: Flow<Long>    = context.dataStore.data.map { it[KEY_CURRENT_SESSION_ID] ?: -1L }

    // ── Write operations ──────────────────────────────────────────────────────
    suspend fun setSessionActive(active: Boolean) {
        context.dataStore.edit { it[KEY_IS_SESSION_ACTIVE] = active }
    }

    suspend fun setSessionEndTime(endTimeMs: Long) {
        context.dataStore.edit { it[KEY_SESSION_END_TIME] = endTimeMs }
    }

    suspend fun setSessionStartTime(startTimeMs: Long) {
        context.dataStore.edit { it[KEY_SESSION_START_TIME] = startTimeMs }
    }

    suspend fun setSessionGoal(goal: String) {
        context.dataStore.edit { it[KEY_SESSION_GOAL] = goal }
    }

    suspend fun setSessionDurationMs(durationMs: Long) {
        context.dataStore.edit { it[KEY_SESSION_DURATION_MS] = durationMs }
    }

    suspend fun setLastDurationMs(durationMs: Long) {
        context.dataStore.edit { it[KEY_LAST_DURATION_MS] = durationMs }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAPTIC_ENABLED] = enabled }
    }

    suspend fun setAmoledMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AMOLED_MODE] = enabled }
    }

    suspend fun setUnlockChallengeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_UNLOCK_CHALLENGE] = enabled }
    }

    suspend fun setUnlockChallengeType(type: String) {
        context.dataStore.edit { it[KEY_UNLOCK_CHALLENGE_TYPE] = type }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setPhoneAppOpen(open: Boolean) {
        context.dataStore.edit { it[KEY_PHONE_APP_OPEN] = open }
    }

    suspend fun setCurrentSessionId(id: Long) {
        context.dataStore.edit { it[KEY_CURRENT_SESSION_ID] = id }
    }

    /** Atomically start a session — writes all related keys in one transaction. */
    suspend fun startSession(endTimeMs: Long, startTimeMs: Long, durationMs: Long, goal: String, sessionId: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_SESSION_ACTIVE]   = true
            prefs[KEY_SESSION_END_TIME]    = endTimeMs
            prefs[KEY_SESSION_START_TIME]  = startTimeMs
            prefs[KEY_SESSION_DURATION_MS] = durationMs
            prefs[KEY_SESSION_GOAL]        = goal
            prefs[KEY_LAST_DURATION_MS]    = durationMs
            prefs[KEY_CURRENT_SESSION_ID]  = sessionId
            prefs[KEY_PHONE_APP_OPEN]      = false
        }
    }

    /** Atomically end a session — clears all active session keys. */
    suspend fun endSession() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_SESSION_ACTIVE]   = false
            prefs[KEY_SESSION_END_TIME]    = 0L
            prefs[KEY_SESSION_START_TIME]  = 0L
            prefs[KEY_SESSION_DURATION_MS] = 0L
            prefs[KEY_SESSION_GOAL]        = ""
            prefs[KEY_PHONE_APP_OPEN]      = false
            prefs[KEY_CURRENT_SESSION_ID]  = -1L
        }
    }
}
