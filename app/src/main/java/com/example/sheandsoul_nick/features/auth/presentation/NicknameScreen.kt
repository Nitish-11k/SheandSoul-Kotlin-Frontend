package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import com.example.sheandsoul_nick.ui.theme.SheAndSoulNickTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NicknameScreen(
    onContinueClicked: (nickname: String) -> Unit = {},
    onSkipClicked: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    var nickname by remember { mutableStateOf("") }

    // Using a Column as the root layout since Scaffold/TopAppBar is not needed
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp) // Add horizontal padding for the whole screen
            .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // This inner Column holds the main content and uses 'weight'
        // to push the buttons to the bottom of the screen.
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center the content vertically within its space
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_sheandsoul_text),
                contentDescription = "She & Soul Logo",
                modifier = Modifier
                    .width(130.dp)
                    .height(50.dp)
            )
            Spacer(modifier = Modifier.height(80.dp))
            Text(
                text = "What would you like to be called?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Text(
                text = "This will be your display name in the community.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Your Nickname") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF9092FF),
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Color(0xFF9092FF)
                )
            )
        }

        // This Row contains the buttons, and it's placed after the weighted Column
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp), // Padding from the bottom edge
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    // When skipping, we don't save any name
                    authViewModel.updateNickname("")
                    onSkipClicked()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF9092FF)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFBBBDFF), Color(0xFF9092FF))
                    )
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text(
                    text = "Skip",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            HorizontalWaveButton(
                onClick = {
                    authViewModel.updateNickname(nickname)
                    onContinueClicked(nickname)
                },
                text = "Continue >",
                startColor = Color(0xFFBBBDFF),
                endColor = Color(0xFF9092FF),
                modifier = Modifier
                    .weight(1f) // Makes the button take up equal space
                    .height(50.dp),
                cornerRadius = 12.dp,
                useVerticalGradient = true,
                enabled = nickname.isNotBlank() // Button is disabled if nickname is empty
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NicknameScreenPreview() {
    SheAndSoulNickTheme {
        NicknameScreen()
    }
}
