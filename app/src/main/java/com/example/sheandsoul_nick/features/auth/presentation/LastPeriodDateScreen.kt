package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import java.time.LocalDate
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LastPeriodDateScreen(
    authViewModel: AuthViewModel,
    onFinish: () -> Unit
) {
    val currentMonth = remember { mutableStateOf(YearMonth.now()) }
    val selectedDates = remember { mutableStateListOf<LocalDate>() }
    val profileCreationResult by authViewModel.profileCreationResult.observeAsState()
    val menstrualDataResult by authViewModel.menstrualDataResult.observeAsState()

    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(profileCreationResult) {
        when (val result = profileCreationResult) {
            is AuthResult.Success -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                authViewModel.createMenstrualData()
            }
            is AuthResult.Error -> {
                isLoading = false
                Toast.makeText(context, "Error: ${result.errorMessage}", Toast.LENGTH_LONG).show()
            }
            is AuthResult.Loading -> isLoading = true
            null -> {}
            is AuthResult.SuccessGoogle -> TODO()
        }
    }

    LaunchedEffect(menstrualDataResult) {
        when (val result = menstrualDataResult) {
            is AuthResult.Success -> {
                isLoading = false
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                onFinish()
            }
            is AuthResult.Error -> {
                isLoading = false
                Toast.makeText(context, "Error: ${result.errorMessage}", Toast.LENGTH_LONG).show()
            }
            is AuthResult.Loading -> isLoading = true
            null -> {}
            is AuthResult.SuccessGoogle -> TODO()
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Logo
        Image(
            painter = painterResource(id = R.drawable.ic_sheandsoul_text),
            contentDescription = "She & Soul Logo",
            modifier = Modifier
                .width(130.dp)
                .height(50.dp)
        )

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "When did your last period start?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Calendar box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFBBBDFF).copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            val firstDayOfMonth = currentMonth.value.atDay(1)
            val totalDays = currentMonth.value.lengthOfMonth()
            val firstDayIndex = firstDayOfMonth.dayOfWeek.value % 7
            val totalCells = firstDayIndex + totalDays
            val weeks = (totalCells / 7) + 1
            var dayCounter = 1

            Column(modifier = Modifier.align(Alignment.Center)) {
                // Weekday labels
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat").forEach {
                        Text(it, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                for (week in 0 until weeks) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        for (dayOfWeek in 0..6) {
                            val cellIndex = week * 7 + dayOfWeek
                            if (cellIndex < firstDayIndex || dayCounter > totalDays) {
                                Box(modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)) {}
                            } else {
                                val date = currentMonth.value.atDay(dayCounter)
                                val isSelected = selectedDates.contains(date)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF9092FF) else Color.Transparent)
                                        .clickable {
                                            if (selectedDates.isEmpty()) {
                                                selectedDates.add(date)
                                            } else {
                                                val first = selectedDates.first()
                                                val last = selectedDates.last()
                                                if (date.isBefore(first)) {
                                                    val diff =
                                                        first.toEpochDay() - date.toEpochDay()
                                                    if (diff <= 6) {
                                                        selectedDates.clear()
                                                        for (d in 0..diff.toInt()) selectedDates.add(
                                                            date.plusDays(d.toLong())
                                                        )
                                                    }
                                                } else if (date.isAfter(last)) {
                                                    val diff = date.toEpochDay() - last.toEpochDay()
                                                    if (diff <= 6 && selectedDates.size + diff.toInt() <= 7) {
                                                        for (d in 1..diff.toInt()) selectedDates.add(
                                                            last.plusDays(d.toLong())
                                                        )
                                                    }
                                                } else {
                                                    selectedDates.clear()
                                                    selectedDates.add(date)
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayCounter",
                                        color = if (isSelected) Color.White else Color.Black,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                dayCounter++
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Finish Button at bottom
        if (!isLoading) {
            HorizontalWaveButton(
                onClick = {
                    if (selectedDates.isNotEmpty()) {
                        authViewModel.lastPeriodStartDate = selectedDates.first()
                        authViewModel.lastPeriodEndDate = selectedDates.last()
                        Log.d("She&Soul", "Selected Dates: ${selectedDates.first()} ${selectedDates.last()}")
                        authViewModel.createFullProfile()
                    }
                },
                text = "Finish",
                startColor = Color(0xFFBBBDFF),
                endColor = Color(0xFF9092FF),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF9092FF))
            }
        }
    }
}
