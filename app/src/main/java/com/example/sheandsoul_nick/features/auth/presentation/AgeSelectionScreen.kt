package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AgeSelectionScreen(
    onContinueClicked: (Int) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val minAge = 10
    val maxAge = 80
    val totalAges = maxAge - minAge + 1

    val defaultAge = 10
    var selectedAge by remember { mutableStateOf(defaultAge) }

    val listState = rememberLazyListState()
    val itemWidthDp = 12.dp
    val itemWidthPx = with(LocalDensity.current) { itemWidthDp.toPx() }
    val circleSize = 200.dp

    // Scroll to selected age and center it
    val centerOffsetPx = with(LocalDensity.current) { (circleSize / 2 - itemWidthDp / 2).toPx().roundToInt() }

    LaunchedEffect(Unit) {
        val initialIndex = defaultAge - minAge
        listState.scrollToItem(initialIndex, centerOffsetPx)
    }

    // Update selected age while scrolling
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex + listState.firstVisibleItemScrollOffset / itemWidthPx
        }.map { scrollIndex ->
            minAge + scrollIndex.toInt()
        }.distinctUntilChanged().collect { age ->
            selectedAge = age.coerceIn(minAge, maxAge)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_sheandsoul_text),
            contentDescription = "She & Soul Logo",
            modifier = Modifier.width(130.dp).height(50.dp)
        )

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "What's your Age ?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Centered circle displaying selected age
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFE0BBFF).copy(alpha = 0.3f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.verticalGradient(listOf(Color(0xFFBBBDFF), Color(0xFFE0BBFF))),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                )
            }
            Text(
                text = "$selectedAge",
                fontSize = 66.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9092FF)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
        val centerOffsetPx = (screenWidthPx / 2 - itemWidthPx / 2).roundToInt()
        val contentPaddingDp = with(LocalDensity.current) { (screenWidthPx / 2 - itemWidthPx / 2).toDp() }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            contentPadding = PaddingValues(horizontal = contentPaddingDp),
            verticalAlignment = Alignment.Bottom
        ) {
            items(totalAges) { index ->
                val age = minAge + index
                RulerMark(age = age)
            }
        }

        LaunchedEffect(Unit) {
            listState.scrollToItem(0, centerOffsetPx) // 10 is now centered initially
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalWaveButton(
            onClick = {
                authViewModel.age = selectedAge
                Log.d("She&Soul", "Age selected: $selectedAge ${authViewModel.age}")
                onContinueClicked(selectedAge)
            },
            text = "Continue >",
            startColor = Color(0xFFBBBDFF),
            endColor = Color(0xFF9092FF),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(50.dp)
        )
    }
}

@Composable
private fun RulerMark(age: Int) {
    val isMajor = age % 10 == 0
    val isMedium = age % 5 == 0 && !isMajor
    val height = when {
        isMajor -> 50.dp
        isMedium -> 35.dp
        else -> 20.dp
    }
    val color = when {
        isMajor -> Color(0xFF9092FF)
        isMedium -> Color(0xFFBBBDFF)
        else -> Color(0xFFE0BBFF).copy(alpha = 0.6f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.height(70.dp)
    ) {
        Canvas(modifier = Modifier.size(width = 12.dp, height = height)) {
            drawLine(
                color = color,
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = if (isMajor) 3.dp.toPx() else 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        if (isMajor) {
            Text(
                text = "$age",
                fontSize = 12.sp,
                color = Color(0xFF9092FF),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AgeSelectionScreenPreview() {
    AgeSelectionScreen(onContinueClicked = {})
}