package com.rogue.scorequest.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.presentation.navigation.Routes
import com.rogue.scorequest.ui.theme.Gold

private data class ToolDestination(val route: String, val label: String, val icon: ImageVector)

private val toolDestinations = listOf(
    ToolDestination(Routes.PickNames.route, "Sorteio por nome", Icons.Filled.Person),
    ToolDestination(Routes.ShuffleOrder.route, "Ordem de turno", Icons.Filled.Reorder),
    ToolDestination(Routes.ShuffleTeams.route, "Sorteio de equipes", Icons.Filled.Groups),
    ToolDestination(Routes.AssignRoles.route, "Sorteio de papéis", Icons.Filled.TheaterComedy),
    ToolDestination(Routes.CoinFlip.route, "Moeda", Icons.Filled.MonetizationOn),
    ToolDestination(Routes.RandomNumber.route, "Número aleatório", Icons.Filled.Numbers),
    ToolDestination(Routes.RandomLetter.route, "Letra aleatória", Icons.Filled.SortByAlpha),
    ToolDestination(Routes.DiceRoller.route, "Dados", Icons.Filled.Casino),
    ToolDestination(Routes.SpinWheel.route, "Roleta", Icons.Filled.PieChart),
    ToolDestination(Routes.ScratchScoreboard.route, "Placar avulso", Icons.Filled.Scoreboard),
    ToolDestination(Routes.TurnTimer.route, "Cronômetro por turno", Icons.Filled.HourglassBottom),
    ToolDestination(Routes.FingerPicker.route, "Dedo na tela", Icons.Filled.TouchApp)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(onToolClick: (String) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Extras") }) }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(toolDestinations) { tool ->
                ToolCard(tool = tool, onClick = { onToolClick(tool.route) })
            }
        }
    }
}

@Composable
private fun ToolCard(tool: ToolDestination, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(text = tool.label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
