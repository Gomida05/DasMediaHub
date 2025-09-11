package com.das.mediaHub.data.model.user

import android.net.Uri

data class SignUpUserData(
    val firstName: String,
    val lastName: String? = null,
    val email: String,
    /**
     * This represent the user's uid id and not raw password
     */
    val password: String,
    val photoUri: Uri? = Uri.EMPTY
)