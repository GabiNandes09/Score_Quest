package com.rogue.scorequest.data.cloud

import com.rogue.scorequest.domain.model.GameSource
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.model.ScoreFieldType
import com.rogue.scorequest.domain.model.ScoreFormula
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.WinnerMode
import kotlinx.serialization.Serializable

/**
 * Espelho completo dos dados locais de um usuário, serializado como um único
 * documento no Firestore (ver CloudBackupRepository). Isolado dos domain
 * models (mesmo motivo do SeedGamesFile, ver data/seed/) — LocalDateTime vira
 * epoch millis (Long), igual às entidades Room.
 */
@Serializable
data class CloudBackup(
    val profile: CloudProfile? = null,
    val games: List<CloudGame> = emptyList(),
    val libraryEntries: List<CloudLibraryEntry> = emptyList(),
    val players: List<CloudPlayer> = emptyList(),
    val groups: List<CloudGroup> = emptyList(),
    val sessions: List<CloudSession> = emptyList(),
    val scoreSchemas: List<CloudScoreSchema> = emptyList(),
    val favoriteGameIds: List<String> = emptyList()
)

@Serializable
data class CloudProfile(
    val displayName: String,
    val bio: String? = null,
    val avatarUri: String? = null,
    val updatedAt: Long
)

@Serializable
data class CloudGame(
    val id: String,
    val name: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val avgDurationMinutes: Int,
    val coverImageUrl: String? = null,
    val category: String? = null,
    val weight: Double? = null,
    val source: GameSource,
    val createdByUserId: String? = null,
    val syncedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
)

@Serializable
data class CloudLibraryEntry(
    val gameId: String,
    val status: LibraryStatus,
    val played: Boolean = false,
    val lentTo: String? = null,
    val rating: Int? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
)

@Serializable
data class CloudPlayer(
    val id: String,
    val nickname: String,
    val linkedUserId: String? = null,
    val avatarPath: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
)

@Serializable
data class CloudGroup(
    val id: String,
    val name: String,
    val photoPath: String? = null,
    val memberIds: List<String> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
)

@Serializable
data class CloudSession(
    val id: String,
    val gameId: String,
    val date: Long,
    val durationMinutes: Int,
    val variantOrExpansion: String? = null,
    val photoUri: String? = null,
    val groupId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val scores: List<CloudScoreEntry> = emptyList()
)

@Serializable
data class CloudScoreEntry(
    val playerId: String,
    val totalScore: Int? = null,
    val isWinner: Boolean? = null,
    val fieldValues: Map<String, String>? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
)

@Serializable
data class CloudScoreSchema(
    val id: String,
    val gameId: String,
    val type: ScoreSchemaType,
    val fields: List<ScoreFieldType> = emptyList(),
    val winnerMode: WinnerMode,
    val formula: ScoreFormula? = null,
    val createdByUserId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null
)
