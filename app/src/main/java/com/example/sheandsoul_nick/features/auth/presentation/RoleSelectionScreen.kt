package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.sheandsoul_nick.ui.theme.SheAndSoulNickTheme

enum class Role {
    USER, PARTNER
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoleSelectionScreen(
    onContinueClicked: (Role) -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedRole by remember { mutableStateOf(Role.USER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // This Column takes up the available space and centers its content vertically.
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_sheandsoul_text),
                contentDescription = "She & Soul Logo",
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp)
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Choose Your Role",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            RoleCard(
                role = Role.USER,
                title = "User",
                imageRes = R.drawable.ic_user_avtar,
                gradientColors = listOf(Color(0xFFFAE0E8), Color(0xFFFAD2E1)),
                isSelected = selectedRole == Role.USER,
                selectedTextColor = Color(0xFFD90429),
                onClick = { selectedRole = Role.USER }
            )

            Spacer(modifier = Modifier.height(24.dp))

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

        // The button is placed after the weighted Column to keep it at the bottom.
        HorizontalWaveButton(
            onClick = {
                authViewModel.role = selectedRole
                onContinueClicked(selectedRole)
            },
            text = "Continue >",
            startColor = Color(0xFFBBBDFF),
            endColor = Color(0xFF9092FF),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp) // Added padding from the bottom edge
                .height(50.dp)
        )
    }
}

@Composable
fun RoleCard(
    role: Role,
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
            if (role == Role.USER) {
                UserImage(imageRes = imageRes, title = title, colorFilter = colorFilter)
                Spacer(modifier = Modifier.weight(1f))
                CardText(title = title, isSelected = isSelected, selectedTextColor = selectedTextColor)
            } else {
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
            .aspectRatio(0.7f),
        contentScale = ContentScale.Fit,
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RoleSelectionScreenPreview() {
    SheAndSoulNickTheme {
        RoleSelectionScreen(onContinueClicked = {})
    }
}
