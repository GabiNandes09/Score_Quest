package com.rogue.scorequest.di

import com.rogue.scorequest.data.repository.BoardGameRepository
import com.rogue.scorequest.data.repository.GameSessionRepository
import com.rogue.scorequest.data.repository.PlayerRepository
import com.rogue.scorequest.data.repository.ProfileRepository
import com.rogue.scorequest.domain.usecase.AddUserGameUseCase
import com.rogue.scorequest.domain.usecase.CreatePlayerUseCase
import com.rogue.scorequest.domain.usecase.DeleteGameSessionUseCase
import com.rogue.scorequest.domain.usecase.DeletePlayerUseCase
import com.rogue.scorequest.domain.usecase.GetActivityHeatmapUseCase
import com.rogue.scorequest.domain.usecase.GetDurationHistogramUseCase
import com.rogue.scorequest.domain.usecase.GetFavoriteGamesUseCase
import com.rogue.scorequest.domain.usecase.GetGameDetailUseCase
import com.rogue.scorequest.domain.usecase.GetGameStatsUseCase
import com.rogue.scorequest.domain.usecase.GetGamesUseCase
import com.rogue.scorequest.domain.usecase.GetHomeStatsUseCase
import com.rogue.scorequest.domain.usecase.GetLastPlayedDatesUseCase
import com.rogue.scorequest.domain.usecase.GetPlayersUseCase
import com.rogue.scorequest.domain.usecase.GetProfileUseCase
import com.rogue.scorequest.domain.usecase.GetRecentSessionsUseCase
import com.rogue.scorequest.domain.usecase.GetSessionCountUseCase
import com.rogue.scorequest.domain.usecase.GetSessionDetailUseCase
import com.rogue.scorequest.domain.usecase.GetSessionsByMonthUseCase
import com.rogue.scorequest.domain.usecase.GetSessionsForGameUseCase
import com.rogue.scorequest.domain.usecase.GetSessionsPagedUseCase
import com.rogue.scorequest.domain.usecase.GetStreakUseCase
import com.rogue.scorequest.domain.usecase.GetThemePreferenceUseCase
import com.rogue.scorequest.domain.usecase.RateGameUseCase
import com.rogue.scorequest.domain.usecase.SaveGameSessionUseCase
import com.rogue.scorequest.domain.usecase.SetFavoriteGameUseCase
import com.rogue.scorequest.domain.usecase.SetLoanUseCase
import com.rogue.scorequest.domain.usecase.SetThemePreferenceUseCase
import com.rogue.scorequest.domain.usecase.UpdateGameSessionUseCase
import com.rogue.scorequest.domain.usecase.UpdateLibraryStatusUseCase
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

    // Use cases - jogadores
    factory { GetPlayersUseCase(get()) }
    factory { CreatePlayerUseCase(get()) }
    factory { UpdatePlayerUseCase(get()) }
    factory { DeletePlayerUseCase(get()) }

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
}
