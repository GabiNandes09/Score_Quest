package com.rogue.scorequest.di

import com.rogue.scorequest.data.repository.ActiveTimerRepository
import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.data.repository.GameScoreSchemaRepository
import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.data.repository.PlayerGroupRepository
import com.rogue.scorequest.data.repository.PlayerRepository
import com.rogue.scorequest.data.repository.ProfileRepository
import com.rogue.scorequest.domain.usecase.AddUserGameUseCase
import com.rogue.scorequest.domain.usecase.CalculateScoreFormulaUseCase
import com.rogue.scorequest.domain.usecase.CancelTimerUseCase
import com.rogue.scorequest.domain.usecase.ClearActiveTimerForGameUseCase
import com.rogue.scorequest.domain.usecase.CreatePlayerGroupUseCase
import com.rogue.scorequest.domain.usecase.CreatePlayerUseCase
import com.rogue.scorequest.domain.usecase.DeleteGameSessionUseCase
import com.rogue.scorequest.domain.usecase.DeletePlayerGroupUseCase
import com.rogue.scorequest.domain.usecase.DeletePlayerUseCase
import com.rogue.scorequest.domain.usecase.ExportGamesUseCase
import com.rogue.scorequest.domain.usecase.FindGroupWithExactMembersUseCase
import com.rogue.scorequest.domain.usecase.FinishTimerUseCase
import com.rogue.scorequest.domain.usecase.GetActiveTimerUseCase
import com.rogue.scorequest.domain.usecase.GetActivityHeatmapUseCase
import com.rogue.scorequest.domain.usecase.GetDuplicableSchemasUseCase
import com.rogue.scorequest.domain.usecase.GetDurationHistogramUseCase
import com.rogue.scorequest.domain.usecase.GetFavoriteGamesUseCase
import com.rogue.scorequest.domain.usecase.GetGameDetailUseCase
import com.rogue.scorequest.domain.usecase.GetGameScoreSchemaUseCase
import com.rogue.scorequest.domain.usecase.GetGameStatsUseCase
import com.rogue.scorequest.domain.usecase.GetGamesUseCase
import com.rogue.scorequest.domain.usecase.GetGroupStatsUseCase
import com.rogue.scorequest.domain.usecase.GetHomeStatsUseCase
import com.rogue.scorequest.domain.usecase.GetLastPlayedDatesUseCase
import com.rogue.scorequest.domain.usecase.GetPlayerGroupUseCase
import com.rogue.scorequest.domain.usecase.GetPlayerGroupsUseCase
import com.rogue.scorequest.domain.usecase.GetPlayerStatsUseCase
import com.rogue.scorequest.domain.usecase.GetPlayerUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.domain.usecase.GetProfileUseCase
import com.rogue.scorequest.domain.usecase.GetRecentSessionsUseCase
import com.rogue.scorequest.domain.usecase.GetSessionCountUseCase
import com.rogue.scorequest.domain.usecase.GetSessionDetailUseCase
import com.rogue.scorequest.domain.usecase.GetSessionsByMonthUseCase
import com.rogue.scorequest.domain.usecase.GetSessionsForGameUseCase
import com.rogue.scorequest.domain.usecase.GetSessionsPagedUseCase
import com.rogue.scorequest.domain.usecase.GetStreakUseCase
import com.rogue.scorequest.domain.usecase.GetHomeWidgetVisibilityUseCase
import com.rogue.scorequest.domain.usecase.GetThemePreferenceUseCase
import com.rogue.scorequest.domain.usecase.ImportSeedGamesUseCase
import com.rogue.scorequest.domain.usecase.PauseTimerUseCase
import com.rogue.scorequest.domain.usecase.RateGameUseCase
import com.rogue.scorequest.domain.usecase.ResumeTimerUseCase
import com.rogue.scorequest.domain.usecase.SaveGameScoreSchemaUseCase
import com.rogue.scorequest.domain.usecase.SaveGameSessionUseCase
import com.rogue.scorequest.domain.usecase.SetFavoriteGameUseCase
import com.rogue.scorequest.domain.usecase.SetLoanUseCase
import com.rogue.scorequest.domain.usecase.SetHomeWidgetVisibleUseCase
import com.rogue.scorequest.domain.usecase.SetThemePreferenceUseCase
import com.rogue.scorequest.domain.usecase.StartTimerUseCase
import com.rogue.scorequest.domain.usecase.UpdateGameSessionUseCase
import com.rogue.scorequest.domain.usecase.UpdateLibraryStatusUseCase
import com.rogue.scorequest.domain.usecase.UpdatePlayerGroupUseCase
import com.rogue.scorequest.domain.usecase.UpdatePlayerUseCase
import com.rogue.scorequest.domain.usecase.UpdateProfileUseCase
import com.rogue.scorequest.domain.usecase.UpdateUserGameUseCase
import org.koin.dsl.module

val appModule = module {

    // Repositories
    single { BoardGameRepository(get(), get()) }
    single { PlayerRepository(get()) }
    single { GameSessionRepository(get(), get(), get()) }
    single { ProfileRepository(get(), get()) }
    single { GameScoreSchemaRepository(get()) }
    single { ActiveTimerRepository(get()) }
    single { PlayerGroupRepository(get()) }

    // Use cases - jogos/estante
    factory { GetGamesUseCase(get()) }
    factory { GetGameDetailUseCase(get()) }
    factory { AddUserGameUseCase(get()) }
    factory { UpdateUserGameUseCase(get()) }
    factory { UpdateLibraryStatusUseCase(get()) }
    factory { SetLoanUseCase(get()) }
    factory { RateGameUseCase(get()) }
    factory { GetGameStatsUseCase(get()) }
    factory { GetSessionsForGameUseCase(get()) }
    factory { GetLastPlayedDatesUseCase(get()) }

    // Use cases - pontuação personalizada
    factory { GetGameScoreSchemaUseCase(get()) }
    factory { SaveGameScoreSchemaUseCase(get()) }
    factory { GetDuplicableSchemasUseCase(get(), get()) }
    factory { CalculateScoreFormulaUseCase() }

    // Use cases - jogadores
    factory { GetPlayersUseCase(get()) }
    factory { GetPlayerUseCase(get()) }
    factory { CreatePlayerUseCase(get()) }
    factory { UpdatePlayerUseCase(get()) }
    factory { DeletePlayerUseCase(get()) }
    factory { GetPlayerStatsUseCase(get()) }

    // Use cases - grupos de jogadores
    factory { GetPlayerGroupsUseCase(get()) }
    factory { GetPlayerGroupUseCase(get()) }
    factory { CreatePlayerGroupUseCase(get()) }
    factory { UpdatePlayerGroupUseCase(get()) }
    factory { DeletePlayerGroupUseCase(get()) }
    factory { FindGroupWithExactMembersUseCase(get()) }
    factory { GetGroupStatsUseCase(get()) }

    // Use cases - partidas
    factory { SaveGameSessionUseCase(get()) }
    factory { UpdateGameSessionUseCase(get()) }
    factory { DeleteGameSessionUseCase(get()) }
    factory { GetSessionsPagedUseCase(get()) }
    factory { GetSessionDetailUseCase(get()) }
    factory { GetRecentSessionsUseCase(get()) }
    factory { GetSessionCountUseCase(get()) }

    // Use cases - home/stats
    factory { GetStreakUseCase(get()) }
    factory { GetHomeStatsUseCase(get(), get()) }
    factory { GetActivityHeatmapUseCase(get()) }
    factory { GetSessionsByMonthUseCase(get()) }
    factory { GetDurationHistogramUseCase(get()) }

    // Use cases - perfil
    factory { GetProfileUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { GetFavoriteGamesUseCase(get()) }
    factory { SetFavoriteGameUseCase(get()) }

    // Use cases - config
    factory { GetThemePreferenceUseCase(get()) }
    factory { SetThemePreferenceUseCase(get()) }
    factory { GetHomeWidgetVisibilityUseCase(get()) }
    factory { SetHomeWidgetVisibleUseCase(get()) }
    factory { ImportSeedGamesUseCase(get(), get()) }
    factory { ExportGamesUseCase(get(), get()) }

    // Use cases - cronômetro de partida ao vivo
    factory { GetActiveTimerUseCase(get()) }
    factory { StartTimerUseCase(get()) }
    factory { PauseTimerUseCase(get()) }
    factory { ResumeTimerUseCase(get()) }
    factory { CancelTimerUseCase(get()) }
    factory { FinishTimerUseCase(get()) }
    factory { ClearActiveTimerForGameUseCase(get()) }
}
