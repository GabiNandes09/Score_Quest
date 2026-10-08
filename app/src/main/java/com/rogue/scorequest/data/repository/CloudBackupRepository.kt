package com.rogue.scorequest.data.repository

import com.google.firebase.firestore.CollectionReference
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
private const val SCHEMAS_COLLECTION = "scoreSchemas"
private const val LIBRARY_COLLECTION = "libraryEntries"
private const val PLAYERS_COLLECTION = "players"
private const val GROUPS_COLLECTION = "groups"
private const val SESSIONS_COLLECTION = "sessions"
private const val FAVORITES_FIELD = "favoriteGameIds"

// Limite real do Firestore é 500 operações por WriteBatch — chunка com folga.
private const val BATCH_CHUNK_SIZE = 450

/**
 * Dois escopos de dado no Firestore (pedido explícito do usuário):
 * - **Catálogo compartilhado** (`games`/`scoreSchemas`, coleções no nível
 *   raiz): jogos e pontuações personalizadas são globais, visíveis/editáveis
 *   por qualquer usuário logado — não pertencem a um dono. Mesmo espírito do
 *   "qualquer usuário edita, sem restrição" já documentado pra pontuação
 *   personalizada antes de ter conta.
 * - **Dado pessoal** (`users/{uid}/...`): estante (`libraryEntries`),
 *   jogadores, grupos, partidas e perfil continuam privados por conta.
 */
class CloudBackupRepository(
    private val firestore: FirebaseFirestore
) {
    private fun userDoc(uid: String) = firestore.collection(USERS_COLLECTION).document(uid)

    // --- Catálogo compartilhado (sem dono) ---

    suspend fun uploadGames(games: List<CloudGame>) =
        uploadCollection(firestore.collection(GAMES_COLLECTION), games) { it.id to toFirestoreMap(CloudGame.serializer(), it) }

    suspend fun downloadGames(): List<CloudGame> =
        downloadCollection(firestore.collection(GAMES_COLLECTION)) { fromFirestoreMap(CloudGame.serializer(), it) }

    suspend fun uploadScoreSchemas(schemas: List<CloudScoreSchema>) =
        uploadCollection(firestore.collection(SCHEMAS_COLLECTION), schemas) { it.gameId to toFirestoreMap(CloudScoreSchema.serializer(), it) }

    suspend fun downloadScoreSchemas(): List<CloudScoreSchema> =
        downloadCollection(firestore.collection(SCHEMAS_COLLECTION)) { fromFirestoreMap(CloudScoreSchema.serializer(), it) }

    // --- Dado pessoal (privado por conta) ---

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

    suspend fun uploadLibraryEntries(uid: String, entries: List<CloudLibraryEntry>) =
        uploadCollection(userDoc(uid).collection(LIBRARY_COLLECTION), entries) { it.gameId to toFirestoreMap(CloudLibraryEntry.serializer(), it) }

    suspend fun downloadLibraryEntries(uid: String): List<CloudLibraryEntry> =
        downloadCollection(userDoc(uid).collection(LIBRARY_COLLECTION)) { fromFirestoreMap(CloudLibraryEntry.serializer(), it) }

    suspend fun uploadPlayers(uid: String, players: List<CloudPlayer>) =
        uploadCollection(userDoc(uid).collection(PLAYERS_COLLECTION), players) { it.id to toFirestoreMap(CloudPlayer.serializer(), it) }

    suspend fun downloadPlayers(uid: String): List<CloudPlayer> =
        downloadCollection(userDoc(uid).collection(PLAYERS_COLLECTION)) { fromFirestoreMap(CloudPlayer.serializer(), it) }

    suspend fun uploadGroups(uid: String, groups: List<CloudGroup>) =
        uploadCollection(userDoc(uid).collection(GROUPS_COLLECTION), groups) { it.id to toFirestoreMap(CloudGroup.serializer(), it) }

    suspend fun downloadGroups(uid: String): List<CloudGroup> =
        downloadCollection(userDoc(uid).collection(GROUPS_COLLECTION)) { fromFirestoreMap(CloudGroup.serializer(), it) }

    suspend fun uploadSessions(uid: String, sessions: List<CloudSession>) =
        uploadCollection(userDoc(uid).collection(SESSIONS_COLLECTION), sessions) { it.id to toFirestoreMap(CloudSession.serializer(), it) }

    suspend fun downloadSessions(uid: String): List<CloudSession> =
        downloadCollection(userDoc(uid).collection(SESSIONS_COLLECTION)) { fromFirestoreMap(CloudSession.serializer(), it) }

    private suspend fun <T> uploadCollection(
        ref: CollectionReference,
        items: List<T>,
        toDoc: (T) -> Pair<String, Map<String, Any?>>
    ) {
        if (items.isEmpty()) return
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
        ref: CollectionReference,
        fromDoc: (Map<String, Any?>) -> T
    ): List<T> {
        val snapshot = ref.get().await()
        return snapshot.documents.mapNotNull { doc -> doc.data?.let { runCatching { fromDoc(it) }.getOrNull() } }
    }
}
