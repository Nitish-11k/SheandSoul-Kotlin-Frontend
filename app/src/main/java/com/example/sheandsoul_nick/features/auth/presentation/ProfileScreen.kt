package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hello, ${authViewModel.name.ifEmpty { "User" }}!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = authViewModel.email,
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom

            HorizontalWaveButton(
                onClick = {
                    authViewModel.logout()
                    onNavigateToLogin()
                },
                text = "Logout",
                startColor = Color(0xFFD90429), // A reddish color for logout
                endColor = Color(0xFFEF233C),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }
    }
}

// ✅ ADD THIS PREVIEW FUNCTION
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    // We create a dummy AuthViewModel for the preview
    val previewAuthViewModel: AuthViewModel = viewModel()
    // Manually set some data for a realistic preview
    previewAuthViewModel.name = "Maria"
    previewAuthViewModel.email = "maria@example.com"

    ProfileScreen(
        authViewModel = previewAuthViewModel,
        onNavigateBack = {},
        onNavigateToLogin = {}
    )
}