package com.blockme.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockme.core.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val hapticEnabled: Boolean = true,
    val amoledMode: Boolean = false,
    val unlockChallengeEnabled: Boolean = false,
    val unlockChallengeType: String = "MATH"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        userPreferences.hapticEnabled,
        userPreferences.amoledMode,
        userPreferences.unlockChallengeEnabled,
        userPreferences.unlockChallengeType
    ) { haptic, amoled, challenge, challengeType ->
        SettingsUiState(
            hapticEnabled = haptic,
            amoledMode = amoled,
            unlockChallengeEnabled = challenge,
            unlockChallengeType = challengeType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setHapticEnabled(enabled) }
    }

    fun setAmoledMode(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setAmoledMode(enabled) }
    }

    fun setUnlockChallengeEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setUnlockChallengeEnabled(enabled) }
    }
}
