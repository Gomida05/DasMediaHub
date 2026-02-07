package com.das.mediaHub.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.NavScreens
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsPage(backStack: NavBackStack<NavKey>, currentUser: FirebaseUser?, signOut: () -> Unit) {

    val name = currentUser?.displayName ?: "Unnamed User"
    val email = currentUser?.email
    val showDeleteDialog = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Account",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                // Avatar and User Info Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(90.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Account Actions Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "SECURITY & ACCOUNT",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                    
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column {
                            AccountActionItem(
                                icon = Icons.Default.Lock,
                                text = "Change Password",
                                onClick = { backStack.add(NavScreens.ChangePassword) }
                            )
                            AccountActionItem(
                                icon = Icons.AutoMirrored.Default.Logout,
                                text = "Logout",
                                onClick = {
                                    signOut()
                                    showNotificationDialog = TopPopUp(
                                        message = "You have successfully signed out",
                                        icon = Icons.AutoMirrored.Default.Logout
                                    )
                                    backStack.removeLastOrNull()
                                }
                            )
                            AccountActionItem(
                                icon = Icons.Default.Delete,
                                text = "Delete Account",
                                textColor = MaterialTheme.colorScheme.error,
                                iconColor = MaterialTheme.colorScheme.error,
                                onClick = { showDeleteDialog.value = true }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDeleteDialog.value) {
        DeleteAccountDialog(
            email = email,
            currentUser = currentUser,
            onDismiss = { showDeleteDialog.value = false },
            onSuccess = {
                showDeleteDialog.value = false
                showNotificationDialog = TopPopUp(
                    message = "You have successfully deleted your account",
                    icon = Icons.Default.Security
                )
                backStack.removeLastOrNull()
            }
        )
    }
}

@Composable
fun AccountActionItem(
    icon: ImageVector,
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DeleteAccountDialog(
    email: String?,
    currentUser: FirebaseUser?,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun deleteAccountAction() {
        if (!email.isNullOrEmpty() && password.isNotBlank()) {
            isLoading = true
            errorMessage = null
            val credential = EmailAuthProvider.getCredential(email, password)
            currentUser?.reauthenticate(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser.delete().addOnCompleteListener { deleteTask ->
                        isLoading = false
                        if (deleteTask.isSuccessful) {
                            onSuccess()
                        } else {
                            errorMessage = deleteTask.exception?.message ?: "Failed to delete account"
                        }
                    }
                } else {
                    isLoading = false
                    errorMessage = "Incorrect password. Please try again."
                    password = ""
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Delete Account", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Are you sure you want to delete your account? This action is permanent and cannot be undone.")
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    isError = errorMessage != null,
                    label = { Text("Confirm Password") },
                    enabled = !isLoading,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { deleteAccountAction() }),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), strokeWidth = 3.dp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { deleteAccountAction() },
                enabled = password.isNotBlank() && !isLoading
            ) {
                Text("Delete Forever", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}
