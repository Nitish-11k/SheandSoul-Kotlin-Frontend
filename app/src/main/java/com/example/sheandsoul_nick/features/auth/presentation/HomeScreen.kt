package com.example.sheandsoul_nick.features.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.AppBottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String,                  // Add this
    onProfileClick: () -> Unit,        // Add this
    onNotificationClick: () -> Unit,   // Add this
    onNavigateToArticles: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPartner: () -> Unit
) {
    Scaffold(
        topBar = { HomeTopAppBar(
            username = username,
            onProfileClick = onProfileClick,
            onNotificationClick = onNotificationClick
        ) },
        bottomBar = {
            AppBottomNavBar(
                onNavigateToArticles = onNavigateToArticles,
                onNavigateToCommunity = onNavigateToCommunity,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        containerColor = Color(0xFFF8F8FF) // Light lavender background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { PeriodTrackerCard(daysLeft = 25, progress = 0.8f) }
            item { FertilityCard(daysLeft = 11) }
            item { AddPartnerCard(onClick = onNavigateToPartner) }
            item { PcosAssessmentCard() }
            item { CuratedForYouSection() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    username: String,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    // The only change is here!
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "$username",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
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
                    .clickable { onProfileClick() }
            )
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF9092FF)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun PeriodTrackerCard(daysLeft: Int, progress: Float) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(), // This ensures the Column takes full width for proper centering
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFFE0E0FF),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF9092FF),
                        startAngle = -90f,
                        sweepAngle = 360 * progress,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_water_drop), // Add this icon
                        contentDescription = "Period",
                        tint = Color(0xFF9092FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Text("For Next Period", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = "$daysLeft",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9092FF)
                    )
                    Text("Days Left", fontSize = 14.sp, color = Color.Gray)
                }
            }
            Button(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0FF))
            ) {
                Text("Edit Cycle", color = Color(0xFF9092FF))
            }
        }
    }
}

@Composable
fun FertilityCard(daysLeft: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Fertility", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Low", color = Color(0xFF9092FF), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("$daysLeft Days Left for next Ovulation Cycle", fontSize = 12.sp, color = Color.Gray)
            }
            // This is a placeholder for the wave graphic
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0FF))
            )
        }
    }
}

@Composable
fun AddPartnerCard(
    onClick: () -> Unit // 1. Add the onClick callback as a parameter
) {
    Card(

        shape = RoundedCornerShape(16.dp),

        modifier = Modifier.fillMaxWidth(),

        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)

    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF9092FF), Color(0xFFC2A3FF))
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp)
            ) {
                Text("Add Your Partner", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Share Your Journey, Let Him Walk Beside You", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                Button(
                    onClick = { onClick() }, // You can also make the button trigger the same action
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.3f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Coming Soon")
                }
            }
            Image(
                painter = painterResource(id = R.drawable.ic_partner_avtar),
                contentDescription = "Partner Avatar",
                modifier = Modifier
                    .fillMaxHeight()
                    .width(130.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun PcosAssessmentCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("PCOS Self-Assessment", fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(
                "Understand Your Symptoms. Take Charge of Your Health.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0FF))
            ) {
                Text("Start Assessment", color = Color(0xFF9092FF))
            }
        }
    }
}

data class Article(val title: String, @DrawableRes val imageRes: Int)

@Composable
fun CuratedForYouSection() {
    val articles = listOf(
        Article("When To Use Menstrual Cups", R.drawable.ic_article_cup),
        Article("How To Workout During Your Period", R.drawable.ic_article_workout)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Curated For You",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(articles) { article ->
                ArticleCard(article = article)
            }
        }
    }
}

@Composable
fun ArticleCard(article: Article) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(160.dp, 200.dp)
    ) {
        Box(contentAlignment = Alignment.BottomStart) {
            Image(
                painter = painterResource(id = article.imageRes),
                contentDescription = article.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
            )
            Text(
                text = article.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(16.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(username = "Angel",onProfileClick = {}, onNavigateToArticles = {}, onNavigateToCommunity = {}, onNavigateToProfile = {}, onNotificationClick = {}, onNavigateToPartner = {})
}

// NOTE: It is a best practice to move AppBottomNavBar and BottomNavIcon to a shared
// components file (e.g., ui/components/BottomBar.kt) so it can be reused.
// For completeness, it is included here.
@Composable
fun AppBottomNavBar(
    onNavigateToArticles: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedItem by remember { mutableStateOf("Home") }
    val items = listOf("Home", "Articles", "Community", "Music")

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
                            "Music" -> onNavigateToProfile()
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