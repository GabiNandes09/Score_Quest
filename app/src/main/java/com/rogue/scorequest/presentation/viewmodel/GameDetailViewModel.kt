package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.usecase.GetGameDetailUseCase
import com.rogue.scorequest.domain.usecase.GetGameScoreSchemaUseCase
import com.rogue.scorequest.domain.usecase.GetGameStatsUseCase
import com.rogue.scorequest.domain.usecase.GetSessionsForGameUseCase
import com.rogue.scorequest.domain.usecase.RateGameUseCase
import com.rogue.scorequest.domain.usecase.SetLoanUseCase
import com.rogue.scorequest.domain.usecase.UpdateLibraryStatusUseCase
import com.rogue.scorequest.presentation.viewmodel.states.GameDetailState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameDetailViewModel(
    private val gameId: String,
    getGameDetail: GetGameDetailUseCase,
    getGameStats: GetGameStatsUseCase,
    getSessionsForGame: GetSessionsForGameUseCase,
    getGameScoreSchema: GetGameScoreSchemaUseCase,
    private val updateLibraryStatusUseCase: UpdateLibraryStatusUseCase,
    private val setLoanUseCase: SetLoanUseCase,
    private val rateGameUseCase: RateGameUseCase
) : ViewModel() {

    val state = combine(
        getGameDetail(gameId),
        getGameStats(gameId),
        getSessionsForGame(gameId),
        getGameScoreSchema(gameId)
    ) { game, stats, sessions, schema ->
        GameDetailState(isLoading = false, game = game, stats = stats, sessions = sessions, hasScoreSchema = schema != null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameDetailState())

    fun onStatusChange(status: LibraryStatus) {
        viewModelScope.launch { updateLibraryStatusUseCase(gameId, status) }
    }

    fun onLoanChange(lentTo: String?) {
        viewModelScope.launch { setLoanUseCase(gameId, lentTo) }
    }

    fun onRatingChange(rating: Int) {
        viewModelScope.launch { rateGameUseCase(gameId, rating) }
    }
}
