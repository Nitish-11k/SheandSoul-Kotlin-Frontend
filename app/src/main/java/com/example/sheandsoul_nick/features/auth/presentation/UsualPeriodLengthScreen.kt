package com.example.sheandsoul_nick.features.auth.presentation


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import com.example.sheandsoul_nick.ui.components.VerticalNumberPicker
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UsualPeriodLengthScreen(
    onContinueClicked:()->Unit,
    authViewModel: AuthViewModel
) {
    val daysList = (1..10).toList()
    val defaultPeriodLength = 5
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = daysList.indexOf(defaultPeriodLength).coerceAtLeast(0))

    // This derived state is now the single source of truth for the selected day.
    val selectedDay by remember {
        derivedStateOf {
            val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                defaultPeriodLength
            } else {
                // This calculation accurately finds the item closest to the vertical center of the viewport.
                val viewportCenter = (listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportEndOffset) / 2
                val centerItem = visibleItemsInfo.minByOrNull { abs((it.offset + it.size / 2) - viewportCenter) }
                centerItem?.index?.let { daysList.getOrNull(it) } ?: defaultPeriodLength
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
            text = "How long does your period usually last?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .height(300.dp)
                .width(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFBBBDFF).copy(alpha = 0.1f))
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(listOf(Color(0xFFBBBDFF), Color(0xFF9092FF))),
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            VerticalNumberPicker(
                listState = listState,
                items = daysList,
                itemHeight = 60.dp,
                selectorHeight = 60.dp
            ) { item, _ -> // We ignore the 'isSelected' from the component itself.
                // ✅ FIX: The selection logic is now driven by our own reliable 'selectedDay' state.
                val isSelected = (item == selectedDay)
                Text(
                    text = "$item days",
                    fontSize = if (isSelected) 32.sp else 24.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF9092FF) else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalWaveButton(
            onClick = {
                // This now reliably uses the correctly calculated centered day.
                authViewModel.period_length = selectedDay
                Log.d("She&Soul", "Selected Period Duration: $selectedDay days")
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

