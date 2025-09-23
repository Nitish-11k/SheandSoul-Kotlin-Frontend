package com.example.sheandsoul_nick.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AppBottomNavBar(
    onNavigateToArticles: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedItem by remember { mutableStateOf("Home") }
    val items = listOf("Home", "Articles", "Community", "Profile")

    NavigationBar(
        containerColor = Color(0xFFE0BBFF).copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        items.forEach { screen ->
            val isSelected = selectedItem == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        selectedItem = screen
                        when (screen) {
                            "Articles" -> onNavigateToArticles()
                            "Community" -> onNavigateToCommunity()
                            "Profile" -> onNavigateToProfile()
                        }
                    }
                },
                label = { Text(screen) },
                icon = { BottomNavIcon(screen = screen, isSelected = isSelected) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF9092FF),
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun BottomNavIcon(screen: String, isSelected: Boolean) {
    val icon: ImageVector = when (screen) {
        "Home" -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
        "Articles" -> if (isSelected) Icons.Filled.ListAlt else Icons.Outlined.ListAlt
        "Community" -> if (isSelected) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline
        "Profile" -> if (isSelected) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic
        else -> Icons.Filled.Home
    }
    Icon(imageVector = icon, contentDescription = screen)
}