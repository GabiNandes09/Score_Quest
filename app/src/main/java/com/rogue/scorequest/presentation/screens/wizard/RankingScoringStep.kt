package com.rogue.scorequest.presentation.screens.wizard

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.presentation.components.PlayerIdentityRow
import com.rogue.scorequest.presentation.viewmodel.AddSessionViewModel
import com.rogue.scorequest.ui.theme.Gold
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScoringStep(
    viewModel: AddSessionViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val schema by viewModel.schema.collectAsStateWithLifecycle()
    val hasPointsField = schema?.fields?.isNotEmpty() == true

    LaunchedEffect(Unit) {
        viewModel.ensureRankingOrderInitialized()
    }

    val orderedPlayers = remember(state.rankingOrder, players) {
        state.rankingOrder.mapNotNull { id -> players.find { it.id == id } }
    }

    var rowHeightPx by remember { mutableFloatStateOf(0f) }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ranking") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "Arraste pela alça pra ordenar do 1º ao último lugar (ou use as setas).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(orderedPlayers, key = { _, player -> player.id }) { index, player ->
                    val isDragging = player.id == draggingId

                    RankingRow(
                        position = index + 1,
                        player = player,
                        pointsValue = state.rankingPoints[player.id].orEmpty(),
                        showPoints = hasPointsField,
                        onPointsChange = { viewModel.onRankingPointsChanged(player.id, it) },
                        canMoveUp = index > 0,
                        canMoveDown = index < orderedPlayers.lastIndex,
                        onMoveUp = { viewModel.onMoveRankingUp(index) },
                        onMoveDown = { viewModel.onMoveRankingDown(index) },
                        modifier = Modifier
                            .onGloballyPositioned { coordinates -> rowHeightPx = coordinates.size.height.toFloat() }
                            .offset { IntOffset(0, if (isDragging) dragOffsetY.roundToInt() else 0) }
                            .zIndex(if (isDragging) 1f else 0f)
                            .then(if (isDragging) Modifier else Modifier.animateItem()),
                        dragHandleModifier = Modifier.pointerInput(player.id) {
                            detectDragGestures(
                                onDragStart = {
                                    draggingId = player.id
                                    dragOffsetY = 0f
                                },
                                onDragEnd = {
                                    draggingId = null
                                    dragOffsetY = 0f
                                },
                                onDragCancel = {
                                    draggingId = null
                                    dragOffsetY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetY += dragAmount.y
                                    val threshold = rowHeightPx
                                    if (threshold > 0f) {
                                        val currentOrder = viewModel.state.value.rankingOrder
                                        val currentIndex = currentOrder.indexOf(player.id)
                                        when {
                                            dragOffsetY > threshold / 2 && currentIndex < currentOrder.lastIndex -> {
                                                val newOrder = currentOrder.toMutableList()
                                                newOrder.removeAt(currentIndex)
                                                newOrder.add(currentIndex + 1, player.id)
                                                viewModel.onRankingReordered(newOrder)
                                                dragOffsetY -= threshold
                                            }
                                            dragOffsetY < -threshold / 2 && currentIndex > 0 -> {
                                                val newOrder = currentOrder.toMutableList()
                                                newOrder.removeAt(currentIndex)
                                                newOrder.add(currentIndex - 1, player.id)
                                                viewModel.onRankingReordered(newOrder)
                                                dragOffsetY += threshold
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    )
                }
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Próximo")
            }
        }
    }
}

@Composable
private fun RankingRow(
    position: Int,
    player: Player,
    pointsValue: String,
    showPoints: Boolean,
    onPointsChange: (String) -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${position}º",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (position == 1) Gold else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp)
            )
            PlayerIdentityRow(
                name = player.nickname,
                isWinner = position == 1,
                modifier = Modifier.weight(1f)
            )
            if (showPoints) {
                OutlinedTextField(
                    value = pointsValue,
                    onValueChange = onPointsChange,
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Pts") }
                )
            }
            Column {
                IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Mover pra cima")
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Mover pra baixo")
                }
            }
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = "Arrastar pra reordenar",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .then(dragHandleModifier)
            )
        }
    }
}
