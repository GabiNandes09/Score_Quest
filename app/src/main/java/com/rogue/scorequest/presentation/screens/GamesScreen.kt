package com.rogue.scorequest.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.GameWithLibraryInfo
import com.rogue.scorequest.presentation.components.GameCoverImage
import com.rogue.scorequest.presentation.viewmodel.GamesViewModel
import com.rogue.scorequest.presentation.viewmodel.states.GamesFilterTab
import com.rogue.scorequest.presentation.viewmodel.states.GamesSortOption
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onGameClick: (String) -> Unit,
    onAddGameClick: () -> Unit,
    viewModel: GamesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jogos") },
                actions = {
                    IconButton(onClick = { searchExpanded = !searchExpanded }) {
                        Icon(Icons.Filled.Search, contentDescription = null)
                    }
                    TextButton(onClick = onAddGameClick) {
                        Text("Adicionar Jogo")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            AnimatedVisibility(
                visible = searchExpanded,
                enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(220)),
                exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(180))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.filters.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Buscar por nome") },
                            singleLine = true
                        )
                        SortDropdown(
                            selected = state.filters.sort,
                            onSelected = viewModel::onSortSelected
                        )
                    }

                    if (state.availableCategories.isNotEmpty()) {
                        CategoryFilterRow(
                            categories = state.availableCategories,
                            selected = state.filters.category,
                            onSelected = viewModel::onCategorySelected
                        )
                    }
                }
            }

            val tabIndex = when (state.filters.tab) {
                GamesFilterTab.LIBRARY -> 0
                GamesFilterTab.WANTED -> 1
                GamesFilterTab.PLAYED -> 2
            }
            SecondaryTabRow(selectedTabIndex = tabIndex) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { viewModel.onTabSelected(GamesFilterTab.LIBRARY) },
                    text = { Text("Estante", color = tabTextColor(tabIndex == 0)) }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { viewModel.onTabSelected(GamesFilterTab.WANTED) },
                    text = { Text("Desejo", color = tabTextColor(tabIndex == 1)) }
                )
                Tab(
                    selected = tabIndex == 2,
                    onClick = { viewModel.onTabSelected(GamesFilterTab.PLAYED) },
                    text = { Text("Jogado", color = tabTextColor(tabIndex == 2)) }
                )
            }

            AnimatedContent(
                targetState = state.filters.tab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 })
                        .togetherWith(fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { -it / 8 })
                },
                label = "games_tab_content"
            ) { _ ->
                if (!state.isLoading && state.games.isEmpty()) {
                    Text(
                        text = "Nenhum jogo encontrado",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.games) { item ->
                            GameGridItem(item = item, onClick = { onGameClick(item.game.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun tabTextColor(selected: Boolean) =
    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun SortDropdown(
    selected: GamesSortOption,
    onSelected: (GamesSortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(sortLabel(selected)) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            GamesSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(sortLabel(option)) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
        }
    }
}

private fun sortLabel(option: GamesSortOption): String = when (option) {
    GamesSortOption.ALPHABETICAL_ASC -> "A-Z"
    GamesSortOption.ALPHABETICAL_DESC -> "Z-A"
    GamesSortOption.RECENTLY_PLAYED -> "Recente ▼"
    GamesSortOption.LEAST_RECENTLY_PLAYED -> "▲ Recente"
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(selected ?: "Categoria") }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = { onSelected(null); expanded = false }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = { onSelected(category); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun GameGridItem(
    item: GameWithLibraryInfo,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            GameCoverImage(
                coverImageUrl = item.game.coverImageUrl,
                gameName = item.game.name,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = item.game.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
