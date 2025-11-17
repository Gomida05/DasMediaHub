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

        val currentUser = remember { mutableStateOf(currentUser) }

        DisposableEffect(Unit) {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                currentUser.value = firebaseAuth.currentUser
            }
            addAuthStateListener(listener)
            onDispose {
                removeAuthStateListener(listener)
            }
        }

        return currentUser.value
    }
}