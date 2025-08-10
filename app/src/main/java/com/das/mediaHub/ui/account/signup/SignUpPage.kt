package com.das.mediaHub.ui.account.signup

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.das.mediaHub.NavScreens
import com.das.mediaHub.data.model.user.SignUpUserData


@Composable
fun SignUpPage(navController: NavController) {

    val viewModel = viewModel<SignUpViewModel>()
    val signUpResult by viewModel.signUpResult
    val isLoading by viewModel.loading
    val errorFound by viewModel.errors

    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }


    val isFormValid = firstName.isNotBlank()
            && email.isNotBlank()
            && password.isNotBlank()
            && confirmPassword == password


    if (signUpResult) {
        navController.run {
            popBackStack()
            navigate(route = NavScreens.Home.route)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
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
                Text(
                    text = "Create Account",
                    fontSize = 28.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                SignUpUserInputs(
                    isLoading = isLoading,
                    firstName = firstName,
                    onFirstNameChange = { firstName = it },
                    lastName = lastName,
                    onLastNameChange = { lastName = it },
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    onDoneClick = {
                        if (isFormValid) {
                            viewModel.signUpUser(
                                SignUpUserData(
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = email,
                                    password = password
                                )
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                ElevatedButton(
                    onClick = {
                        viewModel.signUpUser(
                            SignUpUserData(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                password = password
                            )
                        )
                    },
                    enabled = isFormValid && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(20),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        )
                    } else {
                        Text(text = "Sign Up")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
     /*           ElevatedButton(
                    onClick = {
//                    onSignUpClick(email.value, password.value)
                    },
                    shape = RoundedCornerShape(20),
                ) {
                    Icon(
                        painter = painterResource(id=R.drawable.google_icon),
                        contentDescription = "sign up with google",
                        tint = null,
                        modifier = Modifier
                            .height(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign-Up With Google"
                    )
                }
                */

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        navController.navigateUp()
                    }
                ) {
                    Text(text = "Already have an account? Login")
                }


                errorFound?.let {
                    password = ""
                    confirmPassword = ""
                    Text(text = it, style = MaterialTheme.typography.bodyMedium
                        .copy(color = MaterialTheme.colorScheme.error)
                    )
                }
            }
        }
    }
}









