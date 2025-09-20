package com.example.sheandsoul_nick.features.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
//import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.data.remote.ArticleCategoryDto
import com.example.sheandsoul_nick.data.remote.ArticleDto
import com.example.sheandsoul_nick.features.articles.ArticleViewModel
import com.example.sheandsoul_nick.features.articles.DataState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToMusic: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onArticleClicked: (Long) -> Unit,
    articleViewModel: ArticleViewModel = viewModel() // Get the ViewModel instance
) {
    val categoriesState by articleViewModel.categories.observeAsState()

    Scaffold(
        topBar = { ArticlesTopAppBar() },
        bottomBar = {
            AppBottomNavBar(
                selectedScreen = "Articles",
                onNavigateToHome = onNavigateToHome,
                onNavigateToArticles = {},
                onNavigateToCommunity = onNavigateToCommunity,
                onNavigateToMusic = onNavigateToMusic,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        containerColor = Color(0xFFF8F8FF)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = categoriesState) {
                is DataState.Loading -> {
                    CircularProgressIndicator()
                }
                is DataState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(state.data) { category ->
                            ArticleCategoryRow(
                                category = category,
                                onArticleClicked = onArticleClicked
                            )
                        }
                    }
                }
                is DataState.Error -> {
                    Text(text = state.message)
                }
                null -> {
                    // Initial state before the ViewModel is ready
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesTopAppBar() {
    TopAppBar(
        title = {
            Text(
                "Articles",
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
        actions = {
            IconButton(onClick = { /* TODO: Handle filter click */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_book),
                    contentDescription = "Filter",
                    tint = Color(0xFF9092FF),
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun ArticleCategoryRow(category: ArticleCategoryDto, onArticleClicked: (Long) -> Unit) {
    Column {
        Text(
            text = category.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(category.articles) { article ->
                ArticleCard(
                    article = article,
                    modifier = Modifier.clickable { onArticleClicked(article.id) }
                )
            }
        }
    }
}

@Composable
fun ArticleCard(article: ArticleDto, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.size(160.dp, 180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Use an image loading library like Coil for network images
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_article_workout) // Add a placeholder
            )
            Text(
                text = article.title,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AsyncImage(
    model: String?,
    contentDescription: String,
    modifier: Modifier,
    contentScale: ContentScale,
    placeholder: Painter
) {
    TODO("Not yet implemented")
}

@Composable
fun AppBottomNavBar(
    selectedScreen: String,
    onNavigateToHome: () -> Unit,
    onNavigateToArticles: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToMusic: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedItem by remember { mutableStateOf(selectedScreen) }
    val items = listOf("Home", "Articles", "Community", "Music", "Profile")

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
                            "Home" -> onNavigateToHome()
                            "Articles" -> onNavigateToArticles()
                            "Community" -> onNavigateToCommunity()
                            "Music" -> onNavigateToMusic()
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
        "Music" -> if (isSelected) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic
        else -> Icons.Filled.Home
    }
    Icon(imageVector = icon, contentDescription = screen)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ArticlesScreenPreview() {
    ArticlesScreen(
        onNavigateToHome = {},
        onNavigateToCommunity = {},
        onNavigateToMusic = {},
        onNavigateToProfile = {},
        onArticleClicked = {}
    )
}