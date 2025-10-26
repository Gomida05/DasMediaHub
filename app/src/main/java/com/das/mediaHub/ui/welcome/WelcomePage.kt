package com.das.mediaHub.ui.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.das.mediaHub.NavScreens
import com.das.mediaHub.R

@Composable
fun WelcomePage(
    navController: NavController,
    onSignInAnonymously: () -> Unit
) {
    val visible = remember { mutableStateOf(false) }



    LaunchedEffect(Unit) {
        visible.value = true
    }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF6A11CB), Color(0xFF25FCBF))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible.value,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { it })
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(24.dp)
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // App Title
                    Text(
                        text = "✨ DasMediaHub ✨",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    Image(
                        painter = painterResource(R.mipmap.launcher_foreground),
                        contentDescription = "App icon",
                        modifier = Modifier
                            .clip(RoundedCornerShape(17))
                            .background(Color.Green)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Buttons
                    val buttonColors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    val buttonTextColor = Color(0xFF2575FC)

                    AnimatedVisibility(
                        visible = visible.value,
                        enter = fadeIn(tween(1000)) + slideInHorizontally(initialOffsetX = { -it })
                    ) {
                        Button(
                            onClick = { navController.navigate(NavScreens.SignInPage.route) },
                            colors = buttonColors,
                            shape = RoundedCornerShape(20),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("I Already Have an Account", color = buttonTextColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = visible.value,
                        enter = fadeIn(tween(1200)) + slideInHorizontally(initialOffsetX = { it })
                    ) {
                        Button(
                            onClick = { navController.navigate(NavScreens.SignUpPage.route) },
                            colors = buttonColors,
                            shape = RoundedCornerShape(20),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("I’m New Here", color = buttonTextColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = visible.value,
                        enter = fadeIn(tween(1400)) + slideInHorizontally(initialOffsetX = { -it })
                    ) {
                        Button(
                            onClick = { onSignInAnonymously() },
                            colors = buttonColors,
                            shape = RoundedCornerShape(20),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Continue without Sign In", color = buttonTextColor)
                        }
                    }
                }

        }
    }
}
