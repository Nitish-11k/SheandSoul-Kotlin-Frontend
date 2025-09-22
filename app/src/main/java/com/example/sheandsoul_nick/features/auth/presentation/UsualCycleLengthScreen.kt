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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UsualCycleLengthScreen(
    onContinueClicked:()->Unit,
    authViewModel: AuthViewModel
) {
    val daysList = (1..28).toList() + listOf(null) // fake items on top & bottom
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 27 / 2) // default = middle of 1-28

    val selectedDay by remember {
        derivedStateOf {
            if (listState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
                val centerIndex = listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size / 2
                val value = daysList.getOrNull(centerIndex)
                value ?: 28 / 2
            } else 28 / 2
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
            text = "How long does your cycle usually last?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Number picker for 1-28 days
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
            ) { item, isSelected ->
                if (item != null) {
                    Text(
                        text = "$item days",
                        fontSize = if (isSelected) 32.sp else 24.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF9092FF) else Color.Gray,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalWaveButton(
            onClick = {
                authViewModel.cycleLength = selectedDay-3
                Log.d("She&Soul", "Selected Cycle Duration: $selectedDay days  ${authViewModel.cycleLength}")
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
