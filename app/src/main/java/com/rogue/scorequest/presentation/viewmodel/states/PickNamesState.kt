package com.rogue.scorequest.presentation.viewmodel.states

data class PickNamesState(
    val selectedPlayerIds: Set<String> = emptySet(),
    val selectedGroupId: String? = null
)
