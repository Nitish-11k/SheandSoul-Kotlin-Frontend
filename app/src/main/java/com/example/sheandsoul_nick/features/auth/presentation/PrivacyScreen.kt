// file: app/src/main/java/com/example/sheandsoul_nick/features/auth/presentation/PrivacyScreen.kt

package com.example.sheandsoul_nick.features.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton

@Composable
fun PrivacyScreen(
    onAgree: () -> Unit = {}
) {
    // State to control the visibility of the bottom sheet
    var showPolicySheet by remember { mutableStateOf(false) }

    // When showPolicySheet becomes true, this composable will be displayed
    if (showPolicySheet) {
        PrivacyAndTermsSheet(onDismiss = { showPolicySheet = false })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_privacybg),
            contentDescription = "Blurred She & Soul Logo",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.7f)
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            contentScale = ContentScale.Fit
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFBBBDFF), Color(0xFF9092FF))
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Your privacy is our top priority.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "We’re committed to keeping your data safe and using it only to improve your experience on She&Soul.",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                // This is the new clickable text
                // Inside PrivacyScreen()
                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White, fontSize = 14.sp)) {
                        append("Please review our ")
                    }
                    pushStringAnnotation(tag = "POLICY", annotation = "policy")
                    withStyle(
                        style = SpanStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline // 👈 Highlight as link
                        )
                    ) {
                        append("Terms of Use & Privacy Policy")
                    }
                    pop()
                    withStyle(style = SpanStyle(color = Color.White, fontSize = 14.sp)) {
                        append(".")
                    }
                }

                ClickableText(
                    text = annotatedString,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "POLICY", start = offset, end = offset)
                            .firstOrNull()?.let {
                                showPolicySheet = true
                            }
                    },
                    style = TextStyle(textAlign = TextAlign.Center),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalWaveButton(
                    onClick = { onAgree() },
                    text = "Agree & Continue",
                    startColor = Color.White,
                    endColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    cornerRadius = 16.dp
                )
            }
        }
    }
}

/**
 * A reusable ModalBottomSheet to display the Terms of Use and Privacy Policy.
 * It can be called from any screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyAndTermsSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current // 👈 Get context for launching intent

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "📜 Terms of Use – She and Soul",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "By using She and Soul, you agree to use the app legally and respectfully, keep your account details private, and avoid copying, misusing, or harming the app or its community. We may suspend or terminate accounts that break these rules. Please note that the app is provided “as is” without any guarantees or warranties.",
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "🔒 Privacy Policy – She and Soul",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // ✅ Replace static Text with ClickableText for email
            val email = "support@sheandsoul.co.in"
            val privacyText = "At She and Soul, we care deeply about your privacy. We may collect basic details such as your name, email, usage information, and any content you choose to share. This information is used only to operate the app, improve features, and keep the experience safe for everyone. We never sell your personal data. While we take steps to protect your information, no system is completely secure. You also have the right to request access to or update your personal data at any time. If you have questions or concerns, please reach us at "

            val annotatedPrivacy = buildAnnotatedString {
                append(privacyText)
                pushStringAnnotation(tag = "EMAIL", annotation = email)
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(email)
                }
                pop()
                append(".")
            }

            ClickableText(
                text = annotatedPrivacy,
                onClick = { offset ->
                    annotatedPrivacy.getStringAnnotations("EMAIL", offset, offset)
                        .firstOrNull()?.let { annotation ->
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${annotation.item}")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(annotation.item))
                                putExtra(Intent.EXTRA_SUBJECT, "Support Inquiry - She and Soul")
                            }
                            if (emailIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(emailIntent)
                            }
                        }
                },
                style = TextStyle(fontSize = 16.sp, lineHeight = 24.sp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PrivacyScreenPreview() {
    PrivacyScreen()
}