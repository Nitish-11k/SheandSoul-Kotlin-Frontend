package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OtpScreen(
    onVerificationSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val otpDigits = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val focusManager = LocalFocusManager.current
    var countdown by remember { mutableStateOf(60) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val otpResult by authViewModel.otpResult.observeAsState()
    val resendResult by authViewModel.resendResult.observeAsState()

    // Countdown timer effect
    LaunchedEffect(key1 = countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    // Handles the OTP verification result from the ViewModel
    LaunchedEffect(otpResult) {
        when (val result = otpResult) {
            is AuthResult.Success -> {
                isLoading = false
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                onVerificationSuccess()
            }
            // ADD THIS BRANCH (It won't happen here, but is needed for the code to compile)
            is AuthResult.SuccessGoogle -> {
                isLoading = false
                // This case is not expected here, but we can navigate just in case
                onVerificationSuccess()
            }
            is AuthResult.Error -> {
                isLoading = false
                Toast.makeText(context, "Error: ${result.errorMessage}", Toast.LENGTH_LONG).show()
            }
            is AuthResult.Loading -> isLoading = true
            null -> {}
        }
    }

    // Handles the RESEND OTP result from the ViewModel
    LaunchedEffect(resendResult) {
        when (val result = resendResult) {
            is AuthResult.Success -> {
                isLoading = false
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            }
            is AuthResult.Error -> {
                isLoading = false
                Toast.makeText(context, "Error: ${result.errorMessage}", Toast.LENGTH_LONG).show()
            }
            is AuthResult.Loading -> isLoading = true
            null -> {}
            is AuthResult.SuccessGoogle -> TODO()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_sheandsoul_text),
                contentDescription = "She & Soul Logo",
                modifier = Modifier
                    .width(130.dp)
                    .height(50.dp)
            )
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Verify Your Email",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Please enter the 6 digit code sent to your email.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // OTP Input Boxes
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..5) {
                    OtpInputBox(
                        value = otpDigits[i],
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                otpDigits[i] = newValue
                                if (newValue.isNotEmpty() && i < 5) {
                                    focusRequesters[i + 1].requestFocus()
                                } else if (newValue.isEmpty() && i > 0) {
                                    focusRequesters[i - 1].requestFocus()
                                }
                            }
                        },
                        modifier = Modifier.focusRequester(focusRequesters[i])
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val fullOtp = otpDigits.joinToString("")
            HorizontalWaveButton(
                onClick = {
                    if (fullOtp.length == 6) {
                        authViewModel.verifyOtp(fullOtp)
                    } else {
                        Toast.makeText(context, "Please enter all 6 digits.", Toast.LENGTH_SHORT).show()
                    }
                },
                text = "Verify",
                startColor = Color(0xFFBBBDFF),
                endColor = Color(0xFF9092FF),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            val resendText = buildAnnotatedString {
                append("Didn't receive code? ")
                if (countdown > 0) {
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append("Resend in ${countdown}s")
                    }
                } else {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        pushStringAnnotation("RESEND", "resend_otp")
                        append("Resend OTP")
                        pop()
                    }
                }
            }

            ClickableText(
                text = resendText,
                onClick = { offset ->
                    resendText.getStringAnnotations("RESEND", offset, offset)
                        .firstOrNull()?.let {
                            countdown = 60 // Reset timer
                            authViewModel.resendOtp()
                        }
                }
            )
            Spacer(modifier =  Modifier.height(24.dp))
        }

        // Show this overlay when isLoading is true
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false, onClick = {}), // Prevent clicks behind
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}


@Composable
fun OtpInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .size(48.dp)
            .border(
                width = 1.dp,
                color = if (value.isNotEmpty()) Color(0xFF9092FF) else Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OtpScreenPreview() {
    OtpScreen(onVerificationSuccess = {})
}