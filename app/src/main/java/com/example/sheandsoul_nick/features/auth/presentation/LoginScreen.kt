package com.example.sheandsoul_nick.features.auth.presentation

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import com.example.sheandsoul_nick.ui.components.SocialSignInButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onGoogleSignInSuccess: () -> Unit = {},
    onForgotPasswordClicked: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authResult by authViewModel.authResult.observeAsState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("GoogleSignIn", "Result received. Result Code: ${result.resultCode}") // 👈 ADD LOG

        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken

                if (idToken != null) {
                    Log.d("GoogleSignIn", "SUCCESS! Got idToken.") // 👈 ADD LOG
                    authViewModel.signInWithGoogle(idToken)
                } else {
                    Log.e("GoogleSignIn", "ERROR: idToken is null!") // 👈 ADD LOG
                    isLoading = false
                }

            } catch (e: ApiException) {
                // 👇 THIS IS THE MOST IMPORTANT LOG!
                Log.e("GoogleSignIn", "ApiException: statusCode=${e.statusCode} message=${e.message}")
                isLoading = false
                Toast.makeText(context, "Google Sign-In failed. Code: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.w("GoogleSignIn", "Result was not OK. User might have cancelled.") // 👈 ADD LOG
            isLoading = false
        }
    }

    LaunchedEffect(authResult) {
        when (val result = authResult) {
            is AuthResult.Success -> {
                isLoading = false
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }
            is AuthResult.SuccessGoogle -> {
                isLoading = false
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                onGoogleSignInSuccess()
            }
            is AuthResult.Error -> {
                isLoading = false
                Toast.makeText(context, "Error: ${result.errorMessage}", Toast.LENGTH_LONG).show()
            }
            is AuthResult.Loading -> {
                isLoading = true
            }
            null -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_sheandsoul_text),
                contentDescription = "Logo",
                modifier = Modifier
                    .width(138.dp)
                    .height(50.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))

            Text("Login", fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            Text("Welcome back to app", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Forgot Password ?", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End)
                .clickable { onForgotPasswordClicked() })

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalWaveButton(
                onClick = { authViewModel.loginUser(email, password) },
                text = "Login",
                startColor = Color(0xFFBBBDFF),
                endColor = Color(0xFF9092FF),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                cornerRadius = 12.dp,
                useVerticalGradient = true
            )

            // ==================================================================
            // ## START OF THE FIX ##
            // The following sections were moved back inside the main Column
            // ==================================================================
            Spacer(modifier = Modifier.height(30.dp))

            SocialSignInButton(
                text = "Continue with Google",
                iconResId = R.drawable.ic_google,
                onClick = {
                    isLoading = true
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }
                }
            )


            // Use a Spacer with weight to push the final text to the bottom
            Spacer(modifier = Modifier.height(22.dp))

            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Gray, fontSize = 14.sp)) {
                    append("Not Registered Yet ? ")
                }
                pushStringAnnotation(tag = "SIGNUP", annotation = "Navigates to the Signup screen")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                ) {
                    append("Sign Up")
                }
                pop()
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "SIGNUP", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onNavigateToSignup()
                        }
                }
            )
            // ==================================================================
            // ## END OF THE FIX ##
            // ==================================================================
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
