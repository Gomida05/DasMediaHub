package com.das.mediaHub.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object MyFirebase {

    @Composable
    fun FirebaseAuth.rememberFirebaseUser(): FirebaseUser? {

        val userState = remember { mutableStateOf(this.currentUser) }

        DisposableEffect(this) {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                userState.value = firebaseAuth.currentUser
            }
            addAuthStateListener(listener)
            onDispose {
                removeAuthStateListener(listener)
            }
        }

        return userState.value
    }
}