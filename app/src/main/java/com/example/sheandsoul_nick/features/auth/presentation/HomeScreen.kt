package com.example.sheandsoul_nick.features.home

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.data.remote.ArticleCategoryDto
import com.example.sheandsoul_nick.data.remote.ArticleDto
import com.example.sheandsoul_nick.features.articles.ArticleViewModel
import com.example.sheandsoul_nick.features.articles.ArticleViewModelFactory
import com.example.sheandsoul_nick.features.articles.DataState
import com.example.sheandsoul_nick.features.auth.presentation.AssessmentUiState
import com.example.sheandsoul_nick.features.auth.presentation.AuthViewModel
import com.example.sheandsoul_nick.features.auth.presentation.MenstrualResult
import com.example.sheandsoul_nick.features.auth.presentation.PcosDashboardViewModel
import com.example.sheandsoul_nick.features.auth.presentation.PcosDashboardViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onNavigateToArticles: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPartner: () -> Unit,
    onNavigateToPcosQuiz: () -> Unit,
    onArticleClicked: (Long) -> Unit,
    onNavigateToPcosDashboard: () -> Unit,
    onNavigateToEditCycle: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val articleViewModel: ArticleViewModel = viewModel(factory = ArticleViewModelFactory(authViewModel))
    val articlesState by articleViewModel.categories.observeAsState()
    val menstrualResult by authViewModel.nextMenstrualResult.observeAsState()
    val pcosDashboardViewModel: PcosDashboardViewModel = viewModel(factory = PcosDashboardViewModelFactory(
        authViewModel
    )
    )
    val pcosState by pcosDashboardViewModel.assessmentState
    val context = LocalContext.current


    LaunchedEffect(key1 = authViewModel.token) {
        if(authViewModel.token != null) {
            articleViewModel.loadArticlesIfTokenAvailable()
            authViewModel.getNextMenstrualDetails()
        }
    }

    val username by remember { derivedStateOf { authViewModel.name.ifEmpty { "User" } } }
    Scaffold(
        topBar = {
            HomeTopAppBar(
                username = username,
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            AppBottomNavBar(
                onNavigateToArticles = onNavigateToArticles,
                onNavigateToCommunity = onNavigateToCommunity,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        containerColor = Color(0xFFF8F8FF)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                when (val result = menstrualResult) {
                    is MenstrualResult.Loading -> {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(280.dp)) {
                            CircularProgressIndicator()
                        }
                    }
                    is MenstrualResult.Success -> {
                        val today = LocalDate.now()
                        val nextPeriodDate = LocalDate.parse(result.data.nextPeriodStartDate)
                        val daysLeft = ChronoUnit.DAYS.between(today, nextPeriodDate).coerceAtLeast(0)
                        val totalCycleDays = authViewModel.cycle_length.takeIf { it > 0 } ?: 28
                        val progress = 1.0f - (daysLeft.toFloat() / totalCycleDays)

                        PeriodTrackerCard(
                            daysLeft = daysLeft.toInt(),
                            progress = progress.coerceIn(0f, 1f),
                            nextPeriodDateString = "${LocalDate.parse(result.data.nextPeriodStartDate).format(DateTimeFormatter.ofPattern("MMM dd"))} - ${LocalDate.parse(result.data.nextPeriodEndDate).format(DateTimeFormatter.ofPattern("dd"))}",
                            onEditCycleClick = onNavigateToEditCycle
                        )
                    }
                    is MenstrualResult.Error, null -> {
                        PeriodTrackerCard(
                            daysLeft = 0,
                            progress = 0f,
                            nextPeriodDateString = "No data",
                            onEditCycleClick = onNavigateToEditCycle
                        )
                    }
                }
            }

            item {
                when (val result = menstrualResult) {
                    is MenstrualResult.Success -> {
                        val today = LocalDate.now()
                        val fertileStart = LocalDate.parse(result.data.nextFertileWindowStartDate)
                        val daysToFertility = ChronoUnit.DAYS.between(today, fertileStart).coerceAtLeast(0)

                        FertilityCard(
                            daysLeft = daysToFertility.toInt(),
                            fertilityWindow = "${fertileStart.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${LocalDate.parse(result.data.nextFertileWindowEndDate).format(DateTimeFormatter.ofPattern("MMM dd"))}"
                        )
                    }
                    else -> {
                        FertilityCard(daysLeft = 0, fertilityWindow = "No data available")
                    }
                }
            }
            item { AddPartnerCard(onClick = onNavigateToPartner) }
            item {
                PcosAssessmentCard(
                    onStartAssessmentClick = onNavigateToPcosQuiz,
                    onViewDashboardClick = {
                        when (pcosState) {
                            is AssessmentUiState.Success -> {
                                // User has data, go to the dashboard
                                onNavigateToPcosDashboard()
                            }
                            is AssessmentUiState.NoAssessment, is AssessmentUiState.Error -> {
                                // No data or error, send them straight to the quiz
                                onNavigateToPcosQuiz()
                            }
                            is AssessmentUiState.Loading -> {
                                // Optional: Show a toast to let the user know it's working
                                Toast.makeText(context, "Checking assessment status...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
            item {
                CuratedForYouSection(
                    articlesState = articlesState,
                    onViewAllClicked = onNavigateToArticles,
                    onArticleClicked = onArticleClicked
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    username: String,
    onProfileClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Hi, $username",
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
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun PeriodTrackerCard(
    daysLeft: Int,
    progress: Float,
    nextPeriodDateString: String,
    onEditCycleClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val circleDiameter = screenWidth * 0.55f
    val dynamicFontSize = (circleDiameter.value / 3.5f).sp
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(circleDiameter)
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
                        painter = painterResource(id = R.drawable.ic_water_drop),
                        contentDescription = "Period",
                        tint = Color(0xFF9092FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Text("For Next Period", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = "$daysLeft",
                        fontSize = dynamicFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9092FF)
                    )
                    Text("Days Left", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(nextPeriodDateString, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onEditCycleClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0FF))
            ) {
                Text("Edit Cycle", color = Color(0xFF9092FF))
            }
        }
    }
}

@Composable
fun FertilityCard(
    daysLeft: Int,
    fertilityWindow: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
            .shadow(3.dp, shape = RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Fertility", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Next Fertile Window:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    fertilityWindow,
                    color = Color(0xFF9092FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$daysLeft Days Left for next Fertile Window",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            // ✅ FIXED: Replaced the Canvas with a simple Box for the circular gradient
            // Using a Box with a background gradient is simpler and more Compose-native than using a Canvas.
            Box(
                modifier = Modifier
                    .size(80.dp)
            ) {
                // Use a Canvas only if you need complex drawing.
                // For a simple icon, just use an Icon composable.
                Icon(
                    painter = painterResource(id = R.drawable.ic_fertility_icon), // Replace with your actual icon
                    contentDescription = "Fertility Indicator",
                    tint = Color(0xFF9092FF),
                    modifier = Modifier.size(80.dp)
                        .padding(2.dp)
                )
            }
        }
    }
}
@Composable
fun AddPartnerCard(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                    onClick = { onClick() },
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
fun PcosAssessmentCard(
    onStartAssessmentClick: () -> Unit,
    onViewDashboardClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)

    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "PCOS Health Center",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Understand your symptoms, get insights, and take charge of your health.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onStartAssessmentClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0FF)),
                    modifier = Modifier.weight(1f)
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                ) {
                    Text("Start/Re-take Assessment", color = Color(0xFF9092FF))
                }
                Button(
                    onClick = onViewDashboardClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0FF)),
                    modifier = Modifier.weight(1f)
                   .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                ) {
                    Text("View Dashboard", color = Color(0xFF9092FF))
                }
            }
        }
    }
}

@Composable
fun CuratedForYouSection(
    articlesState: DataState<List<ArticleCategoryDto>>?,
    onViewAllClicked: () -> Unit,
    onArticleClicked: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Curated For You",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onViewAllClicked) {
                Text(
                    text = "View All",
                    color = Color(0xFF9092FF),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (articlesState) {
            is DataState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is DataState.Success -> {
                val articles = articlesState.data.firstOrNull()?.articles?.take(3) ?: emptyList()
                if (articles.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(articles) { article ->
                            ArticleCard(
                                article = article,
                                onClick = { onArticleClicked(article.id) }
                            )
                        }
                    }
                } else {
                    Text("No articles available right now.", color = Color.Gray)
                }
            }
            is DataState.Error -> {
                Text(articlesState.message, color = MaterialTheme.colorScheme.error)
            }
            null -> {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun ArticleCard(
    article: ArticleDto,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .size(160.dp, 200.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.BottomStart) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_launcher_background),
                error = painterResource(id = R.drawable.ic_launcher_background)
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onProfileClick = {},
        onNavigateToArticles = {},
        onNavigateToCommunity = {},
        onNavigateToProfile = {},
        onNavigateToPartner = {},
        onNavigateToPcosQuiz = {},
        onNavigateToPcosDashboard = {},
        onArticleClicked = {},
        onNavigateToEditCycle = {},
    )
}

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
        "Articles" -> if (isSelected) Icons.AutoMirrored.Filled.ListAlt else Icons.AutoMirrored.Outlined.ListAlt
        "Community" -> if (isSelected) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline
        "Music" -> if (isSelected) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic
        else -> Icons.Filled.Home
    }
    Icon(imageVector = icon, contentDescription = screen)
}