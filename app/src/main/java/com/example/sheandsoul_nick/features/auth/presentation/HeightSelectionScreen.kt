package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.sheandsoul_nick.ui.components.ToggleButton
import com.example.sheandsoul_nick.ui.components.VerticalNumberPicker
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Define the units for height
enum class HeightUnit { CM, FT }

// Helper functions (cmToFtIn, ftInToCm, generateFtInList) remain the same...
fun cmToFtIn(cm: Int): Pair<Int, Int> {
    val totalInches = cm / 2.54
    val feet = (totalInches / 12).toInt()
    val inches = (totalInches % 12).roundToInt()
    return feet to inches
}

fun ftInToCm(feet: Int, inches: Int): Int {
    val totalInches = (feet * 12) + inches
    return (totalInches * 2.54).roundToInt()
}

fun generateFtInList(startFt: Int, endFt: Int): List<Pair<Int, Int>> {
    val list = mutableListOf<Pair<Int, Int>>()
    for (ft in startFt..endFt) {
        for (inch in 0..11) {
            list.add(ft to inch)
        }
    }
    return list
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeightSelectionScreen(
    onContinueClicked: (Float, HeightUnit) -> Unit,
    authViewModel: AuthViewModel
) {
    var selectedUnit by remember { mutableStateOf(HeightUnit.CM) }

    // ✅ FIX 1: Add two `null` items to the beginning and end of each list for padding.
    val cmList = listOf(null, null) + (100..220).toList() + listOf(null, null)
    val ftInList = remember { listOf(null, null) + generateFtInList(2, 7) + listOf(null, null) }

    // ✅ FIX 2: Update initial index to account for the new padding items.
    // To center '120 cm', its new index is 22 (20 for 100-119, plus 2 for nulls).
    val listStateCm = rememberLazyListState(initialFirstVisibleItemIndex = 22)
    // To center '5ft 7in', find its new index in the padded list.
    val listStateFt = rememberLazyListState(initialFirstVisibleItemIndex = ftInList.indexOf(5 to 7))

    val coroutineScope = rememberCoroutineScope()

    val selectedHeightCm by remember {
        derivedStateOf {
            if (listStateCm.layoutInfo.visibleItemsInfo.isEmpty()) return@derivedStateOf 120
            val centerIndex = listStateCm.firstVisibleItemIndex + listStateCm.layoutInfo.visibleItemsInfo.size / 2
            cmList.getOrNull(centerIndex) ?: 120
        }
    }
    val selectedHeightFt by remember {
        derivedStateOf {
            if (listStateFt.layoutInfo.visibleItemsInfo.isEmpty()) return@derivedStateOf (5 to 7)
            val centerIndex = listStateFt.firstVisibleItemIndex + listStateFt.layoutInfo.visibleItemsInfo.size / 2
            ftInList.getOrNull(centerIndex) ?: (5 to 7)
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
            text = "What's your height ?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF0F0F0)),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToggleButton(
                text = "cm",
                isSelected = selectedUnit == HeightUnit.CM,
                onClick = {
                    if (selectedUnit != HeightUnit.CM) {
                        selectedUnit = HeightUnit.CM
                        val newCm = ftInToCm(selectedHeightFt.first, selectedHeightFt.second)
                        val index = cmList.indexOf(newCm).coerceAtLeast(0)
                        coroutineScope.launch { listStateCm.animateScrollToItem(index) }
                    }
                },
                modifier = Modifier.weight(1f)
            )
            ToggleButton(
                text = "ft",
                isSelected = selectedUnit == HeightUnit.FT,
                onClick = {
                    if (selectedUnit != HeightUnit.FT) {
                        selectedUnit = HeightUnit.FT
                        val newFtIn = cmToFtIn(selectedHeightCm)
                        val index = ftInList.indexOf(newFtIn).coerceAtLeast(0)
                        coroutineScope.launch { listStateFt.animateScrollToItem(index) }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

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

            if (selectedUnit == HeightUnit.CM) {
                VerticalNumberPicker(
                    listState = listStateCm,
                    items = cmList,
                    itemHeight = 60.dp,
                    selectorHeight = 60.dp
                ) { item, isSelected ->
                    if (item != null) {
                        Text(
                            text = "$item",
                            fontSize = if (isSelected) 32.sp else 24.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF9092FF) else Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Spacer(Modifier.height(60.dp))
                    }
                }
            } else {
                VerticalNumberPicker(
                    listState = listStateFt,
                    items = ftInList,
                    itemHeight = 60.dp,
                    selectorHeight = 60.dp
                ) { item, isSelected ->
                    if (item != null) {
                        Text(
                            text = "${item.first}' ${item.second}\"",
                            fontSize = if (isSelected) 32.sp else 24.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF9092FF) else Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Spacer(Modifier.height(60.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalWaveButton(
            onClick = {
                val finalHeight = if (selectedUnit == HeightUnit.CM) selectedHeightCm else ftInToCm(selectedHeightFt.first, selectedHeightFt.second)
                val finalHeightFloat = finalHeight.toFloat()
                authViewModel.height = (finalHeightFloat - 3f)
                Log.d("She&Soul", "Height,Age selected: $finalHeightFloat ${authViewModel.height}  ${authViewModel.age}")
                onContinueClicked(finalHeightFloat, selectedUnit)
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