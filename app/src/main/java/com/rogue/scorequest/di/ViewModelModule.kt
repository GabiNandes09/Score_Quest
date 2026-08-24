package com.rogue.scorequest.di

import com.rogue.scorequest.presentation.viewmodel.AddEditGameViewModel
import com.rogue.scorequest.presentation.viewmodel.AddEditGroupViewModel
import com.rogue.scorequest.presentation.viewmodel.AddEditPlayerViewModel
import com.rogue.scorequest.presentation.viewmodel.AddSessionViewModel
import com.rogue.scorequest.presentation.viewmodel.EditFavoritesViewModel
import com.rogue.scorequest.presentation.viewmodel.EditProfileViewModel
import com.rogue.scorequest.presentation.viewmodel.GameDetailViewModel
import com.rogue.scorequest.presentation.viewmodel.GamesViewModel
import com.rogue.scorequest.presentation.viewmodel.GroupDetailViewModel
import com.rogue.scorequest.presentation.viewmodel.HomeViewModel
import com.rogue.scorequest.presentation.viewmodel.LiveMatchChooseGameViewModel
import com.rogue.scorequest.presentation.viewmodel.LiveMatchViewModel
import com.rogue.scorequest.presentation.viewmodel.ManagePlayersViewModel
import com.rogue.scorequest.presentation.viewmodel.PlayerDetailViewModel
import com.rogue.scorequest.presentation.viewmodel.ProfileViewModel
import com.rogue.scorequest.presentation.viewmodel.ScoreSchemaBuilderViewModel
import com.rogue.scorequest.presentation.viewmodel.SessionDetailViewModel
import com.rogue.scorequest.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { (sessionId: String, gameId: String, prefillDurationMinutes: String) -> AddSessionViewModel(sessionId, gameId, prefillDurationMinutes, get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { GamesViewModel(get(), get()) }
    viewModel { (gameId: String) -> GameDetailViewModel(gameId, get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (gameId: String) -> LiveMatchViewModel(gameId, get(), get(), get(), get(), get(), get(), get()) }
    viewModel { LiveMatchChooseGameViewModel(get(), get()) }
    viewModel { (gameId: String) -> AddEditGameViewModel(gameId, get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { (sessionId: String) -> SessionDetailViewModel(sessionId, get(), get(), get()) }
    viewModel { EditProfileViewModel(get(), get()) }
    viewModel { EditFavoritesViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
    viewModel { ManagePlayersViewModel(get(), get()) }
    viewModel { (playerId: String) -> PlayerDetailViewModel(playerId, get(), get()) }
    viewModel { (playerId: String) -> AddEditPlayerViewModel(playerId, get(), get(), get(), get()) }
    viewModel { (groupId: String) -> GroupDetailViewModel(groupId, get(), get(), get()) }
    viewModel { (groupId: String) -> AddEditGroupViewModel(groupId, get(), get(), get(), get(), get()) }
    viewModel { (gameId: String) -> ScoreSchemaBuilderViewModel(gameId, get(), get(), get(), get(), get()) }
}
