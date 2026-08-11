package com.rogue.scorequest.presentation.viewmodel.states

data class EditProfileState(
    val displayName: String = "",
    val bio: String = "",
    val avatarUri: String? = null,
    val saved: Boolean = false
)
