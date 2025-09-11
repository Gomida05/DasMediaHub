package com.das.mediaHub.auth

import com.google.firebase.auth.FirebaseUser

sealed interface AuthResponse {
    data class Success(val user: FirebaseUser): AuthResponse
    data class Failed(val message: String): AuthResponse
}