package com.example.sheandsoul_nick.features.auth.presentation

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheandsoul_nick.R
import com.example.sheandsoul_nick.data.remote.PcosAssessmentDetailsDto
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import com.example.sheandsoul_nick.ui.theme.SheAndSoulNickTheme

class PcosDashboardViewModelFactory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PcosDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PcosDashboardViewModel(authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun PcosDashboardScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onStartAssessment: () -> Unit
) {
    val viewModel: PcosDashboardViewModel = viewModel(factory = PcosDashboardViewModelFactory(authViewModel))
    val state by viewModel.assessmentState
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkAssessmentStatus()
    }

    PcosDashboardScreenContent(
        username = authViewModel.name,
        assessmentState = state,
        onNavigateBack = onNavigateBack,
        onStartAssessment = onStartAssessment,
        onDownloadClick = {
            val token = authViewModel.token
            if (token != null) {
                downloadPcosReport(context, token)
            }
        }
    )
}


// ✅ --- START OF FIX: RE-INTRODUCED SCAFFOLD AND TOPAPPBAR --- ✅
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PcosDashboardScreenContent(
    username: String,
    assessmentState: AssessmentUiState,
    onNavigateBack: () -> Unit,
    onStartAssessment: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PCOS Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF8F8FF)
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (assessmentState) {
                is AssessmentUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is AssessmentUiState.Success -> {
                    ReportAvailableContent(
                        username = username,
                        details = assessmentState.details,
                        onDownloadClick = onDownloadClick,
                        onRetakeAssessmentClick = onStartAssessment
                    )
                }
                is AssessmentUiState.NoAssessment -> {
                    StartAssessmentContent(onStartAssessment = onStartAssessment)
                }
                is AssessmentUiState.Error -> {
                    Text(
                        text = "Error: ${assessmentState.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
// ✅ --- END OF FIX --- ✅

private fun downloadPcosReport(context: Context, token: String) {
    val url = "https://sheandsoulversion1.onrender.com/api/report/pcos/my-report"
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle("PCOS_Report.pdf")
        .setDescription("Downloading your She&Soul PCOS Report...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "PCOS_Report_${System.currentTimeMillis()}.pdf")
        .addRequestHeader("Authorization", "Bearer $token")

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
}

@Composable
private fun ReportAvailableContent(
    username: String,
    details: PcosAssessmentDetailsDto,
    onDownloadClick: () -> Unit, // This parameter is no longer used but safe to keep
    onRetakeAssessmentClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(text = "Your Health Report", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Hello, $username! Here is a summary of your latest PCOS assessment.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.Gray
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Risk Level", fontWeight = FontWeight.Bold)
                    Text(details.riskLevel, color = Color(0xFF9092FF), fontSize = 20.sp)
                    Text("Assessed on: ${details.assessmentDate}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Answers", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                AnswerRow("Avg. days between periods:", details.cycleLengthDays.toString())
                AnswerRow("Missed periods last year:", details.missedPeriodsInLastYear.toString())
                AnswerRow("Experience severe acne?", details.hasSevereAcne.toYesNo())
                AnswerRow("Excess hair growth?", details.hasExcessHairGrowth.toYesNo())
                AnswerRow("Thinning hair?", details.hasThinningHair.toYesNo())
                AnswerRow("Confirmed ovarian cysts?", details.hasOvarianCystsConfirmedByUltrasound.toYesNo())
                AnswerRow("Struggle with weight gain?", details.hasWeightGainOrObesity.toYesNo())
                AnswerRow("Dark skin patches?", details.hasDarkSkinPatches.toYesNo())
                AnswerRow("Family history of PCOS?", details.hasFamilyHistoryOfPCOS.toYesNo())
                AnswerRow("Experience high stress?", details.experiencesHighStress.toYesNo())
            }
        }

        item {
            // ✅ --- START OF FIX --- ✅
            // The download button and the spacer above it have been removed.
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onRetakeAssessmentClick,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Re-take Assessment", color = Color(0xFF9092FF))
            }
            Spacer(modifier = Modifier.height(24.dp))
            // ✅ --- END OF FIX --- ✅
        }
    }
}
@Composable
private fun AnswerRow(question: String, answer: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = question, color = Color.Gray, fontSize = 14.sp)
        Text(text = answer, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
private fun Boolean.toYesNo() = if (this) "Yes" else "No"

@Composable
private fun StartAssessmentContent(onStartAssessment: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "PCOS Illustration",
            modifier = Modifier.size(200.dp)
                .alpha(0.4f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Know Your Body Better",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "You haven't taken the PCOS assessment yet. Take our quick quiz to understand your symptoms and get personalized insights.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp),
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalWaveButton(
            onClick = onStartAssessment,
            text = "Start Your Assessment Now",
            startColor = Color(0xFFBBBDFF),
            endColor = Color(0xFF9092FF),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}

@Preview(showBackground = true, name = "State: No Assessment Data")
@Composable
fun PcosDashboardScreen_NoData_Preview() {
    SheAndSoulNickTheme {
        PcosDashboardScreenContent(
            username = "Maria",
            assessmentState = AssessmentUiState.NoAssessment,
            onNavigateBack = {},
            onStartAssessment = {},
            onDownloadClick = {}
        )
    }
}

@Preview(showBackground = true, name = "State: With Assessment Data")
@Composable
fun PcosDashboardScreen_WithData_Preview() {
    val sampleDetails = PcosAssessmentDetailsDto(
        riskLevel = "MODERATE",
        assessmentDate = "2025-09-22",
        cycleLengthDays = 36,
        missedPeriodsInLastYear = 4,
        hasSevereAcne = true,
        hasExcessHairGrowth = true,
        hasThinningHair = false,
        hasOvarianCystsConfirmedByUltrasound = false,
        hasWeightGainOrObesity = true,
        hasDarkSkinPatches = false,
        hasFamilyHistoryOfPCOS = false,
        experiencesHighStress = true
    )

    SheAndSoulNickTheme {
        PcosDashboardScreenContent(
            username = "Maria",
            assessmentState = AssessmentUiState.Success(sampleDetails),
            onNavigateBack = {},
            onStartAssessment = {},
            onDownloadClick = {}
        )
    }
}