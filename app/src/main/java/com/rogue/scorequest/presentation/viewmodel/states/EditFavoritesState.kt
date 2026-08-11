package com.rogue.scorequest.presentation.viewmodel.states

import com.rogue.scorequest.domain.model.BoardGame

data class EditFavoritesState(
    val allGames: List<BoardGame> = emptyList(),
    val favoriteIds: Set<String> = emptySet()
)
