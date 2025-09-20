package com.example.sheandsoul_nick.features.auth.presentation

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

@Composable
fun AgeSelectionScreen(
    onContinueClicked: (Int) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    // Change 1: Updated the age range
    val minAge = 10
    val maxAge = 78
    val totalAges = maxAge - minAge + 1

    val listState = rememberLazyListState()
    val defaultAge = 10 // A sensible default starting point
    var selectedAge by remember { mutableStateOf(defaultAge) }

    val itemWidthDp = 12.dp
    val itemWidthPx = with(LocalDensity.current) { itemWidthDp.toPx() }
    val circleSize = 200.dp
    val circleDiameterPx = with(LocalDensity.current) { circleSize.toPx() }
    val centerOffsetPx = (circleDiameterPx / 2) - (itemWidthPx / 2)

    // This effect listens to scroll changes and calculates the age
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex * itemWidthPx + listState.firstVisibleItemScrollOffset }
            .map { scrollOffset ->
                minAge + ((scrollOffset + centerOffsetPx) / itemWidthPx).toInt()
            }
            .distinctUntilChanged()
            .collect { age ->
                selectedAge = age.coerceIn(minAge, maxAge)
            }
    }

    // Change 2: Scroll to the default age when the screen first appears
    LaunchedEffect(Unit) {
        val initialIndex = (defaultAge - minAge).coerceAtLeast(0)
        listState.scrollToItem(initialIndex)
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
            modifier = Modifier
                .width(130.dp)
                .height(50.dp)
        )

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "What's your Age ?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

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

        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentPadding = PaddingValues(horizontal = (circleSize / 2) - (itemWidthDp / 2)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(totalAges) { index ->
                val age = minAge + index
                val height = when {
                    age % 10 == 0 -> 40.dp
                    age % 5 == 0 -> 30.dp
                    else -> 20.dp
                }
                RulerMark(height = height)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalWaveButton(
            onClick = {
                authViewModel.age =selectedAge
                onContinueClicked(selectedAge) },
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
private fun RulerMark(height: Dp) {
    Canvas(modifier = Modifier.size(width = 12.dp, height = height)) {
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFBBBDFF).copy(alpha = 0.8f),
                    Color(0xFFE0BBFF).copy(alpha = 0.2f)
                )
            ),
            start = Offset(center.x, 0f),
            end = Offset(center.x, size.height),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AgeSelectionScreenPreview() {
    AgeSelectionScreen(onContinueClicked = {})
}