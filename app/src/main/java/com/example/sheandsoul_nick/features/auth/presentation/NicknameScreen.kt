package com.example.sheandsoul_nick.features.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton // Assuming you have this custom button

@Composable
fun NicknameScreen(
    onContinueClicked: (nickname: String) -> Unit = {},
    onSkipClicked: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel ()
) {
    var nickname by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Centered Logo
            Image(
                painter = painterResource(id = R.drawable.ic_sheandsoul_text), // Assuming this is your text logo
                contentDescription = "She & Soul Logo",
                modifier = Modifier
                    .width(130.dp)
                    .height(50.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Question Text
            Text(
                text = "What would you like to be called?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black // Assuming black text
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nickname Input Field
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nick Name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 20.sp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF9092FF), // Purple from your theme
                    unfocusedIndicatorColor = Color.LightGray, // Default border color
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Color(0xFF9092FF) // Cursor color
                )
            )
        }

        // Buttons at the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter), // Aligns the Row to the bottom
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Spacing between buttons
        ) {
            // Continue Button (uses your custom HorizontalWaveButton)
            HorizontalWaveButton(
                onClick = {
                    authViewModel.nickname = nickname
                    onContinueClicked(nickname) },
                text = "Continue >",
                startColor = Color(0xFFBBBDFF), // Light purple
                endColor = Color(0xFF9092FF),   // Darker purple
                modifier = Modifier
                    .weight(1f) // Makes button take half the width
                    .height(50.dp),
                cornerRadius = 12.dp,
                useVerticalGradient = true,
//                textColor = Color.White // Text color for the gradient button
            )

            // Skip Button (simple OutlinedButton-like style)
            androidx.compose.material3.OutlinedButton( // Using standard Material3 OutlinedButton
                onClick = { onSkipClicked() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF9092FF) // Text color for skip button
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFBBBDFF), Color(0xFF9092FF))
                    )
                ),
                modifier = Modifier
                    .weight(1f) // Makes button take half the width
                    .height(50.dp)
            ) {
                Text(
                    text = "Skip >",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NicknameScreenPreview() {
    NicknameScreen()
}