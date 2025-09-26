package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UsualCycleLengthScreen(
    onContinueClicked: () -> Unit,
    authViewModel: AuthViewModel
) {
    // --- CONFIGURATION & DATA ---
    val itemHeight: Dp = 60.dp
    val pickerHeight: Dp = 300.dp
    val verticalPadding = (pickerHeight - itemHeight) / 2 // ✅ Key for full scrolling range

    // ✅ Changed the range from 10..30 to 1..35
    val daysList = (1..35).toList()
    val defaultCycleLength = 28
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = daysList.indexOf(defaultCycleLength).coerceAtLeast(0)
    )

    // Your derivedStateOf logic is already excellent and robust, no changes needed.
    val selectedDay by remember {
        derivedStateOf {
            val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                defaultCycleLength
            } else {
                val viewportCenter = (listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportEndOffset) / 2
                val centerItem = visibleItemsInfo.minByOrNull { abs((it.offset + it.size / 2) - viewportCenter) }
                centerItem?.index?.let { daysList.getOrNull(it) } ?: defaultCycleLength
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .navigationBarsPadding(),
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
            text = "How long does your cycle usually last?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .height(pickerHeight)
                .width(150.dp),
            contentAlignment = Alignment.Center
        ) {
            // Central highlight box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFBBBDFF).copy(alpha = 0.1f))
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(listOf(Color(0xFFBBBDFF), Color(0xFF9092FF))),
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            // ✅ Replaced VerticalNumberPicker with a standard LazyColumn to apply the fix directly
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = verticalPadding),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
            ) {
                items(daysList.size) { index ->
                    val item = daysList[index]
                    val isSelected = (item == selectedDay)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$item days",
                            fontSize = if (isSelected) 32.sp else 24.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF9092FF) else Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalWaveButton(
            onClick = {
                authViewModel.cycle_length = selectedDay
                Log.d("She&Soul", "Selected Cycle Duration: $selectedDay days")
                onContinueClicked()
            },
            text = "Continue >",
            startColor = Color(0xFFBBBDFF),
            endColor = Color(0xFF9092FF),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}