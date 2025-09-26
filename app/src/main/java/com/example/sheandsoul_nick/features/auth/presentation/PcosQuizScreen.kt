package com.example.sheandsoul_nick.features.auth.presentation

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.sheandsoul_nick.ui.components.HorizontalWaveButton
import kotlinx.coroutines.flow.collectLatest

// ✅ FIX: Moved the factory to the top level of the file to resolve potential reference issues.
class PcosQuizViewModelFactory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PcosQuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PcosQuizViewModel(authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PcosQuizScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onAssessmentComplete: (String) -> Unit
) {
    val quizViewModel: PcosQuizViewModel = viewModel(factory = PcosQuizViewModelFactory(authViewModel))
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val questions = quizViewModel.questions
    val currentQuestion = questions[currentQuestionIndex]
    val answers = quizViewModel.answers
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PCOS Assessment") }, // ✅ FIX: Corrected typo
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val progress by animateFloatAsState(
                    targetValue = (currentQuestionIndex + 1) / questions.size.toFloat(),
                    label = "progressAnim"
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                // This scrollable column holds the question and answer, taking up the available space.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center // This centers the content vertically.
                ) {
                    Text(
                        text = currentQuestion.text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )

                    // Answer Section
                    when (currentQuestion.answerType) {
                        AnswerType.NUMBER -> {
                            var textValue by remember(currentQuestionIndex) {
                                mutableStateOf(answers[currentQuestion.id]?.toString() ?: "")
                            }
                            OutlinedTextField(
                                value = textValue,
                                onValueChange = {
                                    textValue = it.filter { char -> char.isDigit() }
                                    answers[currentQuestion.id] = textValue.toIntOrNull() as Any
                                },
                                label = { Text("Enter a number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        AnswerType.YES_NO -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                            ) {
                                AnswerButton(
                                    text = "Yes",
                                    isSelected = answers[currentQuestion.id] == true,
                                    onClick = { answers[currentQuestion.id] = true },
                                    modifier = Modifier.weight(1f)
                                )
                                AnswerButton(
                                    text = "No",
                                    isSelected = answers[currentQuestion.id] == false,
                                    onClick = { answers[currentQuestion.id] = false },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Buttons remain at the bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentQuestionIndex > 0) {
                        OutlinedButton(
                            onClick = { currentQuestionIndex-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    } else {
                        Spacer(modifier = Modifier.weight(1f)) // Placeholder to keep "Next" button aligned
                    }

                    HorizontalWaveButton(
                        onClick = {
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                            } else {
                                quizViewModel.submitAssessment()
                            }
                        },
                        text = if (currentQuestionIndex < questions.size - 1) "Next" else "Submit",
                        startColor = Color(0xFFBBBDFF),
                        endColor = Color(0xFF9092FF),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        enabled = answers[currentQuestion.id] != null
                    )
                }
            }

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
}

@Composable
fun AnswerButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF9092FF) else Color.LightGray,
            contentColor = if (isSelected) Color.White else Color.Black
        )
    ) {
        Text(text)
    }
}

