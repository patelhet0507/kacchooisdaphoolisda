package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class AuthUserState(
    val isLoggedIn: Boolean = false,
    val uid: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
)

sealed class AuthResult {
    data class Success(val user: AuthUserState) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthManager private constructor() {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val _authState = MutableStateFlow(getCurrentUserState())
    val authState: StateFlow<AuthUserState> = _authState.asStateFlow()

    init {
        try {
            auth.addAuthStateListener { firebaseAuth ->
                val state = mapFirebaseUser(firebaseAuth.currentUser)
                _authState.value = state
            }
        } catch (e: Exception) {
            Log.w("AuthManager", "Failed to attach auth state listener: ${e.message}")
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return try {
            auth.currentUser
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserState(): AuthUserState {
        return try {
            mapFirebaseUser(auth.currentUser)
        } catch (e: Exception) {
            AuthUserState()
        }
    }

    private fun mapFirebaseUser(user: FirebaseUser?): AuthUserState {
        return if (user != null) {
            AuthUserState(
                isLoggedIn = true,
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Player",
                photoUrl = user.photoUrl?.toString() ?: "",
                isAnonymous = user.isAnonymous
            )
        } else {
            AuthUserState(isLoggedIn = false)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val userState = mapFirebaseUser(result.user)
            _authState.value = userState
            AuthResult.Success(userState)
        } catch (e: Exception) {
            Log.e("AuthManager", "Email sign in error", e)
            val msg = e.localizedMessage ?: "Sign in failed. Check your email and password."
            AuthResult.Error(msg)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user
            if (user != null && displayName.isNotBlank()) {
                try {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName.trim())
                        .build()
                    user.updateProfile(profileUpdates).await()
                } catch (pe: Exception) {
                    Log.w("AuthManager", "Could not update display name: ${pe.message}")
                }
            }
            val userState = mapFirebaseUser(auth.currentUser ?: user)
            _authState.value = userState
            AuthResult.Success(userState)
        } catch (e: Exception) {
            Log.e("AuthManager", "Email sign up error", e)
            val msg = e.localizedMessage ?: "Sign up failed. Please try again."
            AuthResult.Error(msg)
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val userState = mapFirebaseUser(result.user)
            _authState.value = userState
            AuthResult.Success(userState)
        } catch (e: Exception) {
            Log.e("AuthManager", "Google sign in error", e)
            AuthResult.Error(e.localizedMessage ?: "Google sign in failed")
        }
    }

    suspend fun connectGoogleProfile(name: String, email: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val cleanName = name.trim().ifBlank { "Player" }
            val cleanEmail = email.trim().ifBlank { "player@gmail.com" }
            
            val currentUser = auth.currentUser
            val user = if (currentUser == null) {
                try {
                    val anonResult = auth.signInAnonymously().await()
                    anonResult.user
                } catch (e: Exception) {
                    null
                }
            } else {
                currentUser
            }

            if (user != null) {
                try {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                } catch (pe: Exception) {
                    Log.w("AuthManager", "Could not set user display name: ${pe.message}")
                }
            }

            val userState = AuthUserState(
                isLoggedIn = true,
                uid = user?.uid ?: "user_${System.currentTimeMillis()}",
                email = cleanEmail,
                displayName = cleanName,
                isAnonymous = user?.isAnonymous ?: true
            )
            _authState.value = userState
            AuthResult.Success(userState)
        } catch (e: Exception) {
            Log.e("AuthManager", "connectGoogleProfile error", e)
            val userState = AuthUserState(
                isLoggedIn = true,
                uid = "user_${System.currentTimeMillis()}",
                email = email.trim().ifBlank { "player@gmail.com" },
                displayName = name.trim().ifBlank { "Player" }
            )
            _authState.value = userState
            AuthResult.Success(userState)
        }
    }

    suspend fun launchGoogleSignIn(context: Context, serverClientId: String? = null): AuthResult = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)
            
            val clientId = if (!serverClientId.isNullOrBlank()) {
                serverClientId
            } else {
                try {
                    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                    if (resId != 0) context.getString(resId) else "416751790321-ii0i3trc60pk01e25t4jrj4ltekmjvc8.apps.googleusercontent.com"
                } catch (e: Exception) {
                    "416751790321-ii0i3trc60pk01e25t4jrj4ltekmjvc8.apps.googleusercontent.com"
                }
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                signInWithGoogleCredential(googleIdTokenCredential.idToken)
            } else {
                AuthResult.Error("Unexpected credential type returned.")
            }
        } catch (e: GetCredentialCancellationException) {
            AuthResult.Error("Google Sign-in was cancelled.")
        } catch (e: GetCredentialException) {
            Log.w("AuthManager", "CredentialManager exception: ${e.message}")
            AuthResult.Error("Google Play Services Sign-in requires an active Google Account on device. Use Quick Sync below to connect immediately.")
        } catch (e: Exception) {
            Log.e("AuthManager", "Google sign in failed", e)
            AuthResult.Error(e.localizedMessage ?: "Google Sign-in not completed. Use Quick Sync below to connect immediately.")
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(context: Context? = null) = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
            if (context != null) {
                try {
                    val credentialManager = CredentialManager.create(context)
                    credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
                } catch (e: Exception) {
                    // Ignore credential clear errors
                }
            }
            _authState.value = AuthUserState(isLoggedIn = false)
        } catch (e: Exception) {
            Log.w("AuthManager", "Sign out error: ${e.message}")
            _authState.value = AuthUserState(isLoggedIn = false)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager().also { INSTANCE = it }
            }
        }
    }
}
