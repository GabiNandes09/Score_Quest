package com.rogue.scorequest.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rogue.scorequest.data.cloud.CloudGame
import com.rogue.scorequest.data.cloud.CloudGroup
import com.rogue.scorequest.data.cloud.CloudLibraryEntry
import com.rogue.scorequest.data.cloud.CloudPlayer
import com.rogue.scorequest.data.cloud.CloudProfile
import com.rogue.scorequest.data.cloud.CloudScoreSchema
import com.rogue.scorequest.data.cloud.CloudSession
import com.rogue.scorequest.data.cloud.fromFirestoreMap
import com.rogue.scorequest.data.cloud.toFirestoreMap
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"
private const val GAMES_COLLECTION = "games"
private const val LIBRARY_COLLECTION = "libraryEntries"
private const val PLAYERS_COLLECTION = "players"
private const val GROUPS_COLLECTION = "groups"
private const val SESSIONS_COLLECTION = "sessions"
private const val SCHEMAS_COLLECTION = "scoreSchemas"
private const val FAVORITES_FIELD = "favoriteGameIds"

// Limite real do Firestore é 500 operações por WriteBatch — chunка com folga.
private const val BATCH_CHUNK_SIZE = 450

/**
 * Backup normalizado por coleção em `users/{uid}/...` (um documento por
 * jogo/jogador/partida/etc.) — ver CLAUDE.md "Conta e sincronização com
 * Firebase". Perfil + favoritos ficam no próprio documento `users/{uid}`
 * (escritos sempre com merge, pra um não sobrescrever o outro).
 */
class CloudBackupRepository(
    private val firestore: FirebaseFirestore
) {
    private fun userDoc(uid: String) = firestore.collection(USERS_COLLECTION).document(uid)

    suspend fun uploadProfile(uid: String, profile: CloudProfile) {
        userDoc(uid).set(toFirestoreMap(CloudProfile.serializer(), profile), SetOptions.merge()).await()
    }

    suspend fun downloadProfile(uid: String): CloudProfile? {
        val data = userDoc(uid).get().await().data ?: return null
        return runCatching { fromFirestoreMap(CloudProfile.serializer(), data) }.getOrNull()
    }

    suspend fun uploadFavoriteGameIds(uid: String, gameIds: List<String>) {
        userDoc(uid).set(mapOf(FAVORITES_FIELD to gameIds), SetOptions.merge()).await()
    }

    suspend fun downloadFavoriteGameIds(uid: String): List<String> {
        val data = userDoc(uid).get().await().data ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        return (data[FAVORITES_FIELD] as? List<String>).orEmpty()
    }

    suspend fun uploadGames(uid: String, games: List<CloudGame>) =
        uploadCollection(uid, GAMES_COLLECTION, games) { it.id to toFirestoreMap(CloudGame.serializer(), it) }

    suspend fun downloadGames(uid: String): List<CloudGame> =
        downloadCollection(uid, GAMES_COLLECTION) { fromFirestoreMap(CloudGame.serializer(), it) }

    suspend fun uploadLibraryEntries(uid: String, entries: List<CloudLibraryEntry>) =
        uploadCollection(uid, LIBRARY_COLLECTION, entries) { it.gameId to toFirestoreMap(CloudLibraryEntry.serializer(), it) }

    suspend fun downloadLibraryEntries(uid: String): List<CloudLibraryEntry> =
        downloadCollection(uid, LIBRARY_COLLECTION) { fromFirestoreMap(CloudLibraryEntry.serializer(), it) }

    suspend fun uploadPlayers(uid: String, players: List<CloudPlayer>) =
        uploadCollection(uid, PLAYERS_COLLECTION, players) { it.id to toFirestoreMap(CloudPlayer.serializer(), it) }

    suspend fun downloadPlayers(uid: String): List<CloudPlayer> =
        downloadCollection(uid, PLAYERS_COLLECTION) { fromFirestoreMap(CloudPlayer.serializer(), it) }

    suspend fun uploadGroups(uid: String, groups: List<CloudGroup>) =
        uploadCollection(uid, GROUPS_COLLECTION, groups) { it.id to toFirestoreMap(CloudGroup.serializer(), it) }

    suspend fun downloadGroups(uid: String): List<CloudGroup> =
        downloadCollection(uid, GROUPS_COLLECTION) { fromFirestoreMap(CloudGroup.serializer(), it) }

    suspend fun uploadSessions(uid: String, sessions: List<CloudSession>) =
        uploadCollection(uid, SESSIONS_COLLECTION, sessions) { it.id to toFirestoreMap(CloudSession.serializer(), it) }

    suspend fun downloadSessions(uid: String): List<CloudSession> =
        downloadCollection(uid, SESSIONS_COLLECTION) { fromFirestoreMap(CloudSession.serializer(), it) }

    suspend fun uploadScoreSchemas(uid: String, schemas: List<CloudScoreSchema>) =
        uploadCollection(uid, SCHEMAS_COLLECTION, schemas) { it.gameId to toFirestoreMap(CloudScoreSchema.serializer(), it) }

    suspend fun downloadScoreSchemas(uid: String): List<CloudScoreSchema> =
        downloadCollection(uid, SCHEMAS_COLLECTION) { fromFirestoreMap(CloudScoreSchema.serializer(), it) }

    private suspend fun <T> uploadCollection(
        uid: String,
        collection: String,
        items: List<T>,
        toDoc: (T) -> Pair<String, Map<String, Any?>>
    ) {
        if (items.isEmpty()) return
        val ref = userDoc(uid).collection(collection)
        items.chunked(BATCH_CHUNK_SIZE).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { item ->
                val (docId, data) = toDoc(item)
                batch.set(ref.document(docId), data)
            }
            batch.commit().await()
        }
    }

    private suspend fun <T> downloadCollection(
        uid: String,
        collection: String,
        fromDoc: (Map<String, Any?>) -> T
    ): List<T> {
        val snapshot = userDoc(uid).collection(collection).get().await()
        return snapshot.documents.mapNotNull { doc -> doc.data?.let { runCatching { fromDoc(it) }.getOrNull() } }
    }
}
