package com.rogue.scorequest.domain.usecase

import com.rogue.scorequest.data.cloud.CloudPublicProfile
import com.rogue.scorequest.data.repository.CloudBackupRepository
import com.rogue.scorequest.domain.model.AuthUser
import java.text.Normalizer

/**
 * Roda a cada login — cria (1ª vez) ou atualiza (demais vezes) o diretório
 * público mínimo do usuário (`publicProfiles/{uid}`: nome/e-mail/foto da
 * conta Google + @username). Nome/e-mail/foto são sempre atualizados (podem
 * mudar do lado do Google); @username é gerado só na 1ª vez e nunca muda
 * depois.
 */
class EnsurePublicProfileUseCase(
    private val cloudBackupRepository: CloudBackupRepository
) {
    suspend operator fun invoke(authUser: AuthUser) {
        val existing = cloudBackupRepository.downloadPublicProfile(authUser.uid)
        val now = System.currentTimeMillis()
        val username = existing?.username ?: generateUsername(authUser)

        cloudBackupRepository.uploadPublicProfile(
            CloudPublicProfile(
                uid = authUser.uid,
                displayName = authUser.displayName,
                email = authUser.email,
                photoUrl = authUser.photoUrl,
                username = username,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
    }

    /** 1ª pessoa com aquele primeiro nome vira só "Nome"; as seguintes, "Nome_2", "Nome_3"... */
    private suspend fun generateUsername(authUser: AuthUser): String {
        val firstName = cleanFirstName(authUser)
        val ordinal = cloudBackupRepository.reserveUsernameOrdinal(firstName.lowercase())
        return if (ordinal <= 1L) firstName else "${firstName}_$ordinal"
    }

    private fun cleanFirstName(authUser: AuthUser): String {
        val raw = authUser.displayName?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: authUser.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Usuario"
        val withoutAccents = Normalizer.normalize(raw, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
        val cleaned = withoutAccents.filter { it.isLetterOrDigit() }
        return cleaned.ifBlank { "Usuario" }
    }
}
