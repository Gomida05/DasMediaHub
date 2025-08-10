package com.das.mediaHub.ui.account.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.das.mediaHub.data.model.user.SignUpUserData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class SignUpViewModel: ViewModel() {
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

        val passwordError = validatePassword(userDetails.password)
        if (passwordError != null) {
            _loading.value = false
            _error.value = passwordError
            return
        }

        val fullName = "${userDetails.firstName} ${userDetails.lastName?:""}"
        try {
            auth.createUserWithEmailAndPassword(userDetails.email, userDetails.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result.additionalUserInfo?.isNewUser == true) {
                        
                        val user = task.result?.user!!
                        val profile = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .setPhotoUri(userDetails.photoUri)
                            .build()

                        user.updateProfile(
                            profile
                        )
                        addUserDetails(
                            userDetails = userDetails.copy(password = user.uid),
                            onSuccess = {
                                updateResult(true)
                            },
                            onFailure = {
                                updateResult(false, it.message ?: "Failed to save user data")
                                user.delete()
                                _loading.value = false
                            }
                        )
                    } else if (task.result.additionalUserInfo?.isNewUser == false) {
                        updateResult(false, "User already exists with this email")
                    } else {
                        val exception = task.exception
                        val errorMessage = when (exception) {
                            is FirebaseAuthWeakPasswordException -> "Password should be at least 6 characters"
                            is FirebaseAuthUserCollisionException -> "User already exists with this email"
                            is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                            else -> exception?.message ?: "Unknown error occurred"
                        }

                        updateResult(false, errorMessage)
                    }
                }
        } catch (e: Exception) {
            updateResult(false, "Something went wrong: ${e.message}")
        }

    }

    private fun addUserDetails(
        userDetails: SignUpUserData,
        onSuccess: ()-> Unit,
        onFailure: (Exception)-> Unit
    ) {
        val lastName = userDetails.lastName?: "null"

        val feedbackData = hashMapOf(
            "firstName" to userDetails.firstName,
            "lastName" to lastName,
            "email" to userDetails.email,
            "uid" to userDetails.password
        )

        fireBaseStore.collection("users")
            .document(userDetails.password)
            .set(feedbackData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
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

    private fun updateResult(success: Boolean, errorMessage: String? = null) {
        _result.value = success
        _error.value = errorMessage
        _loading.value = false
    }



}