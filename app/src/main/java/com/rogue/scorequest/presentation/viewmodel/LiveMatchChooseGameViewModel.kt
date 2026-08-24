package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.ActiveTimer
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import com.rogue.scorequest.domain.usecase.GetActiveTimerUseCase
import com.rogue.scorequest.domain.usecase.GetGamesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LiveMatchChooseGameViewModel(
    getGamesUseCase: GetGamesUseCase,
    getActiveTimerUseCase: GetActiveTimerUseCase
) : ViewModel() {

    val games: StateFlow<List<GameWithLibraryInfo>> = getGamesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTimer: StateFlow<ActiveTimer?> = getActiveTimerUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
