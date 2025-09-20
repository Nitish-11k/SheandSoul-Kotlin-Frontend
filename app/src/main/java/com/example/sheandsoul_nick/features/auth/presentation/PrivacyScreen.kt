package com.example.sheandsoul_nick.features.auth.presentation


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton

@Composable
fun PrivacyScreen(
    onAgree: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Blurred Logo (replace with your logo in drawable)
        Image(
            painter = painterResource(id = R.drawable.ic_privacybg),
            contentDescription = "Blurred She & Soul Logo",
            modifier = Modifier
                .fillMaxWidth()
                // 1. Make the Image take up 40% of the screen's height
                .fillMaxHeight(fraction = 0.6f)
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            contentScale = ContentScale.Fit
        )

        // Gradient bottom section
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
                    text = "Your personal journey deserves protection.\n" +
                            "Please take a moment to read and understand our Terms of Service and Privacy Policy.\n\n" +
                            "We’re committed to keeping your data safe and using it only " +
                            "to improve your experience on She&Soul.",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PrivacyScreenPreview() {
    PrivacyScreen()
}
