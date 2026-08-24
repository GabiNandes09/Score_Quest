package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.repository.ActiveTimerRepository
import com.rogue.scorequest.domain.model.TimerStatus
import kotlin.math.roundToInt

class FinishTimerUseCase(
    private val repository: ActiveTimerRepository
) {
    /**
     * Retorna a duração final em minutos (arredondada), ou null se não havia timer ativo.
     *
     * NÃO apaga a linha — só pausa (se ainda estiver rodando; se já estava pausado, não mexe
     * em pausedAtMillis pra não esticar o tempo contado). O timer só é encerrado de verdade
     * quando a partida é efetivamente salva no wizard (ver AddSessionViewModel.save() +
     * ClearActiveTimerForGameUseCase) — assim, se o usuário sair do wizard sem salvar, o
     * cronômetro continua ali, pausado com o tempo certo, em vez de reiniciar do zero.
     */
    suspend operator fun invoke(): Int? {
        val current = repository.getOnce() ?: return null
        val now = System.currentTimeMillis()
        val minutes = (current.elapsedMillis(now) / 60_000.0).roundToInt()
        if (current.status == TimerStatus.RUNNING) {
            repository.save(current.copy(status = TimerStatus.PAUSED, pausedAtMillis = now))
        }
        return minutes
    }
}
