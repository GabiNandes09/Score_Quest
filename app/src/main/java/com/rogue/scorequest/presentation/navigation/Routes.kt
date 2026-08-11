package com.rogue.scorequest.presentation.navigation

sealed class Routes(
    val route: String
) {

    data object Home : Routes("home")
    data object Games : Routes("games")
    data object Profile : Routes("profile")
    data object Settings : Routes("settings")

    data object GameDetail : Routes("game_detail/{gameId}") {
        fun createRoute(gameId: String) = "game_detail/$gameId"
    }

    data object AddEditGame : Routes("add_edit_game/{gameId}") {
        const val NEW_GAME = "new"
        fun createRoute(gameId: String = NEW_GAME) = "add_edit_game/$gameId"
    }

    data object SessionDetail : Routes("session_detail/{sessionId}") {
        fun createRoute(sessionId: String) = "session_detail/$sessionId"
    }

    data object EditProfile : Routes("edit_profile")
    data object EditFavorites : Routes("edit_favorites")
    data object ManagePlayers : Routes("manage_players")

    data object AddSessionWizardGraph : Routes("add_session_wizard/{sessionId}/{gameId}") {
        const val NEW_SESSION = "new"
        const val NO_GAME = "none"
        fun createRoute(sessionId: String = NEW_SESSION, gameId: String = NO_GAME) =
            "add_session_wizard/$sessionId/$gameId"
    }

    data object WizardChooseGame : Routes("add_session_wizard/{sessionId}/{gameId}/choose_game")
    data object WizardSessionData : Routes("add_session_wizard/{sessionId}/{gameId}/session_data")
    data object WizardPlayers : Routes("add_session_wizard/{sessionId}/{gameId}/players")
    data object WizardScoring : Routes("add_session_wizard/{sessionId}/{gameId}/scoring")
    data object WizardConfirm : Routes("add_session_wizard/{sessionId}/{gameId}/confirm")
}
