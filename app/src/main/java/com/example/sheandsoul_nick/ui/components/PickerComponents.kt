package com.example.sheandsoul_nick.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
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
    listState: LazyListState,
    items: List<T>,
    itemHeight: Dp,
    selectorHeight: Dp = 60.dp,
    content: @Composable (T, Boolean) -> Unit
) {

    // Selected index from center
    val selectedIndexState = remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visible = layoutInfo.visibleItemsInfo
            if (visible.isEmpty()) return@derivedStateOf 0
            val viewportCenter = layoutInfo.viewportSize.height / 2f
            visible.minByOrNull { item ->
                abs((item.offset + item.size / 2f) - viewportCenter)
            }?.index ?: visible.first().index
        }
    }

    // Snap correctly after fling ends
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (!isScrolling) {
                    val layoutInfo = listState.layoutInfo
                    val visible = layoutInfo.visibleItemsInfo
                    if (visible.isNotEmpty()) {
                        val viewportCenter = layoutInfo.viewportSize.height / 2f
                        val nearest = visible.minByOrNull { item ->
                            abs((item.offset + item.size / 2f) - viewportCenter)
                        }

                        nearest?.let { item ->
                            val itemCenter = item.offset + item.size / 2f
                            val diff = (itemCenter - viewportCenter).roundToInt()

                            // Scroll by the exact difference instead of next item
                            if (diff != 0) {
                                listState.scrollBy(diff.toFloat())
                            }
                        }
                    }
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = (selectorHeight - itemHeight) / 2),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items.size) { index ->
            val item = items[index]
            val isSelected = index == selectedIndexState.value

            Box(
                modifier = Modifier.height(itemHeight),
                contentAlignment = Alignment.Center
            ) {
                content(item, isSelected)
            }
        }
    }
}



