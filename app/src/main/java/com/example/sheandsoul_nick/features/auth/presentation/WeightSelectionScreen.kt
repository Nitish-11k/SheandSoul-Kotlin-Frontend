package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import com.example.sheandsoul_nick.ui.components.ToggleButton
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


enum class WeightUnit { KG, LBS }
fun kgToLbs(kg: Int): Int = (kg * 2.20462).roundToInt()
fun lbsToKg(lbs: Int): Int = (lbs / 2.20462).roundToInt()


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeightSelectionScreen(
    onContinueClicked: () -> Unit,
    authViewModel: AuthViewModel
) {
    var selectedUnit by remember { mutableStateOf(WeightUnit.KG) }

    // --- CONFIGURATION ---
    val itemHeight = 60.dp
    val pickerHeight = 300.dp
    val verticalPadding = (pickerHeight - itemHeight) / 2 // This is the key change!

    // --- DATA LISTS (without nulls) ---
    val kgList = (30..150).toList()
    val lbsList = (66..330).toList()
    val defaultKg = 60
    val defaultLbs = kgToLbs(defaultKg)

    // --- LAZYLIST STATES ---
    val listStateKg = rememberLazyListState(
        initialFirstVisibleItemIndex = (defaultKg - kgList.first()).coerceAtLeast(0)
    )
    val listStateLbs = rememberLazyListState(
        initialFirstVisibleItemIndex = (defaultLbs - lbsList.first()).coerceAtLeast(0)
    )
    val coroutineScope = rememberCoroutineScope()

    // --- IMPROVED DERIVED STATE FOR SELECTION ---
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    val selectedWeightKg by remember {
        derivedStateOf {
            if (listStateKg.layoutInfo.visibleItemsInfo.isEmpty()) {
                defaultKg
            } else {
                val centerItemIndex = listStateKg.firstVisibleItemIndex + (listStateKg.firstVisibleItemScrollOffset / itemHeightPx).roundToInt()
                kgList.getOrElse(centerItemIndex) { defaultKg }
            }
        }
    }
    val selectedWeightLbs by remember {
        derivedStateOf {
            if (listStateLbs.layoutInfo.visibleItemsInfo.isEmpty()) {
                defaultLbs
            } else {
                val centerItemIndex = listStateLbs.firstVisibleItemIndex + (listStateLbs.firstVisibleItemScrollOffset / itemHeightPx).roundToInt()
                lbsList.getOrElse(centerItemIndex) { defaultLbs }
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
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
                text = "And your current weight ?",
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
                    text = "kg",
                    isSelected = selectedUnit == WeightUnit.KG,
                    onClick = {
                        if (selectedUnit != WeightUnit.KG) {
                            selectedUnit = WeightUnit.KG
                            val newKg = lbsToKg(selectedWeightLbs)
                            val index = (newKg - kgList.first()).coerceIn(0, kgList.lastIndex)
                            coroutineScope.launch { listStateKg.animateScrollToItem(index) }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                ToggleButton(
                    text = "lbs",
                    isSelected = selectedUnit == WeightUnit.LBS,
                    onClick = {
                        if (selectedUnit != WeightUnit.LBS) {
                            selectedUnit = WeightUnit.LBS
                            val newLbs = kgToLbs(selectedWeightKg)
                            val index = (newLbs - lbsList.first()).coerceIn(0, lbsList.lastIndex)
                            coroutineScope.launch { listStateLbs.animateScrollToItem(index) }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .height(pickerHeight)
                    .width(150.dp),
                contentAlignment = Alignment.Center
            ) {
                // Central highlight box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFBBBDFF).copy(alpha = 0.1f))
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(listOf(Color(0xFFBBBDFF), Color(0xFF9092FF))),
                            shape = RoundedCornerShape(8.dp)
                        )
                )

                // The Picker itself is just a LazyColumn
                val (list, listState, currentSelection) = if (selectedUnit == WeightUnit.KG) {
                    Triple(kgList, listStateKg, selectedWeightKg)
                } else {
                    Triple(lbsList, listStateLbs, selectedWeightLbs)
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    // ✅ KEY FIX: Add padding to allow first/last items to reach the center
                    contentPadding = PaddingValues(vertical = verticalPadding),
                    // ✨ BONUS: Add snap behavior for a better UX
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
                ) {
                    items(list.size) { index ->
                        val item = list[index]
                        val isSelected = (item == currentSelection)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$item",
                                fontSize = if (isSelected) 32.sp else 24.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF9092FF) else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalWaveButton(
                onClick = {
                    val finalWeightInKg = if (selectedUnit == WeightUnit.KG) selectedWeightKg else lbsToKg(selectedWeightLbs)
                    authViewModel.weight = (finalWeightInKg.toFloat()) // Removed the -3, seems arbitrary
                    Log.d("She&Soul", "Height,Age,weight selected:  ${authViewModel.height}  ${authViewModel.age} ${authViewModel.weight}")
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
}