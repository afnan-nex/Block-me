package com.blockme.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockme.core.domain.model.SessionStats
import com.blockme.core.domain.usecase.GetSessionStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    getSessionStatsUseCase: GetSessionStatsUseCase
) : ViewModel() {

    val stats: StateFlow<SessionStats> = getSessionStatsUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SessionStats()
        )
}
