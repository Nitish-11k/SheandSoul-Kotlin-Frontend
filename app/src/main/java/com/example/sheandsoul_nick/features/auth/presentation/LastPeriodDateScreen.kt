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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LastPeriodDateScreen(
    authViewModel: AuthViewModel,
    onFinish: () -> Unit
) {
    val currentSystemMonth = YearMonth.now()
    val twoMonthsAgo = currentSystemMonth.minusMonths(2)
    val currentMonth = remember { mutableStateOf(currentSystemMonth.minusMonths(1)) }

    val selectedDates = remember { mutableStateListOf<LocalDate>() }
    val profileCreationResult by authViewModel.profileCreationResult.observeAsState()

    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Use a default of 7 days if the ViewModel value is not set, and ensure it's not more than 7.
    val periodLength = 7

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
            else -> { /* Do nothing */ }
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
                        // ✨ --- START: REFACTORED RANGE SELECTION LOGIC --- ✨
                        val firstSelection = selectedDates.firstOrNull()

                        if (firstSelection == null || selectedDates.size > 1) {
                            // Case 1: No dates are selected OR a full range is already selected.
                            // Action: Clear everything and start a new selection with the tapped date.
                            selectedDates.clear()
                            selectedDates.add(date)
                        } else {
                            // Case 2: Exactly one date (the start date) is selected.
                            // Action: Define the range from the start date to the newly tapped date.
                            val startDate = firstSelection
                            val endDate = date

                            // Ensure start is before end
                            val rangeStart = if (startDate.isBefore(endDate)) startDate else endDate
                            val rangeEnd = if (startDate.isBefore(endDate)) endDate else startDate

                            val daysInSelection = ChronoUnit.DAYS.between(rangeStart, rangeEnd) + 1

                            if (daysInSelection > periodLength) {
                                // If the selected range is too long, show a toast.
                                Toast.makeText(context, "Period cannot be longer than $periodLength days.", Toast.LENGTH_SHORT).show()
                                // And reset the selection to just the newly tapped date.
                                selectedDates.clear()
                                selectedDates.add(date)
                            } else {
                                // If the range is valid, clear the single selection and add all dates in the range.
                                selectedDates.clear()
                                var currentDate = rangeStart
                                while (!currentDate.isAfter(rangeEnd)) {
                                    selectedDates.add(currentDate)
                                    currentDate = currentDate.plusDays(1)
                                }
                            }
                        }
                        // ✨ --- END: REFACTORED RANGE SELECTION LOGIC --- ✨
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalWaveButton(
            onClick = {
                if (selectedDates.isNotEmpty()) {
                    authViewModel.last_period_start_date = selectedDates.minOrNull()!!
                    authViewModel.last_period_end_date = selectedDates.maxOrNull()!!
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
                .height(50.dp)
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
                        Spacer(modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f))
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
    LastPeriodDateScreen(authViewModel = viewModel(), onFinish = {})
}