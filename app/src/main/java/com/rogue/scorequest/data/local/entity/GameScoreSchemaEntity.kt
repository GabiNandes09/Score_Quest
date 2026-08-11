package com.rogue.scorequest.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.rogue.scorequest.domain.model.GameScoreSchema
import com.rogue.scorequest.domain.model.ScoreFieldType
import com.rogue.scorequest.domain.model.ScoreFormula
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.WinnerMode
import com.rogue.scorequest.utils.toEpochMillis
import com.rogue.scorequest.utils.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(
    tableName = "game_score_schema",
    foreignKeys = [
        ForeignKey(
            entity = BoardGameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"]
        )
    ]
)
data class GameScoreSchemaEntity(
    val id: String,
    @PrimaryKey @ColumnInfo(name = "game_id") val gameId: String, // um schema por jogo — PK garante unicidade
    val type: ScoreSchemaType,
    @ColumnInfo(name = "fields_json") val fieldsJson: String,
    @ColumnInfo(name = "winner_mode") val winnerMode: WinnerMode,
    @ColumnInfo(name = "formula_json") val formulaJson: String?,
    @ColumnInfo(name = "created_by_user_id") val createdByUserId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?
)

fun GameScoreSchemaEntity.toDomain() = GameScoreSchema(
    id = id,
    gameId = gameId,
    type = type,
    fields = Json.decodeFromString<List<ScoreFieldType>>(fieldsJson),
    winnerMode = winnerMode,
    formula = formulaJson?.let { Json.decodeFromString<ScoreFormula>(it) },
    createdByUserId = createdByUserId,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime(),
    deletedAt = deletedAt?.toLocalDateTime()
)

fun GameScoreSchema.toEntity() = GameScoreSchemaEntity(
    id = id,
    gameId = gameId,
    type = type,
    fieldsJson = Json.encodeToString(fields),
    winnerMode = winnerMode,
    formulaJson = formula?.let { Json.encodeToString(it) },
    createdByUserId = createdByUserId,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis(),
    deletedAt = deletedAt?.toEpochMillis()
)
