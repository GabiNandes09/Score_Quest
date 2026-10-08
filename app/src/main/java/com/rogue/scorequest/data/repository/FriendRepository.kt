package com.rogue.scorequest.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rogue.scorequest.data.cloud.CloudFriend
import com.rogue.scorequest.data.cloud.CloudFriendRequest
import com.rogue.scorequest.data.cloud.CloudPublicProfile
import com.rogue.scorequest.data.cloud.fromFirestoreMap
import com.rogue.scorequest.data.cloud.toFirestoreMap
import com.rogue.scorequest.domain.model.FriendRequestStatus
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val USERS_COLLECTION = "users"
private const val PUBLIC_PROFILES_COLLECTION = "publicProfiles"
private const val FRIEND_REQUESTS_COLLECTION = "friendRequests"
private const val FRIENDS_COLLECTION = "friends"

class FriendRepository(
    private val firestore: FirebaseFirestore
) {
    private fun userDoc(uid: String) = firestore.collection(USERS_COLLECTION).document(uid)

    suspend fun findUserByUsername(username: String): CloudPublicProfile? {
        val snapshot = firestore.collection(PUBLIC_PROFILES_COLLECTION)
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .await()
        val data = snapshot.documents.firstOrNull()?.data ?: return null
        return runCatching { fromFirestoreMap(CloudPublicProfile.serializer(), data) }.getOrNull()
    }

    /** Duas queries simples (Firestore não faz OR entre campos diferentes) — só bloqueia se houver uma pendente. */
    suspend fun findPendingRequestBetween(uidA: String, uidB: String): CloudFriendRequest? {
        val asSender = firestore.collection(FRIEND_REQUESTS_COLLECTION)
            .whereEqualTo("fromUid", uidA).whereEqualTo("toUid", uidB).get().await()
        val asReceiver = firestore.collection(FRIEND_REQUESTS_COLLECTION)
            .whereEqualTo("fromUid", uidB).whereEqualTo("toUid", uidA).get().await()
        return (asSender.documents + asReceiver.documents)
            .mapNotNull { doc -> doc.data?.let { runCatching { fromFirestoreMap(CloudFriendRequest.serializer(), it) }.getOrNull() } }
            .find { it.status == FriendRequestStatus.PENDING }
    }

    suspend fun sendFriendRequest(request: CloudFriendRequest): CloudFriendRequest {
        val withId = request.copy(id = UUID.randomUUID().toString())
        firestore.collection(FRIEND_REQUESTS_COLLECTION).document(withId.id)
            .set(toFirestoreMap(CloudFriendRequest.serializer(), withId))
            .await()
        return withId
    }

    suspend fun getIncomingRequests(uid: String): List<CloudFriendRequest> {
        val snapshot = firestore.collection(FRIEND_REQUESTS_COLLECTION)
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", FriendRequestStatus.PENDING.name)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.data?.let { runCatching { fromFirestoreMap(CloudFriendRequest.serializer(), it) }.getOrNull() }
        }
    }

    suspend fun respondToRequest(requestId: String, accept: Boolean) {
        firestore.collection(FRIEND_REQUESTS_COLLECTION).document(requestId)
            .set(
                mapOf(
                    "status" to (if (accept) FriendRequestStatus.ACCEPTED else FriendRequestStatus.DECLINED).name,
                    "respondedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
            .await()
    }

    /** Espelha o registro de amizade nos dois lados (batch) — quem aceita (toUid) tem permissão de escrever nos dois, ver firestore.rules. */
    suspend fun addFriendPair(request: CloudFriendRequest) {
        val now = System.currentTimeMillis()
        val friendForSender = CloudFriend(
            uid = request.toUid,
            displayName = request.toDisplayName,
            username = request.toUsername,
            photoUrl = request.toPhotoUrl,
            since = now
        )
        val friendForReceiver = CloudFriend(
            uid = request.fromUid,
            displayName = request.fromDisplayName,
            username = request.fromUsername,
            photoUrl = request.fromPhotoUrl,
            since = now
        )
        firestore.batch()
            .set(userDoc(request.fromUid).collection(FRIENDS_COLLECTION).document(request.toUid), toFirestoreMap(CloudFriend.serializer(), friendForSender))
            .set(userDoc(request.toUid).collection(FRIENDS_COLLECTION).document(request.fromUid), toFirestoreMap(CloudFriend.serializer(), friendForReceiver))
            .commit()
            .await()
    }

    suspend fun getFriends(uid: String): List<CloudFriend> {
        val snapshot = userDoc(uid).collection(FRIENDS_COLLECTION).get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.data?.let { runCatching { fromFirestoreMap(CloudFriend.serializer(), it) }.getOrNull() }
        }
    }

    suspend fun removeFriendPair(uid: String, friendUid: String) {
        firestore.batch()
            .delete(userDoc(uid).collection(FRIENDS_COLLECTION).document(friendUid))
            .delete(userDoc(friendUid).collection(FRIENDS_COLLECTION).document(uid))
            .commit()
            .await()
    }
}
