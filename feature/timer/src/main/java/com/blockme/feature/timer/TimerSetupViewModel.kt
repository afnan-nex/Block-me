package com.blockme.feature.timer

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockme.core.common.Constants
import com.blockme.core.data.local.preferences.UserPreferences
import com.blockme.core.domain.model.FocusGoal
import com.blockme.core.domain.repository.GoalRepository
import com.blockme.core.domain.usecase.StartSessionUseCase
import com.blockme.core.common.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimerSetupUiState(
    val selectedDurationMs: Long = 30 * 60 * 1000L,   // default 30 min
    val goalText: String = "",
    val isSessionActive: Boolean = false,
    val isStarting: Boolean = false,
    val errorMessage: String? = null,
    val recentGoals: List<FocusGoal> = emptyList()
)

@HiltViewModel
class TimerSetupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val startSessionUseCase: StartSessionUseCase,
    private val userPreferences: UserPreferences,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerSetupUiState())
    val uiState: StateFlow<TimerSetupUiState> = _uiState.asStateFlow()

    val isSessionActive = userPreferences.isSessionActive.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    init {
        // Load last used duration
        viewModelScope.launch {
            userPreferences.lastDurationMs.collect { lastDuration ->
                _uiState.value = _uiState.value.copy(selectedDurationMs = lastDuration)
            }
        }
        // Load recent goals
        viewModelScope.launch {
            goalRepository.getRecentGoals(5).collect { goals ->
                _uiState.value = _uiState.value.copy(recentGoals = goals)
            }
        }
        // Keep session active state in sync
        viewModelScope.launch {
            userPreferences.isSessionActive.collect { active ->
                _uiState.value = _uiState.value.copy(isSessionActive = active)
            }
        }
    }

    fun onDurationChanged(durationMs: Long) {
        if (durationMs > Constants.MAX_SESSION_DURATION_MS) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Maximum 3 hours allowed"
            )
            return
        }
        _uiState.value = _uiState.value.copy(
            selectedDurationMs = durationMs,
            errorMessage = null
        )
    }

    fun onGoalTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(goalText = text)
    }

    fun onStartSession() {
        val state = _uiState.value
        if (state.isStarting) return

        _uiState.value = state.copy(isStarting = true, errorMessage = null)

        viewModelScope.launch {
            val result = startSessionUseCase(
                durationMs = state.selectedDurationMs,
                goalText = state.goalText
            )
            when (result) {
                is Result.Success -> {
                    val session = result.data
                    // Write to DataStore for service persistence
                    userPreferences.startSession(
                        endTimeMs = session.endTime,
                        startTimeMs = session.startTime,
                        durationMs = session.durationMs,
                        goal = session.goalText,
                        sessionId = session.id
                    )
                    // Start services
                    context.startForegroundService(
                        Intent(context, com.blockme.app.service.LockdownOverlayService::class.java)
                    )
                    context.startForegroundService(
                        Intent(context, com.blockme.app.service.TimerForegroundService::class.java)
                    )
                    _uiState.value = _uiState.value.copy(isStarting = false)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isStarting = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }
}
