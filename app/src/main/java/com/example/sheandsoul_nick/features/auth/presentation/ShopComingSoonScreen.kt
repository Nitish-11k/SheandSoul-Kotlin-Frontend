package com.example.sheandsoul_nick.features.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton

@Composable
fun ShopComingSoonScreen(
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(26.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_sheandsoul_text),
            contentDescription = "She & Soul Logo",
            modifier = Modifier
                .width(130.dp)
                .height(50.dp)
                .alpha(0.6f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_market), // A generic logo for placeholder
            contentDescription = "Coming Soon Illustration",
            modifier = Modifier
                .size(260.dp)
                .alpha(0.4f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Our Shop is Coming Soon!",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF9092FF)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "We are curating the best products for your well-being. Stay tuned!",
            fontSize = 14.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalWaveButton(
            onClick = { onGoBack() },
            text = "< Go Back",
            startColor = Color(0xFFBBBDFF),
            endColor = Color(0xFF9092FF),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ShopComingSoonScreenPreview() {
    ShopComingSoonScreen(onGoBack = {})
}