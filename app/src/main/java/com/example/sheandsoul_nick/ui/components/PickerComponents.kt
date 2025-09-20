package com.example.sheandsoul_nick.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor by remember(isSelected) { mutableStateOf(if (isSelected) Color.White else Color.Transparent) }
    val textColor by remember(isSelected) { mutableStateOf(if (isSelected) Color(0xFF9092FF) else Color.Gray) }

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
    val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
    val centerIndex = remember(visibleItemsInfo) {
        val centerItem = visibleItemsInfo.minByOrNull {
            abs(it.offset + it.size / 2 - listState.layoutInfo.viewportSize.height / 2)
        }
        centerItem?.index ?: (listState.firstVisibleItemIndex + visibleItemsInfo.size / 2)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = (itemHeight * (listState.layoutInfo.visibleItemsInfo.size / 2f).roundToInt())),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items.size) { index ->
            val item = items[index]
            val distance = abs(index - centerIndex)
            val alpha = animateFloatAsState(
                targetValue = when (distance) {
                    0 -> 1f
                    1 -> 0.6f
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