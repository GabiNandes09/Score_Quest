package com.rogue.scorequest.data.local.entity

import androidx.room.TypeConverter
import com.rogue.scorequest.domain.model.GameSource
import com.rogue.scorequest.domain.model.LibraryStatus

class Converters {

    @TypeConverter
    fun fromGameSource(value: GameSource): String = value.name

    @TypeConverter
    fun toGameSource(value: String): GameSource = GameSource.valueOf(value)

    @TypeConverter
    fun fromLibraryStatus(value: LibraryStatus): String = value.name

    @TypeConverter
    fun toLibraryStatus(value: String): LibraryStatus = LibraryStatus.valueOf(value)
}
