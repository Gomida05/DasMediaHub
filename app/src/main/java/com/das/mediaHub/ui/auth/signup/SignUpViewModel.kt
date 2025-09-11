package com.das.mediaHub.ui.auth.signup

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.auth.AuthResponse
import com.das.mediaHub.auth.FirebaseAuthClient
import com.das.mediaHub.data.model.user.SignUpUserData
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUpViewModel(application: Application): AndroidViewModel(application) {

    private val authClient by lazy {
        FirebaseAuthClient(application)
    }

    private val auth = Firebase.auth
    private val fireBaseStore = Firebase.firestore

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val errors: State<String?> = _error

    private val _result = mutableStateOf(false)
    val signUpResult: State<Boolean> = _result


    fun signUpUser(userDetails: SignUpUserData) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            val passwordError = validatePassword(userDetails.password)
            var user: FirebaseUser? = null
            if (passwordError != null) {
                _loading.value = false
                _error.value = passwordError
                return@launch
            }

            val fullName = "${userDetails.firstName} ${userDetails.lastName ?: ""}"
            try {
                val result = createAccount(userDetails)

                user = result.user ?: throw Exception("User creation failed")

                if (result.additionalUserInfo?.isNewUser == true) {
                    val profile = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .setPhotoUri(userDetails.photoUri)
                        .build()

                    user.updateProfile(profile).await()

                    addUserDetails(userDetails = userDetails.copy(password = user.uid))
                } else {
                    updateResult("User already exists with this email")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException -> "Password should be at least 6 characters"
                    is FirebaseAuthUserCollisionException -> "User already exists with this email"
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                    else -> e.message ?: "Unknown error occurred"
                }
                user?.delete()
                updateResult(errorMessage)
            }

        }
    }

    fun handleCredential() {
        _result.value = false
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = authClient
                .useGoogle()
                .first()
            handleResult(result)
        }
    }


    private suspend fun createAccount(userDetails: SignUpUserData): AuthResult = withContext(Dispatchers.IO) {
        auth.createUserWithEmailAndPassword(
            userDetails.email,
            userDetails.password
        ).await()
    }


    private suspend fun addUserDetails(
        userDetails: SignUpUserData
    ) {
        val lastName = userDetails.lastName?: ""

        val feedbackData = hashMapOf(
            "firstName" to userDetails.firstName,
            "lastName" to lastName,
            "email" to userDetails.email,
            "uid" to userDetails.password
        )

        fireBaseStore.collection("users")
            .document(userDetails.password)
            .set(feedbackData)
            .await()
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.length < 6 ->
                "Password should be at least 6 characters long"
            !password.any { it.isUpperCase() } ->
                "Password must contain at least one uppercase letter"
            !password.any { it.isLowerCase() } ->
                "Password must contain at least one lowercase letter"
            !password.any { it.isDigit() } ->
                "Password must contain at least one number"
            !password.any { "!@#$%^&*()_-+=<>?/{}~|".contains(it) } ->
                "Password must contain at least one special character"
            else -> null
        }
    }

    private fun updateResult(errorMessage: String? = null) {
        _result.value = false
        _error.value = errorMessage
        _loading.value = false
    }

    private suspend fun handleResult(result: AuthResponse) {
        when (result) {
            is AuthResponse.Success -> {
                val user = result.user
                val nameParts = user.displayName?.trim()?.split("\\s+".toRegex()) ?: listOf()
                val firstName = nameParts.firstOrNull()?:""
                val lastName = if (nameParts.size > 1) nameParts.last() else ""

                try {
                    addUserDetails(
                        SignUpUserData(
                            firstName,
                            lastName,
                            email = user.email?:"",
                            password = user.uid,
                            photoUri = user.photoUrl
                        )
                    )
                    _result.value = true
                } catch (e: Exception) {
                    _error.value = e.message
                }
            }
            is AuthResponse.Failed -> _error.value = result.message
        }
        _loading.value = false
    }

}