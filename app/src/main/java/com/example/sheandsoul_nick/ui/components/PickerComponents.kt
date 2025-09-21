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
import androidx.compose.ui.platform.LocalDensity
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
    listState: androidx.compose.foundation.lazy.LazyListState,
    items: List<T>,
    itemHeight: Dp,
    selectorHeight: Dp = 60.dp, // default matches your box
    content: @Composable (T, Boolean) -> Unit
) {
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val selectorHeightPx = with(LocalDensity.current) { selectorHeight.toPx() }

    // ✅ Corrected selected index calculation
    val selectedIndex by remember {
        derivedStateOf {
            val visibleInfo = listState.layoutInfo.visibleItemsInfo
            if (visibleInfo.isEmpty()) return@derivedStateOf 0

            val selectionCenter =
                (listState.layoutInfo.viewportEndOffset + listState.layoutInfo.viewportStartOffset) / 2

            val adjustedCenter = selectionCenter + (selectorHeightPx / 2) - (itemHeightPx / 2)

            visibleInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - adjustedCenter)
            }?.index ?: 0
        }
    }

    // Auto snap when scroll stops
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val visibleInfo = listState.layoutInfo.visibleItemsInfo
            if (visibleInfo.isNotEmpty()) {
                val selectionCenter = listState.layoutInfo.viewportEndOffset / 2

                val nearest = visibleInfo.minByOrNull {
                    abs((it.offset + it.size / 2) - selectionCenter)
                }
                nearest?.let {
                    listState.animateScrollToItem(it.index)
                }
            }
        }
    }



    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = itemHeight * 3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items.size) { index ->
            val item = items[index]
            val isSelected = index == selectedIndex

            Box(
                modifier = Modifier.height(itemHeight),
                contentAlignment = Alignment.Center
            ) {
                content(item, isSelected)
            }
        }
    }
}
