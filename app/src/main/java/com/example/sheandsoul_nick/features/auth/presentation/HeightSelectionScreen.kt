package com.example.sheandsoul_nick.features.auth.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// Define the units for height
enum class HeightUnit { CM, FT }

// Helper function to convert CM to Feet and Inches
fun cmToFtIn(cm: Int): Pair<Int, Int> {
    val totalInches = cm / 2.54
    val feet = (totalInches / 12).toInt()
    val inches = (totalInches % 12).roundToInt()
    return feet to inches
}

// Helper function to convert Feet and Inches to CM
fun ftInToCm(feet: Int, inches: Int): Int {
    val totalInches = (feet * 12) + inches
    return (totalInches * 2.54).roundToInt()
}

// Helper function to generate a list of feet and inches pairs
fun generateFtInList(startFt: Int, endFt: Int): List<Pair<Int, Int>> {
    val list = mutableListOf<Pair<Int, Int>>()
    for (ft in startFt..endFt) {
        for (inch in 0..11) {
            list.add(ft to inch)
        }
    }
    return list
}

@Composable
fun HeightSelectionScreen(
    onContinueClicked: (Float, HeightUnit) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedUnit by remember { mutableStateOf(HeightUnit.CM) }

    val cmList = (100..220).toList()
    val ftInList = remember { generateFtInList(2, 7) } // Range from 2'0" to 7'11"

    val listStateCm = rememberLazyListState(initialFirstVisibleItemIndex = (120 - cmList.first()).coerceAtLeast(0))
    val listStateFt = rememberLazyListState(initialFirstVisibleItemIndex = ftInList.indexOf(5 to 7).coerceAtLeast(0))

    val coroutineScope = rememberCoroutineScope()

    // Get the currently selected height from the center of the list
    val selectedHeightCm by remember {
        derivedStateOf {
            val centerIndex = listStateCm.firstVisibleItemIndex + listStateCm.layoutInfo.visibleItemsInfo.size / 2
            cmList.getOrElse(centerIndex) { 120 }
        }
    }
    val selectedHeightFt by remember {
        derivedStateOf {
            val centerIndex = listStateFt.firstVisibleItemIndex + listStateFt.layoutInfo.visibleItemsInfo.size / 2
            ftInList.getOrElse(centerIndex) { 5 to 7 }
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
            text = "What's your height ?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // CM / FT toggle buttons
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
                        val index = (newCm - cmList.first()).coerceIn(0, cmList.lastIndex)
                        coroutineScope.launch { listStateCm.scrollToItem(index) }
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
                        val index = ftInList.indexOf(newFtIn).coerceIn(0, ftInList.lastIndex)
                        coroutineScope.launch { listStateFt.scrollToItem(index) }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Height picker display
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
                ) { item, alpha ->
                    Text(
                        text = "$item",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D).copy(alpha = alpha),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                VerticalNumberPicker(
                    listState = listStateFt,
                    items = ftInList,
                    itemHeight = 60.dp,
                ) { item, alpha ->
                    Text(
                        text = "${item.first}' ${item.second}\"",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D).copy(alpha = alpha),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalWaveButton(
            onClick = {

                val finalHeight = if (selectedUnit == HeightUnit.CM) selectedHeightCm else ftInToCm(selectedHeightFt.first, selectedHeightFt.second)
                val finalHeightFloat = finalHeight.toFloat()
                authViewModel.height =finalHeightFloat
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

@Composable
fun ToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor by remember(isSelected) {
        mutableStateOf(if (isSelected) Color.White else Color.Transparent)
    }
    val textColor by remember(isSelected) {
        mutableStateOf(if (isSelected) Color(0xFF9092FF) else Color.Gray)
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun <T> VerticalNumberPicker(
    listState: LazyListState,
    items: List<T>,
    itemHeight: Dp,
    content: @Composable (T, Float) -> Unit
) {
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val halfVisibleItems = (listState.layoutInfo.visibleItemsInfo.size / 2f).roundToInt()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = (itemHeight * halfVisibleItems)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items.size) { index ->
            val item = items[index]
            val centerIndex = listState.firstVisibleItemIndex + halfVisibleItems
            val distance = abs(index - centerIndex)
            val alpha = animateFloatAsState(
                targetValue = when (distance) {
                    0 -> 1f
                    1 -> 0.5f
                    2 -> 0.3f
                    else -> 0.2f
                },
                animationSpec = tween(300),
                label = "alphaAnimation"
            ).value

            Box(
                modifier = Modifier.height(itemHeight),
                contentAlignment = Alignment.Center
            ) {
                content(item, alpha)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HeightSelectionScreenPreview() {
    HeightSelectionScreen(onContinueClicked = { _, _ -> })
}