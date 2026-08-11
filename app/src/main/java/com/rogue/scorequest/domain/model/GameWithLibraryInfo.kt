package com.rogue.scorequest.domain.model

data class GameWithLibraryInfo(
    val game: BoardGame,
    val libraryEntry: UserLibraryEntry?
)
