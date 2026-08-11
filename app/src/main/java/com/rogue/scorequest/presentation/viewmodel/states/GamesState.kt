package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.GameWithLibraryInfo

enum class GamesFilterTab {
    LIBRARY, WANTED, PLAYED
}

enum class GamesSortOption {
    ALPHABETICAL_ASC, ALPHABETICAL_DESC, RECENTLY_PLAYED, LEAST_RECENTLY_PLAYED
}

data class GamesFilters(
    val searchQuery: String = "",
    val tab: GamesFilterTab = GamesFilterTab.LIBRARY,
    val category: String? = null,
    val sort: GamesSortOption = GamesSortOption.ALPHABETICAL_ASC
)

data class GamesState(
    val isLoading: Boolean = true,
    val games: List<GameWithLibraryInfo> = emptyList(),
    val availableCategories: List<String> = emptyList(),
    val filters: GamesFilters = GamesFilters()
)
