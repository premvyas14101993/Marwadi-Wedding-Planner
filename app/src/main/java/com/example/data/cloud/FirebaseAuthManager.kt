package com.example.data.cloud

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

data class WeddingUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String? = null,
    val isGoogleUser: Boolean = true
)

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: WeddingUser) : AuthState()
    data class Unauthenticated(val message: String? = null) : AuthState()
    data class Error(val errorMessage: String) : AuthState()
}

class FirebaseAuthManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("wedding_user_auth_prefs", Context.MODE_PRIVATE)

    init {
        FirebaseInitializer.init(context)
    }

    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.w("FirebaseAuthManager", "FirebaseAuth getInstance fallback: ${e.message}")
        null
    }

    private val credentialManager = CredentialManager.create(context)

    private val _authState = MutableStateFlow<AuthState>(getInitialAuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser: WeddingUser?
        get() = when (val state = _authState.value) {
            is AuthState.Authenticated -> state.user
            else -> null
        }

    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                val fbUser = firebaseAuth.currentUser
                if (fbUser != null) {
                    val user = WeddingUser(
                        uid = fbUser.uid,
                        displayName = fbUser.displayName ?: fbUser.email ?: "Family Member",
                        email = fbUser.email,
                        photoUrl = fbUser.photoUrl?.toString()
                    )
                    saveUserToPrefs(user)
                    _authState.value = AuthState.Authenticated(user)
                } else if (!hasSavedUser()) {
                    _authState.value = AuthState.Unauthenticated()
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Failed to add auth listener", e)
        }
    }

    private fun getInitialAuthState(): AuthState {
        val fbUser = auth?.currentUser
        if (fbUser != null) {
            return AuthState.Authenticated(
                WeddingUser(
                    uid = fbUser.uid,
                    displayName = fbUser.displayName ?: fbUser.email ?: "Family Member",
                    email = fbUser.email,
                    photoUrl = fbUser.photoUrl?.toString()
                )
            )
        }
        val savedUid = prefs.getString("user_uid", null)
        if (savedUid != null) {
            return AuthState.Authenticated(
                WeddingUser(
                    uid = savedUid,
                    displayName = prefs.getString("user_name", "Family Member"),
                    email = prefs.getString("user_email", null),
                    photoUrl = prefs.getString("user_photo", null)
                )
            )
        }
        return AuthState.Unauthenticated()
    }

    private fun hasSavedUser(): Boolean = prefs.contains("user_uid")

    private fun saveUserToPrefs(user: WeddingUser) {
        prefs.edit()
            .putString("user_uid", user.uid)
            .putString("user_name", user.displayName)
            .putString("user_email", user.email)
            .putString("user_photo", user.photoUrl)
            .apply()
    }

    private fun clearSavedUser() {
        prefs.edit().clear().apply()
    }

    fun signInWithGoogleAccount(
        email: String,
        displayName: String? = null,
        photoUrl: String? = null
    ): WeddingUser {
        val cleanEmail = email.trim().lowercase()
        val formattedName = displayName?.trim()?.ifBlank { null }
            ?: cleanEmail.substringBefore("@")
                .split(".", "_", "-")
                .filter { it.isNotBlank() }
                .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                .ifBlank { "Google User" }

        val uid = "google_${cleanEmail.replace("@", "_").replace(".", "_")}"
        val user = WeddingUser(
            uid = uid,
            displayName = formattedName,
            email = cleanEmail,
            photoUrl = photoUrl ?: "https://lh3.googleusercontent.com/a/default-user=s96-c",
            isGoogleUser = true
        )
        saveUserToPrefs(user)
        _authState.value = AuthState.Authenticated(user)
        return user
    }

    suspend fun signInWithGoogle(webClientId: String? = null): Result<WeddingUser> {
        _authState.value = AuthState.Loading
        return try {
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val effectiveClientId = webClientId?.ifBlank { null }
                ?: "517721338245-gn0bvrjqgli0pbs4gdka9lb18nu4q66o.apps.googleusercontent.com"

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(effectiveClientId)
                .setNonce(hashedNonce)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                var authenticatedUser = WeddingUser(
                    uid = googleIdTokenCredential.id.ifBlank { "google_${System.currentTimeMillis()}" },
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.id,
                    email = googleIdTokenCredential.id,
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    isGoogleUser = true
                )

                // Try linking with Firebase Auth if available
                if (auth != null && idToken.isNotBlank()) {
                    try {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = auth.signInWithCredential(firebaseCredential).await()
                        val fbUser = authResult.user
                        if (fbUser != null) {
                            authenticatedUser = WeddingUser(
                                uid = fbUser.uid,
                                displayName = fbUser.displayName ?: authenticatedUser.displayName,
                                email = fbUser.email ?: authenticatedUser.email,
                                photoUrl = fbUser.photoUrl?.toString() ?: authenticatedUser.photoUrl,
                                isGoogleUser = true
                            )
                        }
                    } catch (fbEx: Exception) {
                        Log.w("FirebaseAuthManager", "Firebase link warning (continuing with Google Identity): ${fbEx.message}")
                    }
                }

                saveUserToPrefs(authenticatedUser)
                _authState.value = AuthState.Authenticated(authenticatedUser)
                Result.success(authenticatedUser)
            } else {
                val fallbackUser = WeddingUser(
                    uid = "user_${System.currentTimeMillis()}",
                    displayName = "Family Member",
                    email = "family@wedding.local",
                    photoUrl = null,
                    isGoogleUser = false
                )
                saveUserToPrefs(fallbackUser)
                _authState.value = AuthState.Authenticated(fallbackUser)
                Result.success(fallbackUser)
            }
        } catch (e: GetCredentialCancellationException) {
            _authState.value = AuthState.Unauthenticated("Sign-in cancelled")
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.w("FirebaseAuthManager", "CredentialManager: ${e.message}")
            val msg = if (e.message?.contains("No credentials", ignoreCase = true) == true ||
                e.javaClass.simpleName.contains("NoCredential", ignoreCase = true)
            ) {
                "No Google account available on this device. You can sign in using Family Name."
            } else {
                e.message ?: "Google Sign-In failed"
            }
            _authState.value = AuthState.Error(msg)
            Result.failure(e)
        } catch (e: Exception) {
            Log.w("FirebaseAuthManager", "Sign-in exception: ${e.message}")
            _authState.value = AuthState.Error(e.message ?: "Authentication error")
            Result.failure(e)
        }
    }

    fun signInAsFamilyMember(displayName: String, email: String? = null): WeddingUser {
        val cleanName = displayName.trim().ifBlank { "Family Member" }
        val cleanEmail = email?.trim()?.ifBlank { null }
        val uid = "fam_${UUID.randomUUID().toString().take(8)}"
        val user = WeddingUser(
            uid = uid,
            displayName = cleanName,
            email = cleanEmail ?: "$cleanName@wedding.family",
            photoUrl = null,
            isGoogleUser = false
        )
        saveUserToPrefs(user)
        _authState.value = AuthState.Authenticated(user)
        return user
    }

    fun signOut() {
        try {
            clearSavedUser()
            auth?.signOut()
            _authState.value = AuthState.Unauthenticated()
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Sign out error", e)
        }
    }
}
