package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import com.example.sheandsoul_nick.ui.components.VerticalNumberPicker
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCycleDetailsScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    // Period Length Picker (1–7 with null padding)
    val periodLengthList: List<Int?> = listOf(null) + (1..7).toList() + listOf(null)
    val initialPeriodIndex = authViewModel.period_length.coerceIn(1, 7)
    val periodListState = rememberLazyListState(initialFirstVisibleItemIndex = initialPeriodIndex)

    val selectedPeriodLength by remember {
        derivedStateOf {
            val visibleItems = periodListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                authViewModel.period_length.coerceIn(1, 7)
            } else {
                val centerIndex =
                    periodListState.firstVisibleItemIndex + visibleItems.size / 2
                periodLengthList.getOrNull(centerIndex) ?: authViewModel.period_length
            }
        }
    }

    // Cycle Length Picker (1–28 with null padding)
    val cycleLengthList: List<Int?> = listOf(null) + (1..28).toList() + listOf(null)
    val initialCycleIndex = authViewModel.cycle_length.coerceIn(1, 28)
    val cycleListState = rememberLazyListState(initialFirstVisibleItemIndex = initialCycleIndex)

    val selectedCycleLength by remember {
        derivedStateOf {
            val visibleItems = cycleListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                authViewModel.cycle_length.coerceIn(1, 28)
            } else {
                val centerIndex =
                    cycleListState.firstVisibleItemIndex + visibleItems.size / 2
                cycleLengthList.getOrNull(centerIndex) ?: authViewModel.cycle_length
            }
        }
    }

    // Calendar states
    val selectedDates = remember { mutableStateListOf<LocalDate>() }
    val currentSystemMonth = YearMonth.now()
    val twoMonthsAgo = currentSystemMonth.minusMonths(2)
    val currentMonth = remember { mutableStateOf(currentSystemMonth) }
    val maxSelectionDays = 7

    val updateResult by authViewModel.menstrualUpdateResult.observeAsState()

    LaunchedEffect(updateResult) {
        when (val result = updateResult) {
            is AuthResult.Success -> {
                isLoading = false
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
            is AuthResult.Error -> {
                isLoading = false
                Toast.makeText(context, "Error: ${result.errorMessage}", Toast.LENGTH_LONG).show()
            }
            is AuthResult.Loading -> isLoading = true
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Cycle Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Usual Period Length", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    NumberPickerBox(
                        listState = periodListState,
                        items = periodLengthList,
                        unit = "days"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text("Usual Cycle Length", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    NumberPickerBox(
                        listState = cycleListState,
                        items = cycleLengthList,
                        unit = "days"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text("Select Last Period Dates", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFBBBDFF).copy(alpha = 0.1f))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            EditMonthNavigationHeader(
                                currentMonth = currentMonth.value,
                                onPreviousMonth = {
                                    if (currentMonth.value.isAfter(twoMonthsAgo))
                                        currentMonth.value = currentMonth.value.minusMonths(1)
                                },
                                onNextMonth = {
                                    if (currentMonth.value.isBefore(currentSystemMonth))
                                        currentMonth.value = currentMonth.value.plusMonths(1)
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
                                    Text(
                                        it,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            EditCalendarGrid(
                                currentMonth = currentMonth.value,
                                selectedDates = selectedDates,
                                onDateSelected = { date ->
                                    if (selectedDates.isEmpty()) {
                                        selectedDates.add(date)
                                    } else {
                                        val minDate = selectedDates.minOrNull()!!
                                        val maxDate = selectedDates.maxOrNull()!!
                                        if (selectedDates.contains(date)) {
                                            selectedDates.clear()
                                            selectedDates.add(date)
                                        } else if (date == maxDate.plusDays(1) || date == minDate.minusDays(1)) {
                                            if (selectedDates.size < maxSelectionDays) {
                                                selectedDates.add(date)
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "You can select up to $maxSelectionDays consecutive days.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            selectedDates.clear()
                                            selectedDates.add(date)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalWaveButton(
                        onClick = {
                            if (selectedDates.isNotEmpty()) {
                                selectedDates.sort()
                                authViewModel.period_length = selectedPeriodLength-1
                                authViewModel.cycle_length = selectedCycleLength-1
                                authViewModel.last_period_start_date = selectedDates.first()
                                authViewModel.last_period_end_date = selectedDates.last()
                                Log.i("She&Soul",authViewModel.period_length.toString()+" "+authViewModel.cycle_length+" "+authViewModel.last_period_start_date+" "+authViewModel.last_period_end_date)
                                authViewModel.updateMenstrualCycleData()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please select at least one date for your last period.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        text = "Save Changes",
                        startColor = Color(0xFFBBBDFF),
                        endColor = Color(0xFF9092FF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = selectedDates.isNotEmpty()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF9092FF))
                }
            }
        }
    }
}

@Composable
private fun NumberPickerBox(
    listState: LazyListState,
    items: List<Int?>,
    unit: String
) {
    Box(
        modifier = Modifier
            .height(150.dp)
            .width(180.dp),
        contentAlignment = Alignment.Center
    ) {
        // Box for selector background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
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
            items = items,
            itemHeight = 50.dp,
            selectorHeight = 50.dp
        ) { item, isSelected ->
            if (item != null) {
                Text(
                    text = "$item $unit",
                    fontSize = if (isSelected) 28.sp else 20.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF9092FF) else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EditMonthNavigationHeader(
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
private fun EditCalendarGrid(
    currentMonth: YearMonth,
    selectedDates: List<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val totalDays = currentMonth.lengthOfMonth()
    val firstDayIndex = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
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
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}
