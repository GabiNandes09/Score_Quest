package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.usecase.GetGamesUseCase
import com.rogue.scorequest.domain.usecase.GetLastPlayedDatesUseCase
import com.rogue.scorequest.presentation.viewmodel.states.GamesFilterTab
import com.rogue.scorequest.presentation.viewmodel.states.GamesFilters
import com.rogue.scorequest.presentation.viewmodel.states.GamesSortOption
import com.rogue.scorequest.presentation.viewmodel.states.GamesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class GamesViewModel(
    getGames: GetGamesUseCase,
    getLastPlayedDates: GetLastPlayedDatesUseCase
) : ViewModel() {

    private val filters = MutableStateFlow(GamesFilters())

    val state = combine(getGames(), filters, getLastPlayedDates()) { games, filters, lastPlayed ->
        val filtered = games
            .filter { matchesTab(it, filters.tab) }
            .filter { filters.category == null || it.game.category == filters.category }
            .filter { filters.searchQuery.isBlank() || it.game.name.contains(filters.searchQuery, ignoreCase = true) }

        GamesState(
            isLoading = false,
            games = sortGames(filtered, filters.sort, lastPlayed),
            availableCategories = games.mapNotNull { it.game.category }.distinct().sorted(),
            filters = filters
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GamesState())

    fun onSearchQueryChange(query: String) {
        filters.value = filters.value.copy(searchQuery = query)
    }

    fun onTabSelected(tab: GamesFilterTab) {
        filters.value = filters.value.copy(tab = tab)
    }

    fun onCategorySelected(category: String?) {
        filters.value = filters.value.copy(category = category)
    }

    fun onSortSelected(sort: GamesSortOption) {
        filters.value = filters.value.copy(sort = sort)
    }

    private fun matchesTab(item: GameWithLibraryInfo, tab: GamesFilterTab): Boolean = when (tab) {
        GamesFilterTab.LIBRARY -> item.libraryEntry?.status == LibraryStatus.HAVE
        GamesFilterTab.WANTED -> item.libraryEntry?.status == LibraryStatus.WANT
        GamesFilterTab.PLAYED -> item.libraryEntry?.played == true
    }

    private fun sortGames(
        games: List<GameWithLibraryInfo>,
        sort: GamesSortOption,
        lastPlayed: Map<String, Long>
    ): List<GameWithLibraryInfo> = when (sort) {
        GamesSortOption.ALPHABETICAL_ASC ->
            games.sortedBy { it.game.name.lowercase() }

        GamesSortOption.ALPHABETICAL_DESC ->
            games.sortedByDescending { it.game.name.lowercase() }

        GamesSortOption.RECENTLY_PLAYED ->
            games.sortedWith(
                compareByDescending<GameWithLibraryInfo> { lastPlayed[it.game.id] ?: Long.MIN_VALUE }
                    .thenBy { it.game.name.lowercase() }
            )

        GamesSortOption.LEAST_RECENTLY_PLAYED -> {
            val (neverPlayed, played) = games.partition { lastPlayed[it.game.id] == null }
            neverPlayed.sortedBy { it.game.name.lowercase() } +
                played.sortedBy { lastPlayed[it.game.id] }
        }
    }
}
