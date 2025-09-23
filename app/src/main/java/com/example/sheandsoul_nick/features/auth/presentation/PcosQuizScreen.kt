package com.example.sheandsoul_nick.features.auth.presentation

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PcosQuizScreen(
    authViewModel: AuthViewModel,
    onAssessmentComplete: (String) -> Unit // Callback to navigate to a results screen
) {
    // We use a factory here if the ViewModel has dependencies
    val quizViewModel: PcosQuizViewModel = viewModel(factory = PcosQuizViewModelFactory(authViewModel))

    var currentQuestionIndex by remember { mutableStateOf(0) }
    val questions = quizViewModel.questions
    val currentQuestion = questions[currentQuestionIndex]
    val answers = quizViewModel.answers

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    // Listen for results from the ViewModel
    LaunchedEffect(Unit) {
        quizViewModel.quizResult.collectLatest { result ->
            isLoading = when (result) {
                is QuizResult.Loading -> true
                is QuizResult.Success -> {
                    Toast.makeText(context, "Assessment Complete!", Toast.LENGTH_SHORT).show()
                    onAssessmentComplete(result.response.riskLevel)
                    false
                }
                is QuizResult.Error -> {
                    Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                    false
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val progress by animateFloatAsState(
                targetValue = (currentQuestionIndex + 1) / questions.size.toFloat(),
                label = "progressAnim"
            )

            Text(
                text = "PCOS Assessment",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Question Text
            Text(
                text = currentQuestion.text,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Answer Section
            when (currentQuestion.answerType) {
                AnswerType.NUMBER -> {
                    var textValue by remember {
                        mutableStateOf(answers[currentQuestion.id]?.toString() ?: "")
                    }
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = {
                            textValue = it.filter { char -> char.isDigit() }
                            answers[currentQuestion.id] = textValue.toIntOrNull() ?: 0
                        },
                        label = { Text("Enter a number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                AnswerType.YES_NO -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        Button(onClick = { answers[currentQuestion.id] = true }) { Text("Yes") }
                        Button(onClick = { answers[currentQuestion.id] = false }) { Text("No") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentQuestionIndex > 0) {
                    Button(onClick = { currentQuestionIndex-- }) { Text("Back") }
                } else {
                    Spacer(modifier = Modifier) // Placeholder to keep "Next" on the right
                }

                Button(
                    onClick = {
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                        } else {
                            quizViewModel.submitAssessment()
                        }
                    },
                    enabled = answers[currentQuestion.id] != null
                ) {
                    Text(if (currentQuestionIndex < questions.size - 1) "Next" else "Submit")
                }
            }
        }

        // Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}


// You'll need a factory to pass the AuthViewModel to the PcosQuizViewModel

class PcosQuizViewModelFactory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PcosQuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PcosQuizViewModel(authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
