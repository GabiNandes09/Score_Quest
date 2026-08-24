package com.rogue.scorequest.presentation.screens.tools

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rogue.scorequest.presentation.components.EditableTextList
import com.rogue.scorequest.ui.theme.Gold
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch

private const val EXTRA_SPINS = 5

// 3 tons alternados em vez de 2 — o amarelo intermediário (âmbar) ajuda a diferenciar fatias
// vizinhas quando há muitas opções, já que 2 cores só alternando ficam repetitivas em roda com
// mais de ~6 fatias.
private val SLICE_COLORS = listOf(Gold, Color(0xFFB8860B), Color(0xFF2C2C2C))
private val SLICE_TEXT_COLORS = listOf(Color.Black, Color.Black, Color.White)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinWheelScreen(onBackClick: () -> Unit) {
    var options by remember { mutableStateOf(listOf<String>()) }
    var resultIndex by remember { mutableStateOf<Int?>(null) }
    var isSpinning by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Roleta") },
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
        ) {
            // Roda travada no topo, fora da área rolável — sempre visível.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (options.size >= 2) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Wheel(
                            options = options,
                            rotationDegrees = rotation.value,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .aspectRatio(1f)
                        )
                        Text(
                            text = "▼",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Gold,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-4).dp)
                        )
                    }

                    resultIndex?.let { index ->
                        Text(
                            text = "Resultado: ${options[index]}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = "Adicione pelo menos 2 opções pra girar a roleta",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Lista de opções rola independente, entre a roda (topo) e o botão (base).
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                EditableTextList(
                    items = options,
                    onAdd = { value -> if (value.isNotBlank()) options = options + value.trim() },
                    onRemove = { index -> options = options.filterIndexed { i, _ -> i != index } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Nova opção"
                )
            }

            // Botão travado na base da tela.
            Button(
                onClick = {
                    if (options.size < 2 || isSpinning) return@Button
                    val chosen = Random.nextInt(options.size)
                    val sliceAngle = 360f / options.size
                    val centerAngle = -90f + (chosen + 0.5f) * sliceAngle
                    val targetMod = ((-90f - centerAngle) % 360f + 360f) % 360f
                    val currentMod = rotation.value % 360f
                    val deltaToTarget = ((targetMod - currentMod) % 360f + 360f) % 360f
                    val finalTarget = rotation.value + EXTRA_SPINS * 360f + deltaToTarget

                    scope.launch {
                        isSpinning = true
                        resultIndex = null
                        rotation.animateTo(
                            targetValue = finalTarget,
                            animationSpec = tween(durationMillis = 3200, easing = FastOutSlowInEasing)
                        )
                        resultIndex = chosen
                        isSpinning = false
                    }
                },
                enabled = !isSpinning && options.size >= 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Girar")
            }
        }
    }
}

@Composable
private fun Wheel(options: List<String>, rotationDegrees: Float, modifier: Modifier = Modifier) {
    val sliceTextColors = SLICE_TEXT_COLORS.map { it.toArgb() }

    Canvas(modifier = modifier.rotate(rotationDegrees)) {
        val sliceAngle = 360f / options.size
        val radius = size.minDimension / 2
        val labelRadius = radius * 0.62f
        val textPaint = Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = 13.sp.toPx()
            isAntiAlias = true
        }

        options.forEachIndexed { index, label ->
            val startAngle = -90f + index * sliceAngle
            val colorIndex = index % SLICE_COLORS.size
            drawArc(
                color = SLICE_COLORS[colorIndex],
                startAngle = startAngle,
                sweepAngle = sliceAngle,
                useCenter = true
            )

            val midAngleDeg = startAngle + sliceAngle / 2
            val midAngleRad = Math.toRadians(midAngleDeg.toDouble())
            val labelX = center.x + labelRadius * cos(midAngleRad).toFloat()
            val labelY = center.y + labelRadius * sin(midAngleRad).toFloat()

            drawContext.canvas.nativeCanvas.apply {
                save()
                translate(labelX, labelY)
                rotate(midAngleDeg + 90f)
                textPaint.color = sliceTextColors[colorIndex]
                drawText(label, 0f, 0f, textPaint)
                restore()
            }
        }
    }
}
