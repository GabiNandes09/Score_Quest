package com.rogue.scorequest.presentation.screens.tools

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rogue.scorequest.presentation.components.rememberSpinTicker
import com.rogue.scorequest.ui.theme.Gold
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private const val MIN_FINGERS = 2
private const val WAIT_SECONDS = 5
private val CIRCLE_SIZE = 72.dp
private val WINNER_CIRCLE_SIZE = 140.dp

// 8 cores bem distintas (espectro completo, não só tons de dourado) — cada dedo na tela
// recebe uma pela ordem em que encostou, pra dar pra diferenciar visualmente quem é quem.
private val FINGER_COLORS = listOf(
    Gold,
    Color(0xFFE53935), // vermelho
    Color(0xFFFB8C00), // laranja
    Color(0xFF43A047), // verde
    Color(0xFF00ACC1), // ciano
    Color(0xFF1E88E5), // azul
    Color(0xFF8E24AA), // roxo
    Color(0xFFD81B60) // rosa
)

private fun colorForIndex(index: Int): Color = FINGER_COLORS[index % FINGER_COLORS.size]

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FingerPickerScreen(onBackClick: () -> Unit) {
    // Dedos ao vivo — só é atualizado enquanto nada foi "travado" ainda (antes do sorteio
    // começar). Uma vez travado, a lista de participantes do sorteio (`lockedPositions`) para
    // de mudar, mesmo que alguém tire ou encoste outro dedo no meio da animação — assim a
    // roleta/resultado não fica instável no meio do sorteio.
    var touches by remember { mutableStateOf(mapOf<PointerId, Offset>()) }
    var lockedPositions by remember { mutableStateOf(mapOf<PointerId, Offset>()) }
    var isLocked by remember { mutableStateOf(false) }
    var waitSecondsLeft by remember { mutableIntStateOf(WAIT_SECONDS) }

    val ticker = rememberSpinTicker(lockedPositions.keys, randomValue = { lockedPositions.keys.randomOrNull() })

    // Contagem de 5s: começa (ou reinicia) toda vez que o conjunto de dedos muda enquanto
    // ainda não travou — encostar/soltar um dedo durante a espera reinicia a contagem.
    LaunchedEffect(touches.keys, isLocked) {
        if (!isLocked && touches.size >= MIN_FINGERS) {
            waitSecondsLeft = WAIT_SECONDS
            repeat(WAIT_SECONDS) {
                delay(1000)
                waitSecondsLeft -= 1
            }
            lockedPositions = touches
            isLocked = true
        }
    }

    // Anima o sorteio (cor cicla entre os dedos travados) assim que trava, landing num
    // vencedor real.
    LaunchedEffect(lockedPositions) {
        if (lockedPositions.isNotEmpty()) {
            ticker.spin(lockedPositions.keys.random())
        }
    }

    val winnerId = if (isLocked && !ticker.isSpinning) ticker.current else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dedo na tela") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (!isLocked) {
                                val updated = touches.toMutableMap()
                                event.changes.forEach { change ->
                                    if (change.pressed) {
                                        updated[change.id] = change.position
                                    } else {
                                        updated.remove(change.id)
                                    }
                                }
                                touches = updated
                            } else if (event.changes.none { it.pressed }) {
                                // Todo mundo tirou o dedo — libera pra um novo sorteio.
                                touches = emptyMap()
                                lockedPositions = emptyMap()
                                isLocked = false
                            }
                        }
                    }
                }
        ) {
            when {
                touches.isEmpty() && !isLocked -> {
                    Text(
                        text = "Encoste 2 ou mais dedos na tela e segure",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }
                !isLocked && touches.size < MIN_FINGERS -> {
                    Text(
                        text = "Faltam dedos — encoste pelo menos ${MIN_FINGERS - touches.size} a mais",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }
                !isLocked -> {
                    Text(
                        text = "Sorteando em ${waitSecondsLeft}s...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 32.dp)
                    )
                }
                winnerId == null -> {
                    Text(
                        text = "Sorteando...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 32.dp)
                    )
                }
                else -> {
                    Text(
                        text = "Dedo sorteado!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 32.dp)
                    )
                }
            }

            if (!isLocked) {
                touches.entries.forEachIndexed { index, (_, position) ->
                    FingerCircle(position = position, color = colorForIndex(index).copy(alpha = 0.7f))
                }
            } else if (winnerId == null) {
                // Fase de sorteio: cada dedo travado mantém sua própria cor, mas só o "aceso"
                // no momento (ticker.current) fica em opacidade cheia — os outros escurecem —
                // criando o efeito de "cor circulando" até desacelerar e parar no sorteado.
                lockedPositions.entries.forEachIndexed { index, (id, position) ->
                    val highlighted = id == ticker.current
                    val baseColor = colorForIndex(index)
                    FingerCircle(
                        position = position,
                        color = if (highlighted) baseColor else baseColor.copy(alpha = 0.3f)
                    )
                }
            } else {
                // Resultado: só o dedo sorteado continua na tela, com a própria cor e círculo maior.
                val winnerIndex = lockedPositions.keys.indexOf(winnerId)
                lockedPositions[winnerId]?.let { position ->
                    FingerCircle(
                        position = position,
                        color = colorForIndex(winnerIndex),
                        big = true
                    )
                }
            }
        }
    }
}

@Composable
private fun FingerCircle(
    position: Offset,
    color: Color,
    big: Boolean = false
) {
    val density = LocalDensity.current
    val size by animateDpAsState(
        targetValue = if (big) WINNER_CIRCLE_SIZE else CIRCLE_SIZE,
        animationSpec = tween(durationMillis = 400),
        label = "finger_size"
    )
    val sizePx = with(density) { size.toPx() }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (position.x - sizePx / 2).roundToInt(),
                    (position.y - sizePx / 2).roundToInt()
                )
            }
            .size(size)
            .background(color = color, shape = CircleShape)
    )
}
