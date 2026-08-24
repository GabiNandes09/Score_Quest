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

    data object ScoreSchemaBuilder : Routes("score_schema_builder/{gameId}") {
        fun createRoute(gameId: String) = "score_schema_builder/$gameId"
    }

    data object SessionDetail : Routes("session_detail/{sessionId}") {
        fun createRoute(sessionId: String) = "session_detail/$sessionId"
    }

    data object EditProfile : Routes("edit_profile")
    data object EditFavorites : Routes("edit_favorites")
    data object ManagePlayers : Routes("manage_players")

    data object PlayerDetail : Routes("player_detail/{playerId}") {
        fun createRoute(playerId: String) = "player_detail/$playerId"
    }

    data object AddEditPlayer : Routes("add_edit_player/{playerId}") {
        const val NEW_PLAYER = "new"
        fun createRoute(playerId: String = NEW_PLAYER) = "add_edit_player/$playerId"
    }

    data object GroupDetail : Routes("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }

    data object AddEditGroup : Routes("add_edit_group/{groupId}") {
        const val NEW_GROUP = "new"
        fun createRoute(groupId: String = NEW_GROUP) = "add_edit_group/$groupId"
    }

    data object AddSessionWizardGraph : Routes("add_session_wizard/{sessionId}/{gameId}/{prefillDurationMinutes}") {
        const val NEW_SESSION = "new"
        const val NO_GAME = "none"
        const val NO_DURATION = "0"
        fun createRoute(sessionId: String = NEW_SESSION, gameId: String = NO_GAME, prefillDurationMinutes: String = NO_DURATION) =
            "add_session_wizard/$sessionId/$gameId/$prefillDurationMinutes"
    }

    data object WizardChooseGame : Routes("add_session_wizard/{sessionId}/{gameId}/{prefillDurationMinutes}/choose_game")
    data object WizardSessionData : Routes("add_session_wizard/{sessionId}/{gameId}/{prefillDurationMinutes}/session_data")
    data object WizardPlayers : Routes("add_session_wizard/{sessionId}/{gameId}/{prefillDurationMinutes}/players")
    data object WizardScoring : Routes("add_session_wizard/{sessionId}/{gameId}/{prefillDurationMinutes}/scoring")
    data object WizardCompositeScoring : Routes("add_session_wizard/{sessionId}/{gameId}/{prefillDurationMinutes}/composite_scoring")
    data object WizardRankingScoring : Routes("add_session_wizard/{sessionId}/{gameId}/{prefillDurationMinutes}/ranking_scoring")
    data object WizardConfirm : Routes("add_session_wizard/{sessionId}/{gameId}/{prefillDurationMinutes}/confirm")

    data object LiveMatch : Routes("live_match/{gameId}") {
        fun createRoute(gameId: String) = "live_match/$gameId"
    }

    data object LiveMatchChooseGame : Routes("live_match_choose_game")

    data object Tools : Routes("tools")
    data object CoinFlip : Routes("tools/coin_flip")
    data object RandomNumber : Routes("tools/random_number")
    data object RandomLetter : Routes("tools/random_letter")
    data object PickNames : Routes("tools/pick_names")
    data object ShuffleOrder : Routes("tools/shuffle_order")
    data object ShuffleTeams : Routes("tools/shuffle_teams")
    data object AssignRoles : Routes("tools/assign_roles")
    data object DiceRoller : Routes("tools/dice_roller")
    data object SpinWheel : Routes("tools/spin_wheel")
    data object ScratchScoreboard : Routes("tools/scratch_scoreboard")
    data object TurnTimer : Routes("tools/turn_timer")
    data object FingerPicker : Routes("tools/finger_picker")
}
