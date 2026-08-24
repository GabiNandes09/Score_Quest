package com.rogue.scorequest.presentation.viewmodel.states

data class ShuffleTeamsState(
    val selectedPlayerIds: Set<String> = emptySet(),
    val selectedGroupId: String? = null,
    val numberOfTeams: Int = 2
)
