package com.rogue.scorequest.presentation.viewmodel.states

data class ShuffleOrderState(
    val selectedPlayerIds: Set<String> = emptySet(),
    val selectedGroupId: String? = null
)
