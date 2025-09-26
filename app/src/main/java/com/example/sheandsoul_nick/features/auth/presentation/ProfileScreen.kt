// file: app/src/main/java/com/example/sheandsoul_nick/features/auth/presentation/ProfileScreen.kt

package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import com.example.sheandsoul_nick.ui.theme.Purple40

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var periodRemindersEnabled by remember { mutableStateOf(true) }
    var fertileWindowAlertsEnabled by remember { mutableStateOf(false) }

    // State to control the visibility of the bottom sheet
    var showPolicySheet by remember { mutableStateOf(false) }

    // When showPolicySheet becomes true, the sheet is displayed
    if (showPolicySheet) {
        PrivacyAndTermsSheet(onDismiss = { showPolicySheet = false })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_sheandsoul_text),
                        contentDescription = "She & Soul Logo",
                        modifier = Modifier.width(130.dp)
                            .height(50.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Card for Personal Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoRow(label = "Name", value = authViewModel.name.ifEmpty { "N/A" })
                        InfoRow(label = "Age", value = if (authViewModel.age > 0) "${authViewModel.age} years" else "N/A")
                        InfoRow(label = "Height", value = if (authViewModel.height > 0) "${authViewModel.height} cm" else "N/A")
                        InfoRow(label = "Weight", value = if (authViewModel.weight > 0) "${authViewModel.weight} kg" else "N/A")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Card for Health Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Menstrual Health", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoRow(label = "Usual Period Length", value = if (authViewModel.period_length > 0) "${authViewModel.period_length} days" else "N/A")
                        InfoRow(label = "Usual Cycle Length", value = if (authViewModel.cycle_length > 0) "${authViewModel.cycle_length} days" else "N/A")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Card for Notification Settings
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Notification Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingRow(
                            label = "Push Notifications",
                            isChecked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                        if (notificationsEnabled) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            SettingRow(
                                label = "Period Reminders",
                                isChecked = periodRemindersEnabled,
                                onCheckedChange = { periodRemindersEnabled = it }
                            )
                            SettingRow(
                                label = "Fertile Window Alerts",
                                isChecked = fertileWindowAlertsEnabled,
                                onCheckedChange = { fertileWindowAlertsEnabled = it }
                            )
                        }
                    }
                }

                // New Card for "About & Legal"
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("About & Legal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPolicySheet = true } // This opens the sheet
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Privacy Policy & Terms of Use", color = Color.Gray, fontSize = 16.sp)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View details",
                                tint = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // Padding for the logout button
            }

            HorizontalWaveButton(
                onClick = {
                    authViewModel.logout()
                    onNavigateToLogin()
                },
                text = "Logout",
                startColor = Color(0xFFD90429),
                endColor = Color(0xFFEF233C),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(50.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}


@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 16.sp)
        Text(text = value, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun SettingRow(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 16.sp)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Purple40,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    val previewAuthViewModel: AuthViewModel = viewModel()
    previewAuthViewModel.name = "Maria"
    previewAuthViewModel.email = "maria@example.com"
    previewAuthViewModel.age = 28
    previewAuthViewModel.height = 165f
    previewAuthViewModel.weight = 60f
    previewAuthViewModel.period_length = 5
    previewAuthViewModel.cycle_length = 28

    ProfileScreen(
        authViewModel = previewAuthViewModel,
        onNavigateBack = {},
        onNavigateToLogin = {}
    )
}