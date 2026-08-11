package com.rogue.scorequest.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.R
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.presentation.screens.AddEditGameScreen
import com.rogue.scorequest.presentation.screens.EditFavoritesScreen
import com.rogue.scorequest.presentation.screens.EditProfileScreen
import com.rogue.scorequest.presentation.screens.GameDetailScreen
import com.rogue.scorequest.presentation.screens.GamesScreen
import com.rogue.scorequest.presentation.screens.HomeScreen
import com.rogue.scorequest.presentation.screens.ManagePlayersScreen
import com.rogue.scorequest.presentation.screens.ProfileScreen
import com.rogue.scorequest.presentation.screens.SessionDetailScreen
import com.rogue.scorequest.presentation.screens.SettingsScreen
import com.rogue.scorequest.presentation.screens.scoreschema.ScoreSchemaBuilderScreen
import com.rogue.scorequest.presentation.screens.wizard.ChooseGameStep
import com.rogue.scorequest.presentation.screens.wizard.CompositeScoringStep
import com.rogue.scorequest.presentation.screens.wizard.ConfirmStep
import com.rogue.scorequest.presentation.screens.wizard.PlayersStep
import com.rogue.scorequest.presentation.screens.wizard.ScoringStep
import com.rogue.scorequest.presentation.screens.wizard.SessionDataStep
import com.rogue.scorequest.presentation.viewmodel.AddSessionViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private val mainTabRoutes = setOf(Routes.Home.route, Routes.Games.route, Routes.Profile.route, Routes.Settings.route)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBarAndFab = currentRoute in mainTabRoutes
    val showFab = showBottomBarAndFab && currentRoute != Routes.Home.route

    Scaffold(
        bottomBar = {
            if (showBottomBarAndFab) {
                ScoreQuestBottomBar(currentRoute = currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.AddSessionWizardGraph.createRoute()) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_session_fab_description)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Home.route) {
                HomeScreen(
                    onRegisterSessionClick = { navController.navigate(Routes.AddSessionWizardGraph.createRoute()) },
                    onGameClick = { gameId -> navController.navigate(Routes.GameDetail.createRoute(gameId)) }
                )
            }

            composable(Routes.Games.route) {
                GamesScreen(
                    onGameClick = { gameId -> navController.navigate(Routes.GameDetail.createRoute(gameId)) },
                    onAddGameClick = { navController.navigate(Routes.AddEditGame.createRoute()) }
                )
            }

            composable(Routes.Profile.route) {
                ProfileScreen(
                    onSessionClick = { sessionId -> navController.navigate(Routes.SessionDetail.createRoute(sessionId)) },
                    onEditProfileClick = { navController.navigate(Routes.EditProfile.route) },
                    onEditFavoritesClick = { navController.navigate(Routes.EditFavorites.route) }
                )
            }

            composable(Routes.Settings.route) {
                SettingsScreen(
                    onManagePlayersClick = { navController.navigate(Routes.ManagePlayers.route) }
                )
            }

            composable(
                route = Routes.GameDetail.route,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) { entry ->
                val gameId = entry.arguments?.getString("gameId").orEmpty()
                GameDetailScreen(
                    gameId = gameId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate(Routes.AddEditGame.createRoute(gameId)) },
                    onRegisterSessionClick = {
                        navController.navigate(Routes.AddSessionWizardGraph.createRoute(gameId = gameId))
                    },
                    onSessionClick = { sessionId -> navController.navigate(Routes.SessionDetail.createRoute(sessionId)) },
                    onConfigureScoreClick = { navController.navigate(Routes.ScoreSchemaBuilder.createRoute(gameId)) }
                )
            }

            composable(
                route = Routes.ScoreSchemaBuilder.route,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) { entry ->
                val gameId = entry.arguments?.getString("gameId").orEmpty()
                ScoreSchemaBuilderScreen(
                    gameId = gameId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.AddEditGame.route,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) { entry ->
                val gameId = entry.arguments?.getString("gameId") ?: Routes.AddEditGame.NEW_GAME
                AddEditGameScreen(
                    gameId = gameId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.SessionDetail.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { entry ->
                val sessionId = entry.arguments?.getString("sessionId").orEmpty()
                SessionDetailScreen(
                    sessionId = sessionId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = {
                        navController.navigate(Routes.AddSessionWizardGraph.createRoute(sessionId)) {
                            popUpTo(Routes.SessionDetail.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.EditProfile.route) {
                EditProfileScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Routes.EditFavorites.route) {
                EditFavoritesScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Routes.ManagePlayers.route) {
                ManagePlayersScreen(onBackClick = { navController.popBackStack() })
            }

            val wizardArgs: List<NamedNavArgument> = listOf(
                navArgument("sessionId") { type = NavType.StringType },
                navArgument("gameId") { type = NavType.StringType }
            )

            navigation(
                route = Routes.AddSessionWizardGraph.route,
                startDestination = Routes.WizardChooseGame.route
            ) {
                composable(
                    route = Routes.WizardChooseGame.route,
                    arguments = wizardArgs
                ) { entry ->
                    val viewModel = wizardViewModel(entry, navController)
                    ChooseGameStep(
                        viewModel = viewModel,
                        onNext = { navController.navigate(routeForStep(Routes.WizardSessionData.route, viewModel)) },
                        onCancel = { navController.popBackStack(Routes.AddSessionWizardGraph.route, true) }
                    )
                }

                composable(
                    route = Routes.WizardSessionData.route,
                    arguments = wizardArgs
                ) { entry ->
                    val viewModel = wizardViewModel(entry, navController)
                    SessionDataStep(
                        viewModel = viewModel,
                        onNext = { navController.navigate(routeForStep(Routes.WizardPlayers.route, viewModel)) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Routes.WizardPlayers.route,
                    arguments = wizardArgs
                ) { entry ->
                    val viewModel = wizardViewModel(entry, navController)
                    val schema by viewModel.schema.collectAsStateWithLifecycle()
                    PlayersStep(
                        viewModel = viewModel,
                        onNext = {
                            val nextRoute = if (schema?.type == ScoreSchemaType.COMPOSITE) {
                                Routes.WizardCompositeScoring.route
                            } else {
                                Routes.WizardScoring.route
                            }
                            navController.navigate(routeForStep(nextRoute, viewModel))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Routes.WizardScoring.route,
                    arguments = wizardArgs
                ) { entry ->
                    val viewModel = wizardViewModel(entry, navController)
                    ScoringStep(
                        viewModel = viewModel,
                        onNext = { navController.navigate(routeForStep(Routes.WizardConfirm.route, viewModel)) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Routes.WizardCompositeScoring.route,
                    arguments = wizardArgs
                ) { entry ->
                    val viewModel = wizardViewModel(entry, navController)
                    CompositeScoringStep(
                        viewModel = viewModel,
                        onNext = { navController.navigate(routeForStep(Routes.WizardConfirm.route, viewModel)) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Routes.WizardConfirm.route,
                    arguments = wizardArgs
                ) { entry ->
                    val viewModel = wizardViewModel(entry, navController)
                    ConfirmStep(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onSaved = {
                            navController.popBackStack(Routes.AddSessionWizardGraph.route, true)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun wizardViewModel(
    entry: NavBackStackEntry,
    navController: NavHostController
): AddSessionViewModel {
    val parentEntry = remember(entry) { navController.getBackStackEntry(Routes.AddSessionWizardGraph.route) }
    val sessionId = parentEntry.arguments?.getString("sessionId") ?: Routes.AddSessionWizardGraph.NEW_SESSION
    val gameId = parentEntry.arguments?.getString("gameId") ?: Routes.AddSessionWizardGraph.NO_GAME
    return koinViewModel(viewModelStoreOwner = parentEntry) { parametersOf(sessionId, gameId) }
}

private fun routeForStep(stepRoute: String, viewModel: AddSessionViewModel): String =
    stepRoute
        .replace("{sessionId}", viewModel.sessionId)
        .replace("{gameId}", viewModel.initialGameId)

@Composable
private fun ScoreQuestBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.Home.route,
            onClick = { onNavigate(Routes.Home.route) },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.Games.route,
            onClick = { onNavigate(Routes.Games.route) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_games)) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.Profile.route,
            onClick = { onNavigate(Routes.Profile.route) },
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_profile)) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.Settings.route,
            onClick = { onNavigate(Routes.Settings.route) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_settings)) }
        )
    }
}
