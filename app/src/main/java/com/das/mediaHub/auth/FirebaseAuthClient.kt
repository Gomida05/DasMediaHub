package com.das.mediaHub.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.PasswordCredential
import com.das.mediaHub.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class FirebaseAuthClient(private val context: Context): AuthClient {

    private val author = Firebase.auth

    override fun createWithEmail(email: String, pass: String): Flow<AuthResponse> = callbackFlow {

        author.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task->
                if (task.isSuccessful) {
                    trySend(AuthResponse.Success(task.result.user!!))
                } else{
                    trySend(
                        AuthResponse.Failed(
                            task.exception?.message ?: "Error"
                        )
                    )
                }
            }
        awaitClose()
    }

    override fun logInEmail(email: String, pass: String): Flow<AuthResponse> = callbackFlow {

        author.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResponse.Success(task.result.user!!))
                } else {
                    trySend(
                        AuthResponse.Failed(
                            task.exception?.message ?: ""
                        )
                    )
                }
            }
        awaitClose()

    }

    override fun useGoogle(): Flow<AuthResponse> = callbackFlow{

        val webClientId = context.getString(R.string.default_web_client_id)
        launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setNonce(customNonce())
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                when (val credential = result.credential) {

                    is GoogleIdTokenCredential -> {
                        val respond = googleIdTokenCredential(credential).first()
                        trySend(respond)
                    }
                    is PasswordCredential -> {
                        val respond = passwordCredential(credential).first()
                        trySend(respond)
                    }
                    is CustomCredential -> {
                        val response = customCredential(credential).first()
                        trySend(response)
                    }
                    else -> {
                        trySend(AuthResponse.Failed("Unsupported credential type received: ${credential::class.java.name}"))
                    }
                }

            } catch (e: Exception) {
                trySend(AuthResponse.Failed(e.message ?: ""))
            }
            close()
        }
        awaitClose()
    }

    fun googleIdTokenCredential(credential: GoogleIdTokenCredential): Flow<AuthResponse> = callbackFlow {

        val idToken = credential.idToken
        if (idToken.isNotEmpty()) {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

            author.signInWithCredential(firebaseCredential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(AuthResponse.Success(task.result.user!!))
                    } else {
                        trySend(AuthResponse.Failed(task.exception?.message ?: ""))
                    }
                    close()
                }
        } else {
            trySend(AuthResponse.Failed("No ID token received"))
            close()
        }
        awaitClose()
    }


    override fun passwordCredential(credential: PasswordCredential): Flow<AuthResponse> = callbackFlow {

        val email = credential.id
        val pwd = credential.password
        if (email.isNotEmpty() && pwd.isNotEmpty()) {
            author.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(AuthResponse.Success(task.result.user!!))
                    } else {
                        trySend(AuthResponse.Failed(task.exception?.message ?: ""))
                    }
                }
            close()
        } else {
            trySend(AuthResponse.Failed("Invalid password credential received"))
            close()
        }
        awaitClose()
    }


    override fun customCredential(credential: CustomCredential): Flow<AuthResponse> = callbackFlow {
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            try {
                val googleIdTokens = GoogleIdTokenCredential
                    .createFrom(credential.data)

                val fireBaseCredential = GoogleAuthProvider
                    .getCredential(googleIdTokens.idToken, null)

                author.signInWithCredential(fireBaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            trySend(AuthResponse.Success(task.result.user!!))

                        } else {
                            trySend(AuthResponse.Failed(task.exception?.message ?: ""))
                        }
                        close()
                    }
            } catch (e: GoogleIdTokenParsingException) {
                trySend(AuthResponse.Failed(e.message ?: ""))
                close()
            }
        } else {
            trySend(AuthResponse.Failed("Invalid credential type"))
            close()
        }
        awaitClose()

    }

    private fun customNonce(): String {

        val rawBytes = UUID.randomUUID()
            .toString()
            .toByteArray()

        val hashBytes = MessageDigest.getInstance("SHA-256")
            .digest(rawBytes)

        return hashBytes.fold("") { str, it ->
            str + "%02x".format(it)
        }
    }


}