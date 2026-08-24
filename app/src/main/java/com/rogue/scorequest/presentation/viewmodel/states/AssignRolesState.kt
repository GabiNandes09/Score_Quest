package com.rogue.scorequest.presentation.viewmodel.states

data class AssignRolesState(
    val selectedPlayerIds: Set<String> = emptySet(),
    val selectedGroupId: String? = null,
    val roles: List<String> = emptyList()
)
