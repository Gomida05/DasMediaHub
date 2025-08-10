package com.das.mediaHub.ui.settings.userSettings



/*

@Composable
fun GoogleSignInButton(
    context: Context,
    onResult: (Boolean, String?) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val tasks = getGoogleSignInClient(context).silentSignIn().result
        try {
            val account = tasks.account
            val credential = GoogleAuthProvider.getCredential(account?.name, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, task.exception?.message)
                    }
                }
        } catch (e: ApiException) {
            onResult(false, e.message)
        }
    }

    Button(
        onClick = {
            val signInClient = getGoogleSignInClient(context)
            val signInIntent = signInClient.signInIntent
            launcher.launch(signInIntent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(50)
    ) {
        Icon(painterResource(id = R.drawable.ic_google), contentDescription = "Google", tint = Color.Unspecified)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Continue with Google", color = Color.Black)
    }
}

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)) // Must match Firebase OAuth client
        .requestEmail()
        .build()

    return GoogleSignIn.getClient(context, gso)
}

*/