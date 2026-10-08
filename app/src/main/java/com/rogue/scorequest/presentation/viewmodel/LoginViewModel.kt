package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.domain.usecase.SignInWithGoogleUseCase
import com.rogue.scorequest.presentation.viewmodel.states.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val signInWithGoogle: SignInWithGoogleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onSignInStarted() {
        _state.update { it.copy(isSigningIn = true, error = null) }
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        viewModelScope.launch {
            runCatching { signInWithGoogle(idToken) }
                .onSuccess { _state.update { it.copy(isSigningIn = false, signedIn = true) } }
                .onFailure { e ->
                    _state.update { it.copy(isSigningIn = false, error = e.message ?: "Falha ao entrar com Google.") }
                }
        }
    }

    fun onSignInError(message: String) {
        _state.update { it.copy(isSigningIn = false, error = message) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}
