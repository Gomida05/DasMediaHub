package com.das.forui.ui.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.das.forui.NavScreens


@Composable
fun WelcomePage(navController: NavController) {
    val visible = remember { mutableStateOf(false) }

    // Trigger the animations when the page loads
    LaunchedEffect(Unit) {
        visible.value = true
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible.value,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { it })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "✨ Welcome to MyCoolApp ✨",
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    AnimatedVisibility(
                        visible = visible.value,
                        enter = fadeIn(tween(1000)) + slideInHorizontally(initialOffsetX = { -it }),
                    ) {
                        Button(
                            onClick = {
                                visible.value = false
                                navController.navigate(NavScreens.LoginPage1.route)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text(
                                "I Already Have an Account",
                                color = Color(0xFF2575FC)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = visible.value,
                        enter = fadeIn(tween(1200)) + slideInHorizontally(initialOffsetX = { it }),
                    ) {
                        Button(
                            onClick = {
                                visible.value = false
                                navController.navigate(NavScreens.SignUpPage.route)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("I’m New Here", color = Color(0xFF2575FC))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = visible.value,
                        enter = fadeIn(tween(1200)) + slideInHorizontally(initialOffsetX = { it }),
                    ) {
                        Button(
                            onClick = {
                                visible.value = false
                                navController.run {
                                    popBackStack()
                                    navigate(NavScreens.Home.route)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("Continue without sign in", color = Color(0xFF2575FC))
                        }
                    }
                }
            }
        }
    }
}