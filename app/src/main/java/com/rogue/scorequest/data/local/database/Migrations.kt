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

// Grupos de jogadores: 2 tabelas novas (player_group + a tabela de junção
// player_group_member, primeiro relacionamento N:N do projeto) + 1 coluna nova em
// game_session. `game_session.group_id` é adicionada via ALTER TABLE ADD COLUMN
// deliberadamente SEM constraint de FOREIGN KEY — o SQLite não permite acrescentar uma FK a
// uma tabela já existente por ALTER TABLE (só em CREATE TABLE), e recriar a tabela inteira
// pra isso seria desnecessariamente arriscado pra uma referência que é só apresentacional
// (nome do grupo no detalhe da partida) e que o app trata como solta/órfã-segura de propósito
// (exclusão de grupo não é bloqueada, ver DeletePlayerGroupUseCase). A tabela de junção, por
// ser criada do zero aqui, já nasce com as duas FKs de verdade. Ver CLAUDE.md "Grupos de
// jogadores".
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `player_group` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `photo_path` TEXT,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `deleted_at` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `player_group_member` (
                `group_id` TEXT NOT NULL,
                `player_id` TEXT NOT NULL,
                PRIMARY KEY(`group_id`, `player_id`),
                FOREIGN KEY(`group_id`) REFERENCES `player_group`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(`player_id`) REFERENCES `player`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_player_group_member_group_id` ON `player_group_member` (`group_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_player_group_member_player_id` ON `player_group_member` (`player_id`)")
        db.execSQL("ALTER TABLE `game_session` ADD COLUMN `group_id` TEXT")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_game_session_group_id` ON `game_session` (`group_id`)")
    }
}
