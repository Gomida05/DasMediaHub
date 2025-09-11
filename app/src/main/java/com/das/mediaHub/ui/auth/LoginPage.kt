package com.das.mediaHub.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDataType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.onAutofillText
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.das.mediaHub.NavScreens
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.data.model.user.LoginUserData
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginPage(
    navController: NavController,
    auth: FirebaseAuth
) {
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF6A11CB), Color(0xFF25FCBF))
                    )
                )
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                // App name
                Text(
                    text = "✨ DasMediaHub ✨",
                    fontSize = 30.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Email Field
                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentType = ContentType.EmailAddress
                            contentDataType = ContentDataType.Text
                            onAutofillText {
                                email.value = it.text
                                true
                            }
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                OutlinedTextField(
                    value = password.value,
                    onValueChange = {
                        password.value = it
                        message = null
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    isError = message != null,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "",
                                tint = Color.White
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val user = LoginUserData(email = email.value, password = password.value)
                            loginUser(auth,user) { success, error ->
                                if (success) {
                                    message = null
                                    showNotificationDialog = TopPopUp(
                                        message = "You have successfully Login",
                                        icon = Icons.AutoMirrored.Default.Logout
                                    )
                                    navController.popBackStack()
                                } else {
                                    message = error
                                }
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Login Button
                ElevatedButton(
                    onClick = {
                        val user = LoginUserData(email = email.value, password = password.value)
                        loginUser(auth = auth, userDetails = user) { success, error ->
                            if (success) {
                                message = null
                                showNotificationDialog = TopPopUp(
                                    message = "You have successfully Login",
                                    icon = Icons.AutoMirrored.Default.Logout
                                )
                                navController.popBackStack()
                            } else {
                                message = error
                            }
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(20),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Login", color = Color(0xFF2575FC), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign up text
                TextButton(
                    onClick = {
                        navController.navigate(NavScreens.SignUpPage.route)
                    }
                ) {
                    Text("Don't have an account? Sign up", color = Color.White)
                }

                message?.let {
                    Text(text = it, color = Color.Red)
                    password.value = ""
                }
            }

        }
    }
}



private fun loginUser(
    auth: FirebaseAuth,
    userDetails: LoginUserData,
    onResult: (Boolean, String?) -> Unit
) {

    auth.signInWithEmailAndPassword(userDetails.email, userDetails.password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {

                onResult(true, null)
            } else {
                onResult(false, task.exception?.message)
            }
        }
}