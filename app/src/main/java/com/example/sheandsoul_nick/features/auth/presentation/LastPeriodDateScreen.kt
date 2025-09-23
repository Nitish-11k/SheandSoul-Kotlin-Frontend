package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import com.example.sheandsoul_nick.ui.theme.SheAndSoulNickTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LastPeriodDateScreen(
    authViewModel: AuthViewModel,
    onFinish: () -> Unit
) {
    val currentSystemMonth = YearMonth.now()
    val twoMonthsAgo = currentSystemMonth.minusMonths(2)
    val currentMonth = remember { mutableStateOf(currentSystemMonth) }

    val selectedDates = remember { mutableStateListOf<LocalDate>() }
    val profileCreationResult by authViewModel.profileCreationResult.observeAsState()

    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val maxSelectionDays = 7

    LaunchedEffect(profileCreationResult) {
        when (val result = profileCreationResult) {
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
            else -> { /* Do nothing */
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
            text = "When did your last period start?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFBBBDFF).copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MonthNavigationHeader(
                    currentMonth = currentMonth.value,
                    onPreviousMonth = {
                        if (currentMonth.value.isAfter(twoMonthsAgo)) {
                            currentMonth.value = currentMonth.value.minusMonths(1)
                        }
                    },
                    onNextMonth = {
                        if (currentMonth.value.isBefore(currentSystemMonth)) {
                            currentMonth.value = currentMonth.value.plusMonths(1)
                        }
                    },
                    isPreviousEnabled = currentMonth.value.isAfter(twoMonthsAgo),
                    isNextEnabled = currentMonth.value.isBefore(currentSystemMonth)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                        Text(it, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                CalendarGrid(
                    currentMonth = currentMonth.value,
                    selectedDates = selectedDates,
                    onDateSelected = { date ->
                        // New logic for consecutive date selection
                        if (selectedDates.isEmpty()) {
                            // 1. Start a new selection
                            selectedDates.add(date)
                        } else {
                            val minDate = selectedDates.minOrNull()!!
                            val maxDate = selectedDates.maxOrNull()!!

                            if (selectedDates.contains(date)) {
                                // 2. Tapped a selected date - reset to this date
                                selectedDates.clear()
                                selectedDates.add(date)
                            } else if (date == maxDate.plusDays(1) || date == minDate.minusDays(1)) {
                                // 3. Tapped an adjacent date - extend selection
                                if (selectedDates.size < maxSelectionDays) {
                                    selectedDates.add(date)
                                } else {
                                    Toast.makeText(context, "You can select up to $maxSelectionDays consecutive days.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // 4. Tapped a non-adjacent date - start new selection
                                selectedDates.clear()
                                selectedDates.add(date)
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalWaveButton(
            onClick = {
                if (selectedDates.isNotEmpty()) {
                    // Sort the dates to correctly find the min and max
                    selectedDates.sort()
                    authViewModel.last_period_start_date = selectedDates.first()
                    authViewModel.last_period_end_date = selectedDates.last()
                    // The actual period length is the number of days selected
                    authViewModel.period_length = selectedDates.size
                    authViewModel.finalizeOnboardingAndSaveData()
                } else {
                    Toast.makeText(context, "Please select your last period date(s).", Toast.LENGTH_SHORT).show()
                }
            },
            text = "Finish",
            startColor = Color(0xFFBBBDFF),
            endColor = Color(0xFF9092FF),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = selectedDates.isNotEmpty() // Disable button if no dates are selected
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false, onClick = {}),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF9092FF))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthNavigationHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    isPreviousEnabled: Boolean,
    isNextEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth, enabled = isPreviousEnabled) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = "Previous Month",
                tint = if (isPreviousEnabled) LocalContentColor.current else Color.Gray
            )
        }
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextMonth, enabled = isNextEnabled) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Next Month",
                tint = if (isNextEnabled) LocalContentColor.current else Color.Gray
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDates: List<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val totalDays = currentMonth.lengthOfMonth()
    val firstDayIndex = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0, Saturday = 6
    val totalCells = (firstDayIndex + totalDays + 6) / 7 * 7

    Column {
        for (i in 0 until totalCells step 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (j in 0..6) {
                    val dayOfMonth = i + j - firstDayIndex + 1
                    if (dayOfMonth in 1..totalDays) {
                        val date = currentMonth.atDay(dayOfMonth)
                        val isSelected = selectedDates.contains(date)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF9092FF) else Color.Transparent)
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayOfMonth",
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LastPeriodDateScreenPreview() {
    SheAndSoulNickTheme {
        LastPeriodDateScreen(authViewModel = viewModel(), onFinish = {})
    }
}

