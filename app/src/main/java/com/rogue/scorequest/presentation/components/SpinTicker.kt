package com.rogue.scorequest.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Estado de um "sorteio com suspense": ao chamar spin(), cicla valores aleatórios (via
 * randomValue) cada vez mais devagar até parar no valor final passado, dando sensação de
 * desaceleração sem precisar de Animatable/biblioteca de animação. Cada ferramenta decide o
 * que renderizar pra cada valor (moeda, número, letra, nome de jogador...) — só a cadência de
 * tempo é compartilhada.
 */
class SpinTickerState<T>(private val randomValue: () -> T) {
    var current by mutableStateOf<T?>(null)
        private set
    var isSpinning by mutableStateOf(false)
        private set

    suspend fun spin(final: T) {
        isSpinning = true
        val steps = 18
        repeat(steps) { step ->
            current = randomValue()
            val progress = step / (steps - 1).toFloat()
            delay((40 + (220 - 40) * progress).toLong())
        }
        current = final
        isSpinning = false
    }
}

@Composable
fun <T> rememberSpinTicker(vararg keys: Any?, randomValue: () -> T): SpinTickerState<T> =
    remember(*keys) { SpinTickerState(randomValue) }
