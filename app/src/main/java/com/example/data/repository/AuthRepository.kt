package com.example.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

  val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
      trySend(firebaseAuth.currentUser)
    }
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
  }

  val currentUser: FirebaseUser?
    get() = auth.currentUser

  suspend fun signInWithGoogleCredential(context: Context): Result<FirebaseUser> {
    return try {
      val credentialManager = CredentialManager.create(context)
      val serverClientId = context.getString(R.string.default_web_client_id)

      val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .setAutoSelectEnabled(false)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result = credentialManager.getCredential(
        request = request,
        context = context
      )

      val credential = result.credential
      if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(firebaseCredential).await()
        val user = authResult.user
        if (user != null) {
          Result.success(user)
        } else {
          Result.failure(Exception("Failed to get Firebase User from Google credential."))
        }
      } else {
        Result.failure(Exception("Unsupported credential type returned."))
      }
    } catch (e: GetCredentialCancellationException) {
      Result.failure(Exception("Sign in cancelled."))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
    return try {
      val result = auth.signInWithEmailAndPassword(email, pass).await()
      val user = result.user ?: throw Exception("Authentication returned null user.")
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser> {
    return try {
      val result = auth.createUserWithEmailAndPassword(email, pass).await()
      val user = result.user ?: throw Exception("Account creation returned null user.")
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun signOut() {
    auth.signOut()
  }

  suspend fun deleteAccount(): Result<Unit> {
    return try {
      val user = auth.currentUser ?: throw Exception("No authenticated user to delete.")
      user.delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
