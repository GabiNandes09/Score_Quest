package com.rogue.scorequest.presentation.viewmodel.states

data class LoginState(
    val isSigningIn: Boolean = false,
    val signedIn: Boolean = false,
    val error: String? = null
)
