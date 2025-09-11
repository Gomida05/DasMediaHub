package com.das.mediaHub.auth

import androidx.credentials.CustomCredential
import androidx.credentials.PasswordCredential
import kotlinx.coroutines.flow.Flow

interface AuthClient {

    fun createWithEmail(email: String, pass: String): Flow<AuthResponse>

    fun logInEmail(email: String, pass: String): Flow<AuthResponse>

    fun useGoogle(): Flow<AuthResponse>

    fun passwordCredential(credential: PasswordCredential): Flow<AuthResponse>

    fun customCredential(credential: CustomCredential): Flow<AuthResponse>
}