package com.rogue.scorequest.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val BACKUPS_COLLECTION = "user_backups"
private const val PAYLOAD_FIELD = "payload"
private const val UPDATED_AT_FIELD = "updatedAt"

/** Lê/escreve o backup completo de um usuário (ver CloudBackup) como um único documento. */
class CloudBackupRepository(
    private val firestore: FirebaseFirestore
) {

    suspend fun upload(uid: String, payloadJson: String) {
        firestore.collection(BACKUPS_COLLECTION).document(uid)
            .set(mapOf(PAYLOAD_FIELD to payloadJson, UPDATED_AT_FIELD to System.currentTimeMillis()))
            .await()
    }

    suspend fun download(uid: String): String? {
        val snapshot = firestore.collection(BACKUPS_COLLECTION).document(uid).get().await()
        return snapshot.getString(PAYLOAD_FIELD)
    }
}
