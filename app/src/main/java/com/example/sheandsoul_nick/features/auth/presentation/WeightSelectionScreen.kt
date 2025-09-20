package com.example.sheandsoul_nick.features.auth.presentation

import android.widget.Toast
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

@Composable
fun WeightSelectionScreen(
    onContinueClicked: () -> Unit, // Re-added for navigation
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedUnit by remember { mutableStateOf(WeightUnit.KG) }
    var isLoading by remember { mutableStateOf(false) } // State for loading indicator

    val context = LocalContext.current
    // 1. Observe the profile creation result from the ViewModel
    val profileCreationResult by authViewModel.profileCreationResult.observeAsState()

    // (Your existing state logic is correct)
    val kgList = (30..150).toList()
    val lbsList = (66..330).toList()
    val defaultKg = 60
    val defaultLbs = kgToLbs(defaultKg)
    val listStateKg = rememberLazyListState(initialFirstVisibleItemIndex = (defaultKg - kgList.first()).coerceAtLeast(0))
    val listStateLbs = rememberLazyListState(initialFirstVisibleItemIndex = (defaultLbs - lbsList.first()).coerceAtLeast(0))
    val coroutineScope = rememberCoroutineScope()

    val selectedWeightKg by remember {
        derivedStateOf {
            if (listStateKg.isScrollInProgress) return@derivedStateOf defaultKg
            val centerIndex = listStateKg.firstVisibleItemIndex + listStateKg.layoutInfo.visibleItemsInfo.size / 2
            kgList.getOrElse(centerIndex) { defaultKg }
        }
    }
    val selectedWeightLbs by remember {
        derivedStateOf {
            if (listStateLbs.isScrollInProgress) return@derivedStateOf defaultLbs
            val centerIndex = listStateLbs.firstVisibleItemIndex + listStateLbs.layoutInfo.visibleItemsInfo.size / 2
            lbsList.getOrElse(centerIndex) { defaultLbs }
        }
    }

    // 2. Add this LaunchedEffect to handle the API response
    LaunchedEffect(profileCreationResult) {
        when (val result = profileCreationResult) {
            is AuthResult.Success -> {
                isLoading = false
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                onContinueClicked() // Navigate ONLY after success
            }
            is AuthResult.Error -> {
                isLoading = false
                Toast.makeText(context, "Error: ${result.errorMessage}", Toast.LENGTH_LONG).show()
            }
            is AuthResult.Loading -> {
                isLoading = true
            }
            null -> {}
            is AuthResult.SuccessGoogle -> TODO()
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
                    VerticalNumberPicker(listState = listStateKg, items = kgList, itemHeight = 60.dp) { item, alpha ->
                        Text(text = "$item", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2D2D).copy(alpha = alpha), textAlign = TextAlign.Center)
                    }
                } else {
                    VerticalNumberPicker(listState = listStateLbs, items = lbsList, itemHeight = 60.dp) { item, alpha ->
                        Text(text = "$item", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2D2D).copy(alpha = alpha), textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalWaveButton(
                onClick = {
                    val finalWeightInKg = if (selectedUnit == WeightUnit.KG) selectedWeightKg else lbsToKg(selectedWeightLbs)
                    authViewModel.weight = finalWeightInKg.toFloat()
                    authViewModel.createFullProfile()
                },
                text = "Continue >",
                startColor = Color(0xFFBBBDFF),
                endColor = Color(0xFF9092FF),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }

        // 3. Show the loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false, onClick = {}), // Prevent clicks behind the overlay
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WeightSelectionScreenPreview() {
    // Update the preview to match the correct signature
    WeightSelectionScreen(onContinueClicked = {})
}