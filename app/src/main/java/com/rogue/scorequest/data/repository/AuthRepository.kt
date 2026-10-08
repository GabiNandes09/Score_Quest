package com.rogue.scorequest.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.rogue.scorequest.domain.model.AuthUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth
) {

    fun authStateChanges(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser?.toAuthUser()) }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogleIdToken(idToken: String): AuthUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        return result.user?.toAuthUser() ?: error("Login com Google falhou: usuário nulo.")
    }

    fun signOut() = firebaseAuth.signOut()

    private fun FirebaseUser.toAuthUser() = AuthUser(
        uid = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl?.toString()
    )
}
