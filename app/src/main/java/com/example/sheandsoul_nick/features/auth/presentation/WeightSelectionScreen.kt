package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.sheandsoul_nick.ui.components.ToggleButton
import com.example.sheandsoul_nick.ui.components.VerticalNumberPicker
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// (Your enum and helper functions are correct)
enum class WeightUnit { KG, LBS }
fun kgToLbs(kg: Int): Int = (kg * 2.20462).roundToInt()
fun lbsToKg(lbs: Int): Int = (lbs / 2.20462).roundToInt()

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeightSelectionScreen(
    onContinueClicked: () -> Unit, // Re-added for navigation
    authViewModel: AuthViewModel
) {
    var selectedUnit by remember { mutableStateOf(WeightUnit.KG) }

    // (Your existing state logic is correct)
    val kgList = (30..150).toList() + listOf(null)
    val lbsList = (66..330).toList() + listOf(null)
    val defaultKg = 60
    val defaultLbs = kgToLbs(defaultKg)
    val listStateKg = rememberLazyListState(
        initialFirstVisibleItemIndex = (defaultKg - 30 + 1).coerceAtLeast(0) // +1 because of null at start
    )
    val listStateLbs = rememberLazyListState(
        initialFirstVisibleItemIndex = (defaultLbs - 66 + 1).coerceAtLeast(0)
    )
    val coroutineScope = rememberCoroutineScope()

    val selectedWeightKg by remember {
        derivedStateOf {
            if (listStateKg.layoutInfo.visibleItemsInfo.isEmpty()) return@derivedStateOf defaultKg
            val centerIndex = listStateKg.firstVisibleItemIndex + listStateKg.layoutInfo.visibleItemsInfo.size / 2
            kgList.getOrNull(centerIndex) ?: defaultKg
        }
    }
    val selectedWeightLbs by remember {
        derivedStateOf {
            if (listStateLbs.layoutInfo.visibleItemsInfo.isEmpty()) return@derivedStateOf defaultLbs
            val centerIndex = listStateLbs.firstVisibleItemIndex + listStateLbs.layoutInfo.visibleItemsInfo.size / 2
            lbsList.getOrNull(centerIndex) ?: defaultLbs
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
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
                            val index = (newKg - 30).coerceIn(0, kgList.lastIndex - 1)
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
                            val index = (newLbs - 66).coerceIn(0, lbsList.lastIndex - 1)
                            coroutineScope.launch { listStateLbs.animateScrollToItem(index) }
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
                if (selectedUnit == WeightUnit.KG) {
                    VerticalNumberPicker(
                        listState = listStateKg,
                        items = kgList,
                        itemHeight = 60.dp
                    ) { item, isSelected ->
                        if(item!=null) {
                            Text(
                                text = "$item",
                                fontSize = if (isSelected) 32.sp else 24.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF9092FF) else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }else{
                            Spacer(modifier = Modifier.height(60.dp)) // fake padding row
                        }
                    }
                } else {
                    VerticalNumberPicker(
                        listState = listStateLbs,
                        items = lbsList,
                        itemHeight = 60.dp
                    ) { item, isSelected ->
                        if(item!=null) {
                            Text(
                                text = "$item",
                                fontSize = if (isSelected) 32.sp else 24.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF9092FF) else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        } else{
                            Spacer(modifier = Modifier.height(60.dp)) // fake padding row
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalWaveButton(
                onClick = {
                    val finalWeightInKg = if (selectedUnit == WeightUnit.KG) selectedWeightKg else lbsToKg(selectedWeightLbs)
                    authViewModel.weight = (finalWeightInKg.toFloat()-3)
                    Log.d("She&Soul", "Height,Age,weight selected:  ${authViewModel.height}  ${authViewModel.age} ${authViewModel.weight}")
//                    authViewModel.createFullProfile()
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
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun WeightSelectionScreenPreview() {
//    // Update the preview to match the correct signature
//    WeightSelectionScreen(onContinueClicked = {})
//}