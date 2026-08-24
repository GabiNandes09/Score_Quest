package com.rogue.scorequest.data.local.entity

import androidx.room.TypeConverter
import com.rogue.scorequest.domain.model.GameSource
import com.rogue.scorequest.domain.model.LibraryStatus
import com.rogue.scorequest.domain.model.ScoreSchemaType
import com.rogue.scorequest.domain.model.TimerStatus
import com.rogue.scorequest.domain.model.WinnerMode

class Converters {

    @TypeConverter
    fun fromGameSource(value: GameSource): String = value.name

    @TypeConverter
    fun toGameSource(value: String): GameSource = GameSource.valueOf(value)

    @TypeConverter
    fun fromLibraryStatus(value: LibraryStatus): String = value.name

    @TypeConverter
    fun toLibraryStatus(value: String): LibraryStatus = LibraryStatus.valueOf(value)

    @TypeConverter
    fun fromScoreSchemaType(value: ScoreSchemaType): String = value.name

    @TypeConverter
    fun toScoreSchemaType(value: String): ScoreSchemaType = ScoreSchemaType.valueOf(value)

    @TypeConverter
    fun fromWinnerMode(value: WinnerMode): String = value.name

    @TypeConverter
    fun toWinnerMode(value: String): WinnerMode = WinnerMode.valueOf(value)

    @TypeConverter
    fun fromTimerStatus(value: TimerStatus): String = value.name

    @TypeConverter
    fun toTimerStatus(value: String): TimerStatus = TimerStatus.valueOf(value)
}
