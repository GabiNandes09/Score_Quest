package com.rogue.scorequest.data.cloud

import com.rogue.scorequest.domain.model.BoardGame
import com.rogue.scorequest.domain.model.GameScoreSchema
import com.rogue.scorequest.domain.model.GameSession
import com.rogue.scorequest.domain.model.Player
import com.rogue.scorequest.domain.model.PlayerGroup
import com.rogue.scorequest.domain.model.ScoreEntry
import com.rogue.scorequest.domain.model.SessionWithDetails
import com.rogue.scorequest.domain.model.UserLibraryEntry
import com.rogue.scorequest.domain.model.UserProfile
import com.rogue.scorequest.utils.toEpochMillis
import com.rogue.scorequest.utils.toLocalDateTime

fun UserProfile.toCloud() = CloudProfile(
    displayName = displayName,
    bio = bio,
    avatarUri = avatarUri,
    updatedAt = updatedAt.toEpochMillis()
)

fun CloudProfile.toDomain() = UserProfile(
    displayName = displayName,
    bio = bio,
    avatarUri = avatarUri,
    updatedAt = updatedAt.toLocalDateTime()
)

fun BoardGame.toCloud() = CloudGame(
    id = id,
    name = name,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    avgDurationMinutes = avgDurationMinutes,
    coverImageUrl = coverImageUrl,
    category = category,
    weight = weight,
    source = source,
    createdByUserId = createdByUserId,
    syncedAt = syncedAt?.toEpochMillis(),
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

fun CloudGame.toDomain() = BoardGame(
    id = id,
    name = name,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    avgDurationMinutes = avgDurationMinutes,
    coverImageUrl = coverImageUrl,
    category = category,
    weight = weight,
    source = source,
    createdByUserId = createdByUserId,
    syncedAt = syncedAt?.toLocalDateTime(),
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun UserLibraryEntry.toCloud() = CloudLibraryEntry(
    gameId = gameId,
    status = status,
    played = played,
    lentTo = lentTo,
    rating = rating,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

fun CloudLibraryEntry.toDomain() = UserLibraryEntry(
    gameId = gameId,
    status = status,
    played = played,
    lentTo = lentTo,
    rating = rating,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun Player.toCloud() = CloudPlayer(
    id = id,
    nickname = nickname,
    linkedUserId = linkedUserId,
    avatarPath = avatarPath,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

fun CloudPlayer.toDomain() = Player(
    id = id,
    nickname = nickname,
    linkedUserId = linkedUserId,
    avatarPath = avatarPath,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun PlayerGroup.toCloud() = CloudGroup(
    id = id,
    name = name,
    photoPath = photoPath,
    memberIds = memberIds,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

fun CloudGroup.toDomain() = PlayerGroup(
    id = id,
    name = name,
    photoPath = photoPath,
    memberIds = memberIds,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun ScoreEntry.toCloud() = CloudScoreEntry(
    playerId = playerId,
    totalScore = totalScore,
    isWinner = isWinner,
    fieldValues = fieldValues,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

fun CloudScoreEntry.toDomain(sessionId: String) = ScoreEntry(
    sessionId = sessionId,
    playerId = playerId,
    totalScore = totalScore,
    isWinner = isWinner,
    fieldValues = fieldValues,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun SessionWithDetails.toCloud() = CloudSession(
    id = session.id,
    gameId = session.gameId,
    date = session.date.toEpochMillis(),
    durationMinutes = session.durationMinutes,
    variantOrExpansion = session.variantOrExpansion,
    photoUri = session.photoUri,
    groupId = session.groupId,
    createdAt = session.createdAt.toEpochMillis(),
    updatedAt = session.updatedAt.toEpochMillis(),
    deletedAt = session.deletedAt?.toEpochMillis(),
    scores = scores.map { it.toCloud() }
)

/** Reconstrói o par GameSession+scores pra persistir via GameSessionRepository.saveSession. */
fun CloudSession.toDomainSessionAndScores(): Pair<GameSession, List<ScoreEntry>> {
    val domainScores = scores.map { it.toDomain(id) }
    val session = GameSession(
        id = id,
        gameId = gameId,
        date = date.toLocalDateTime(),
        durationMinutes = durationMinutes,
        variantOrExpansion = variantOrExpansion,
        photoUri = photoUri,
        groupId = groupId,
        participantIds = domainScores.map { it.playerId },
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime(),
        deletedAt = deletedAt?.toLocalDateTime()
    )
    return session to domainScores
}

fun GameScoreSchema.toCloud() = CloudScoreSchema(
    id = id,
    gameId = gameId,
    type = type,
    fields = fields,
    winnerMode = winnerMode,
    formula = formula,
    createdByUserId = createdByUserId,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)

fun CloudScoreSchema.toDomain() = GameScoreSchema(
    id = id,
    gameId = gameId,
    type = type,
    fields = fields,
    winnerMode = winnerMode,
    formula = formula,
    createdByUserId = createdByUserId,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)
