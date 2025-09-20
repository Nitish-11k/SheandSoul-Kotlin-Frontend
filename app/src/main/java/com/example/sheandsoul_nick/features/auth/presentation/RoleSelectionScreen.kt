package com.example.sheandsoul_nick.features.auth.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton

enum class Role {
    USER, PARTNER
}

@Composable
fun RoleSelectionScreen(
    onContinueClicked: (Role) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedRole by remember { mutableStateOf(Role.USER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(26.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_sheandsoul_text),
                contentDescription = "She & Soul Logo",
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp)
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Choose Your Role",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp)
            )

            // Pass the alignment as a parameter to the card
            RoleCard(
                role = Role.USER,
                title = "User",
                imageRes = R.drawable.ic_user_avtar,
                gradientColors = listOf(Color(0xFFFAE0E8), Color(0xFFFAD2E1)),
                isSelected = selectedRole == Role.USER,
                selectedTextColor = Color(0xFFD90429),
                onClick = { selectedRole = Role.USER }
            )

            Spacer(modifier = Modifier.height(15.dp))

            RoleCard(
                role = Role.PARTNER,
                title = "Partner",
                imageRes = R.drawable.ic_partner_avtar,
                gradientColors = listOf(Color(0xFFBDE0FE), Color(0xFFA9D2FF)),
                isSelected = selectedRole == Role.PARTNER,
                selectedTextColor = Color(0xFF0077B6),
                onClick = { selectedRole = Role.PARTNER }
            )
        }

        HorizontalWaveButton(
            onClick = {
                authViewModel.role = selectedRole
                onContinueClicked(selectedRole) },
            text = "Continue >",
            startColor = Color(0xFFBBBDFF),
            endColor = Color(0xFF9092FF),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(50.dp)
        )
    }
}

// ==================================================================
// ## START OF THE FIX ##
// ==================================================================
@Composable
fun RoleCard(
    role: Role, // Added to determine layout
    title: String,
    @DrawableRes imageRes: Int,
    gradientColors: List<Color>,
    isSelected: Boolean,
    selectedTextColor: Color,
    onClick: () -> Unit
) {
    val colorFilter = if (isSelected) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    val unselectedGradient = listOf(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.1f))

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFFBBBDFF) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = if (isSelected) gradientColors else unselectedGradient
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Determine the order based on the role
            if (role == Role.USER) {
                // User: Image on the left, Text on the right
                UserImage(imageRes = imageRes, title = title, colorFilter = colorFilter)
                Spacer(modifier = Modifier.weight(1f))
                CardText(title = title, isSelected = isSelected, selectedTextColor = selectedTextColor)
            } else {
                // Partner: Text on the left, Image on the right
                CardText(title = title, isSelected = isSelected, selectedTextColor = selectedTextColor)
                Spacer(modifier = Modifier.weight(1f))
                UserImage(imageRes = imageRes, title = title, colorFilter = colorFilter)
            }
        }
    }
}

@Composable
fun UserImage(@DrawableRes imageRes: Int, title: String, colorFilter: ColorFilter?) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = title,
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(0.7f), // Control the width relative to height
        contentScale = ContentScale.Fit, // Use Fit to show the whole avatar
        colorFilter = colorFilter
    )
}

@Composable
fun CardText(title: String, isSelected: Boolean, selectedTextColor: Color) {
    Text(
        text = title,
        color = if (isSelected) selectedTextColor else Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}
// ==================================================================
// ## END OF THE FIX ##
// ==================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RoleSelectionScreenPreview() {
    RoleSelectionScreen(onContinueClicked = {})
}