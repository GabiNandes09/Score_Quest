package com.rogue.scorequest.presentation.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.presentation.components.rememberSpinTicker
import com.rogue.scorequest.ui.theme.Gold
import kotlin.random.Random
import kotlinx.coroutines.launch

private val DICE_SIDES_OPTIONS = listOf(4, 6, 8, 10, 12, 20)
private const val MIN_DICE = 1
private const val MAX_DICE = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceRollerScreen(onBackClick: () -> Unit) {
    var diceCount by remember { mutableIntStateOf(2) }
    var sides by remember { mutableStateOf(6) }
    val ticker = rememberSpinTicker(diceCount, sides, randomValue = { List(diceCount) { Random.nextInt(1, sides + 1) } })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dados") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DICE_SIDES_OPTIONS.forEach { option ->
                    FilterChip(
                        selected = sides == option,
                        onClick = { sides = option },
                        label = { Text("d$option") }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { diceCount = (diceCount - 1).coerceAtLeast(MIN_DICE) }) {
                    Icon(Icons.Filled.Remove, contentDescription = null)
                }
                Text(
                    text = "$diceCount ${if (diceCount == 1) "dado" else "dados"}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { diceCount = (diceCount + 1).coerceAtMost(MAX_DICE) }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }

            ticker.current?.let { rolls ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rolls.chunked(5).forEach { rowValues ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowValues.forEach { value ->
                                Card(
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = "$value", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Total: ${rolls.sum()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )
            }

            Button(
                onClick = { scope.launch { ticker.spin(List(diceCount) { Random.nextInt(1, sides + 1) }) } },
                enabled = !ticker.isSpinning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rolar")
            }
        }
    }
}
