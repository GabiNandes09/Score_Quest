package com.rogue.scorequest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogue.scorequest.data.cloud.CloudFriend
import com.rogue.scorequest.data.cloud.CloudFriendRequest
import com.rogue.scorequest.domain.model.FriendRequestResult
import com.rogue.scorequest.domain.usecase.GetAuthStateUseCase
import com.rogue.scorequest.domain.usecase.GetFriendsUseCase
import com.rogue.scorequest.domain.usecase.GetIncomingFriendRequestsUseCase
import com.rogue.scorequest.domain.usecase.GetOrCreatePlayerForFriendUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.domain.usecase.GetPublicUsernameUseCase
import com.rogue.scorequest.domain.usecase.RemoveFriendUseCase
import com.rogue.scorequest.domain.usecase.RespondToFriendRequestUseCase
import com.rogue.scorequest.domain.usecase.SearchUserByUsernameUseCase
import com.rogue.scorequest.domain.usecase.SendFriendRequestUseCase
import com.rogue.scorequest.presentation.viewmodel.states.FriendsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FriendsViewModel(
    getAuthState: GetAuthStateUseCase,
    getPlayers: GetPlayersUseCase,
    private val searchUserByUsername: SearchUserByUsernameUseCase,
    private val getMyUsername: GetPublicUsernameUseCase,
    private val sendFriendRequest: SendFriendRequestUseCase,
    private val getIncomingFriendRequests: GetIncomingFriendRequestsUseCase,
    private val respondToFriendRequest: RespondToFriendRequestUseCase,
    private val getFriends: GetFriendsUseCase,
    private val removeFriend: RemoveFriendUseCase,
    private val getOrCreatePlayerForFriend: GetOrCreatePlayerForFriendUseCase
) : ViewModel() {

    private val authUser = getAuthState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val players = getPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Incrementado depois de aceitar/recusar/remover — reexecuta a carga sob demanda
    // (requests/friends vêm de leituras avulsas do Firestore, não de um listener vivo).
    private val refreshTrigger = MutableStateFlow(0)

    private val _state = MutableStateFlow(FriendsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(authUser, refreshTrigger) { user, _ -> user }.collect { user ->
                if (user == null) {
                    _state.value = FriendsState()
                    return@collect
                }
                _state.update { it.copy(isLoggedIn = true, isLoading = true) }
                // Sessão Firebase fica logada localmente mesmo offline — sem conexão,
                // mostra mensagem em vez de deixar a exceção do Firestore subir sem tratamento.
                runCatching { getIncomingFriendRequests(user.uid) to getFriends(user.uid) }
                    .onSuccess { (requests, friendsList) ->
                        _state.update { it.copy(isLoading = false, incomingRequests = requests, friends = friendsList) }
                    }
                    .onFailure {
                        _state.update {
                            it.copy(isLoading = false, sendRequestMessage = "Sem conexão — não foi possível carregar amigos agora.")
                        }
                    }
            }
        }
        viewModelScope.launch {
            players.collect { list ->
                val linked = list.mapNotNull { it.linkedUserId }.toSet()
                _state.update { it.copy(linkedFriendUids = linked) }
            }
        }
    }

    private fun refresh() {
        refreshTrigger.update { it + 1 }
    }

    fun onSearchQueryChange(value: String) {
        _state.update { it.copy(searchQuery = value, searchResult = null, searchError = null) }
    }

    fun onSearchClick() {
        val query = _state.value.searchQuery.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, searchError = null, searchResult = null) }
            runCatching { searchUserByUsername(query) }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isSearching = false,
                            searchResult = result,
                            searchError = if (result == null) "Usuário \"@$query\" não encontrado" else null
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isSearching = false, searchError = "Sem conexão — não foi possível buscar agora.") }
                }
        }
    }

    fun onSendRequestClick() {
        val target = _state.value.searchResult ?: return
        val me = authUser.value ?: return
        viewModelScope.launch {
            runCatching {
                val myUsername = getMyUsername(me.uid) ?: return@runCatching null
                sendFriendRequest(me, myUsername, target)
            }.onSuccess { result ->
                if (result == null) return@launch
                _state.update {
                    it.copy(
                        sendRequestMessage = result.toMessage(target.username),
                        searchResult = if (result == FriendRequestResult.SENT) null else it.searchResult,
                        searchQuery = if (result == FriendRequestResult.SENT) "" else it.searchQuery
                    )
                }
            }.onFailure {
                _state.update { it.copy(sendRequestMessage = "Sem conexão — não foi possível enviar a solicitação.") }
            }
        }
    }

    fun dismissSendRequestMessage() {
        _state.update { it.copy(sendRequestMessage = null) }
    }

    fun onAcceptClick(request: CloudFriendRequest) {
        viewModelScope.launch {
            runCatching { respondToFriendRequest(request, accept = true) }
                .onSuccess { refresh() }
                .onFailure { _state.update { it.copy(sendRequestMessage = "Sem conexão — não foi possível aceitar agora.") } }
        }
    }

    fun onDeclineClick(request: CloudFriendRequest) {
        viewModelScope.launch {
            runCatching { respondToFriendRequest(request, accept = false) }
                .onSuccess { refresh() }
                .onFailure { _state.update { it.copy(sendRequestMessage = "Sem conexão — não foi possível recusar agora.") } }
        }
    }

    fun onRemoveFriendClick(friend: CloudFriend) {
        val me = authUser.value ?: return
        viewModelScope.launch {
            runCatching { removeFriend(me.uid, friend.uid) }
                .onSuccess { refresh() }
                .onFailure { _state.update { it.copy(sendRequestMessage = "Sem conexão — não foi possível remover agora.") } }
        }
    }

    fun onAddAsPlayerClick(friend: CloudFriend) {
        viewModelScope.launch { getOrCreatePlayerForFriend(friend) }
    }
}

private fun FriendRequestResult.toMessage(username: String): String = when (this) {
    FriendRequestResult.SENT -> "Solicitação enviada pra @$username"
    FriendRequestResult.ALREADY_FRIENDS -> "Vocês já são amigos"
    FriendRequestResult.ALREADY_PENDING -> "Já existe uma solicitação pendente com @$username"
    FriendRequestResult.CANNOT_ADD_SELF -> "Você não pode adicionar a si mesmo"
}
