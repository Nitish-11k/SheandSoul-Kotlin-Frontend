package com.example.sheandsoul_nick.features.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.sheandsoul_nick.features.auth.presentation.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    onNavigateToNote: () -> Unit,
    onNavigateToChatBot: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToShop: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val articleViewModel: ArticleViewModel = viewModel(factory = ArticleViewModelFactory(authViewModel))
    val articlesState by articleViewModel.categories.observeAsState()
    val menstrualResult by authViewModel.nextMenstrualResult.observeAsState()
    val pcosDashboardViewModel: PcosDashboardViewModel = viewModel(factory = PcosDashboardViewModelFactory(authViewModel))
    val pcosState by pcosDashboardViewModel.assessmentState

    LaunchedEffect(key1 = authViewModel.token) {
        if (authViewModel.token != null) {
            articleViewModel.loadArticlesIfTokenAvailable()
            authViewModel.getNextMenstrualDetails()
        }
    }

    val displayName by remember {
        derivedStateOf {
            authViewModel.nickname.ifBlank { authViewModel.name }.ifEmpty { "User" }
        }
    }
    Scaffold(
        topBar = {
            HomeTopAppBar(
                username = displayName,
                onProfileClick = onProfileClick,
                onNavigateToNote = onNavigateToNote
            )
        },
        bottomBar = {
            AppBottomNavBar(
                onNavigateToArticles = onNavigateToArticles,
                onNavigateToCommunity = onNavigateToCommunity,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        floatingActionButton = {
            val infiniteTransition = rememberInfiniteTransition(label = "border_rotation")
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "angle"
            )

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val pressedScale by animateFloatAsState(
                targetValue = if (isPressed) 0.9f else 1f,
                animationSpec = tween(durationMillis = 100),
                label = "press_scale"
            )

            val textAlpha = remember { Animatable(0f) }
            val textScale = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                delay(1000)
                coroutineScope {
                    launch { textAlpha.animateTo(1f, tween(300)) }
                    launch { textScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                }
                delay(1500)
                coroutineScope {
                    launch { textAlpha.animateTo(0f, tween(500)) }
                    launch { textScale.animateTo(1.2f, tween(500)) }
                }
            }

            Box(contentAlignment = Alignment.Center) {
                FloatingActionButton(
                    onClick = { onNavigateToChatBot() },
                    shape = CircleShape,
                    containerColor = Color.White,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = pressedScale
                            scaleY = pressedScale
                        }
                        .drawWithContent {
                            drawContent()
                            rotate(angle) {
                                drawCircle(
                                    brush = Brush.sweepGradient(
                                        0.0f to Color.Transparent,
                                        0.2f to Color.Transparent,
                                        0.8f to Color(0xFFBBBDFF),
                                        1.0f to Color.Magenta
                                    ),
                                    style = Stroke(width = 3.dp.toPx()),
                                )
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_bot_logo),
                        contentDescription = "Open AI Assistant",
                        modifier = Modifier.size(65.dp)
                    )
                }
                Text(
                    text = "Hey!",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-40).dp)
                        .graphicsLayer {
                            scaleX = textScale.value
                            scaleY = textScale.value
                            alpha = textAlpha.value
                        }
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
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
                        Box(contentAlignment = Alignment.Center, modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)) {
                            CircularProgressIndicator()
                        }
                    }
                    is MenstrualResult.Success -> {
                        val today = LocalDate.now()
                        val data = result.data
                        val totalCycleDays = (authViewModel.cycle_length.takeIf { it > 0 } ?: 28).toLong()

                        // Parse NEXT cycle dates from API
                        val nextPeriodStart = LocalDate.parse(data.nextPeriodStartDate)
                        val nextPeriodEnd = LocalDate.parse(data.nextPeriodEndDate)
                        val nextFollicularStart = LocalDate.parse(data.nextFollicularStartDate)
                        val nextFollicularEnd = LocalDate.parse(data.nextFollicularEndDate)
                        val nextOvulationStart = LocalDate.parse(data.nextOvulationDate)
                        val nextOvulationEnd = LocalDate.parse(data.nextOvulationEndDate)
                        val nextLutealStart = LocalDate.parse(data.nextLutealStartDate)
                        val nextLutealEnd = LocalDate.parse(data.nextLutealEndDate)

                        // ✅ **START FIX**: Estimate CURRENT phase dates by subtracting one cycle length
                        val currentPeriodStart = nextPeriodStart.minusDays(totalCycleDays)
                        val currentPeriodEnd = nextPeriodEnd.minusDays(totalCycleDays)
                        val currentFollicularStart = nextFollicularStart.minusDays(totalCycleDays)
                        val currentFollicularEnd = nextFollicularEnd.minusDays(totalCycleDays)
                        val currentOvulationStart = nextOvulationStart.minusDays(totalCycleDays)
                        val currentOvulationEnd = nextOvulationEnd.minusDays(totalCycleDays)
                        val currentLutealStart = nextLutealStart.minusDays(totalCycleDays)
                        val currentLutealEnd = nextLutealEnd.minusDays(totalCycleDays)

                        // ✅ **FIX**: Determine current phase using the corrected dates
                        val currentPhaseName = when {
                            !today.isBefore(currentPeriodStart) && !today.isAfter(currentPeriodEnd) -> "Menstrual"
                            !today.isBefore(currentOvulationStart) && !today.isAfter(currentOvulationEnd) -> "Ovulation"
                            !today.isBefore(currentLutealStart) && !today.isAfter(currentLutealEnd) -> "Luteal"
                            !today.isBefore(currentFollicularStart) && !today.isAfter(currentFollicularEnd) -> "Follicular"
                            else -> "Your Cycle"
                        }
                        // ✅ **END FIX**

                        // Calculate durations for the visual representation
                        val periodDuration = ChronoUnit.DAYS.between(nextPeriodStart, nextPeriodEnd).toInt() + 1
                        val follicularDuration = ChronoUnit.DAYS.between(nextPeriodEnd.plusDays(1), nextOvulationStart.minusDays(1)).toInt() + 1
                        val ovulationDuration = ChronoUnit.DAYS.between(nextOvulationStart, nextOvulationEnd).toInt() + 1
                        val lutealDuration = totalCycleDays.toInt() - periodDuration - follicularDuration - ovulationDuration

                        // Calculate progress
                        val daysLeft = ChronoUnit.DAYS.between(today, nextPeriodStart).coerceAtLeast(0)
                        val progress = 1.0f - (daysLeft.toFloat() / totalCycleDays)

                        PeriodTrackerCard(
                            daysLeft = daysLeft.toInt(),
                            progress = progress.coerceIn(0f, 1f),
                            nextPeriodDateString = "${nextPeriodStart.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${nextPeriodEnd.format(DateTimeFormatter.ofPattern("dd"))}",
                            onEditCycleClick = onNavigateToEditCycle,
                            onViewLogsClick = onNavigateToLogs,
                            currentPhaseName = currentPhaseName,
                            periodDuration = periodDuration,
                            follicularDuration = follicularDuration,
                            ovulationDuration = ovulationDuration,
                            lutealDuration = lutealDuration,
                            totalCycleDays = totalCycleDays.toInt()
                        )
                    }
                    is MenstrualResult.Error, null -> {
                        PeriodTrackerCard(
                            daysLeft = 0,
                            progress = 0f,
                            nextPeriodDateString = "No data",
                            onEditCycleClick = onNavigateToEditCycle,
                            onViewLogsClick = onNavigateToLogs,
                            currentPhaseName = "Your Cycle",
                            periodDuration = 5,
                            follicularDuration = 8,
                            ovulationDuration = 4,
                            lutealDuration = 11,
                            totalCycleDays = 28
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
                    onViewDashboardClick = { onNavigateToPcosDashboard() }
                )
            }
            item {
                CuratedForYouSection(
                    articlesState = articlesState,
                    onViewAllClicked = onNavigateToArticles,
                    onArticleClicked = onArticleClicked
                )
            }
            item {
                ShopCard(onClick = onNavigateToShop)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    username: String,
    onProfileClick: () -> Unit,
    onNavigateToNote: () -> Unit
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
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBBBDFF))
                    .border(1.5.dp, Color(0xFFC39BE0), CircleShape)
                    .clickable { onProfileClick() }
            )
        },
        actions = {
            IconButton(onClick = onNavigateToNote) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = "Open Notes",
                    tint = Color(0xFF9092FF),
                    modifier = Modifier.size(38.dp)
                )
            }
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
    onEditCycleClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    currentPhaseName: String,
    periodDuration: Int,
    follicularDuration: Int,
    ovulationDuration: Int,
    lutealDuration: Int,
    totalCycleDays: Int
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
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onViewLogsClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "View Past Logs", tint = Color(color=0xFF9092FF))
            }

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
                        val menstrualColor = Color(0xFFE57373) // Light Red
                        val follicularColor = Color(0xFF81D4FA) // Light Blue
                        val ovulationColor = Color(0xFFBA68C8)   // Light Purple
                        val lutealColor = Color(0xFFFFD54F)      // Light Yellow

                        val anglePerDay = if (totalCycleDays > 0) 360f / totalCycleDays.toFloat() else 0f

                        val periodSweep = periodDuration * anglePerDay
                        val follicularSweep = follicularDuration * anglePerDay
                        val ovulationSweep = ovulationDuration * anglePerDay
                        val lutealSweep = (totalCycleDays - periodDuration - follicularDuration - ovulationDuration) * anglePerDay

                        var startAngle = -90f

                        drawArc(
                            color = menstrualColor,
                            startAngle = startAngle,
                            sweepAngle = periodSweep,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Butt)
                        )
                        startAngle += periodSweep

                        drawArc(
                            color = follicularColor,
                            startAngle = startAngle,
                            sweepAngle = follicularSweep,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Butt)
                        )
                        startAngle += follicularSweep

                        drawArc(
                            color = ovulationColor,
                            startAngle = startAngle,
                            sweepAngle = ovulationSweep,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Butt)
                        )
                        startAngle += ovulationSweep

                        drawArc(
                            color = lutealColor,
                            startAngle = startAngle,
                            sweepAngle = lutealSweep,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Butt)
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
                        Text(
                            text = "Phase: $currentPhaseName",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )
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
                    Text("Update Cycle", color = Color(0xFF9092FF))
                }
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
        modifier = Modifier
            .fillMaxWidth()
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
            Box(
                modifier = Modifier
                    .size(80.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_fertility_icon), // Replace with your actual icon
                    contentDescription = "Fertility Indicator",
                    tint = Color(0xFF9092FF),
                    modifier = Modifier
                        .size(80.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onStartAssessmentClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0FF)),
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                ) {
                    Text("Start/Re-take Assessment", color = Color(0xFF9092FF))
                }
                Button(
                    onClick = onViewDashboardClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0FF)),
                    modifier = Modifier
                        .weight(1f)
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
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), contentAlignment = Alignment.Center) {
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
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
@Composable
fun ShopCard(onClick: () -> Unit) {
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
                        colors = listOf(Color(0xFFC2A3FF), Color(0xFF9092FF))
                    )
                )
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp)
            ) {
                Text("Visit Our Shop", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Find products curated for your wellness journey.", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
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
                painter = painterResource(id = R.drawable.ic_shop_bgc), // Using a generic logo
                contentDescription = "Shop Icon",
                modifier = Modifier
                    .fillMaxHeight()
                    .width(130.dp)
                    .height(80.dp),
//                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
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