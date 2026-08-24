package com.rogue.scorequest.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Puramente aditiva: cria a tabela nova de cronômetro ativo, não toca em nenhuma
// tabela/dado existente. Ver CLAUDE.md ("Cronômetro de partida ao vivo") para o porquê
// disso ser seguro fazer com uma Migration de verdade, ao contrário de mudanças em
// entidades já existentes.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `active_timer` (
                `id` TEXT NOT NULL,
                `game_id` TEXT NOT NULL,
                `game_name` TEXT NOT NULL,
                `started_at` INTEGER NOT NULL,
                `status` TEXT NOT NULL,
                `paused_at` INTEGER,
                `accumulated_paused_millis` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}
