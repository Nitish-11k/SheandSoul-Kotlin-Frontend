package com.example.sheandsoul_nick.features.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheandsoul_nick.R

@Composable
fun CommunityComingSoonScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToArticles: () -> Unit,
    onNavigateToMusic: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        // You can reuse your ArticlesTopAppBar or create a new one
        topBar = { CommunityTopAppBar() },
        bottomBar = {
            AppBottomNavBar(
                selectedScreen = "Community",
                onNavigateToHome = onNavigateToHome,
                onNavigateToArticles = onNavigateToArticles,
                onNavigateToCommunity = { }, // Already on this screen
                onNavigateToMusic = onNavigateToMusic,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        containerColor = Color(0xFFF8F8FF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_sheandsoul_text),
                contentDescription = "She & Soul Logo",
                modifier = Modifier
                    .width(130.dp)
                    .height(50.dp)
                    .alpha(0.4f),

            )

            Spacer(modifier = Modifier.height(28.dp))

            // An illustration helps make the screen more engaging.
            // Using the partner avatar as it relates to community.
//            Image(
//                painter = painterResource(id = R.drawable.ic_partner_avtar),
//                contentDescription = "Coming Soon Illustration",
//                modifier = Modifier
//                    .fillMaxWidth(0.8f)
//                    .alpha(0.6f)
//            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Community is Coming Soon!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF9092FF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We're building a safe and supportive space for you to connect. Stay tuned!",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityTopAppBar() {
    TopAppBar(
        title = {
            Text(
                "Community",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9092FF)
            )
        },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.ic_user_avtar),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
            )
        },

        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CommunityComingSoonScreenPreview() {
    CommunityComingSoonScreen(
        onNavigateToHome = {},
        onNavigateToArticles = {},
        onNavigateToMusic = {},
        onNavigateToProfile = {}
    )
}
