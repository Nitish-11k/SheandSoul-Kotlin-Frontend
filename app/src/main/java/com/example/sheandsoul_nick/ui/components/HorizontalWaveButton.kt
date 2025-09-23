package com.example.sheandsoul_nick.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HorizontalWaveButton(
    onClick: () -> Unit,
    text: String,
    startColor: Color,
    endColor: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    useVerticalGradient: Boolean = false,
    enabled: Boolean = true // Added enabled parameter
) {
    // Animate the alpha to give a visual cue when the button is disabled
    val alpha by animateFloatAsState(targetValue = if (enabled) 1f else 0.5f, label = "alphaAnim")

    Box(
        modifier = modifier
            .alpha(alpha) // Apply the alpha for disabled state
            .background(
                brush = if (useVerticalGradient) {
                    Brush.verticalGradient(colors = listOf(startColor, endColor))
                } else {
                    Brush.horizontalGradient(colors = listOf(startColor, endColor))
                },
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(enabled = enabled, onClick = onClick), // Use the enabled parameter here
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = if (startColor == Color.White && endColor == Color.White) Color.Black else Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}
