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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimerSetupUiState(
    val selectedDurationMs: Long = 30 * 60 * 1000L,   // default 30 min
    val goalText: String = "",
    val isSessionActive: Boolean = false,
    val sessionRemainingMs: Long = 0L,
    val sessionTotalMs: Long = 0L,
    val sessionGoalText: String = "",
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

    private var tickerJob: Job? = null

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
        // Observe session persistence and wall-clock end time
        viewModelScope.launch {
            combine(
                userPreferences.isSessionActive,
                userPreferences.sessionEndTime,
                userPreferences.sessionDurationMs,
                userPreferences.sessionGoal
            ) { active, endTime, duration, goal ->
                val now = System.currentTimeMillis()
                val remaining = (endTime - now).coerceAtLeast(0L)
                val actuallyActive = active && remaining > 0

                _uiState.value = _uiState.value.copy(
                    isSessionActive = actuallyActive,
                    sessionRemainingMs = remaining,
                    sessionTotalMs = duration,
                    sessionGoalText = goal
                )

                if (actuallyActive) {
                    startActiveSessionTicker(endTime)
                } else {
                    tickerJob?.cancel()
                }
            }.collect {}
        }
    }

    private fun startActiveSessionTicker(endTime: Long) {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val remaining = (endTime - now).coerceAtLeast(0L)
                if (remaining <= 0) {
                    _uiState.value = _uiState.value.copy(
                        isSessionActive = false,
                        sessionRemainingMs = 0L
                    )
                    userPreferences.setSessionActive(false)
                    break
                }
                _uiState.value = _uiState.value.copy(
                    isSessionActive = true,
                    sessionRemainingMs = remaining
                )
                delay(1000L)
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
                    context.startService(
                        Intent().apply {
                            component = android.content.ComponentName(context.packageName, "com.blockme.app.service.LockdownOverlayService")
                        }
                    )
                    try {
                        context.startForegroundService(
                            Intent().apply {
                                component = android.content.ComponentName(context.packageName, "com.blockme.app.service.TimerForegroundService")
                            }
                        )
                    } catch (e: Exception) {
                        try {
                            context.startService(
                                Intent().apply {
                                    component = android.content.ComponentName(context.packageName, "com.blockme.app.service.TimerForegroundService")
                                }
                            )
                        } catch (e2: Exception) {
                            e2.printStackTrace()
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        isStarting = false,
                        isSessionActive = true,
                        sessionRemainingMs = session.durationMs,
                        sessionTotalMs = session.durationMs,
                        sessionGoalText = session.goalText
                    )
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
