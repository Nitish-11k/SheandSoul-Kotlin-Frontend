package com.example.sheandsoul_nick.features.auth.presentation

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class PcosChatMessage(
    val text: String,
    val isFromBot: Boolean,
    val question: Question? = null
)

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
    val questions = quizViewModel.questions
    val context = LocalContext.current

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No questions available. Please try again later.")
        }
        return
    }

    var isLoading by remember { mutableStateOf(false) }
    var chatMessages by remember { mutableStateOf(listOf<PcosChatMessage>()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var currentQuestion by remember { mutableStateOf(questions.first()) }
    var showUserInput by remember { mutableStateOf(false) }
    var isBotTyping by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        showUserInput = false
        val firstBotMessage = PcosChatMessage("Let's start your PCOS assessment. I'll ask you some quick questions.", isFromBot = true)
        chatMessages = listOf(firstBotMessage)

        kotlinx.coroutines.delay(1200)

        val firstQuestionMessage = PcosChatMessage(questions.first().text, isFromBot = true, question = questions.first())
        chatMessages = listOf(firstQuestionMessage) + chatMessages
        showUserInput = true
    }

    LaunchedEffect(Unit) {
        quizViewModel.quizResult.collectLatest { result ->
            isLoading = when (result) {
                is QuizResult.Loading -> true
                is QuizResult.Success -> {
                    isBotTyping = false
                    Toast.makeText(context, "Assessment Complete!", Toast.LENGTH_SHORT).show()
                    onAssessmentComplete(result.response.riskLevel)
                    false
                }
                is QuizResult.Error -> {
                    isBotTyping = false
                    Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                    chatMessages = listOf(PcosChatMessage("Sorry, something went wrong. Please try submitting again.", isFromBot = true)) + chatMessages
                    false
                }
            }
        }
    }

    fun nextQuestion() {
        showUserInput = false
        isBotTyping = true
        coroutineScope.launch {
            if (currentQuestionIndex < questions.size - 1) {
                val nextIndex = currentQuestionIndex + 1
                currentQuestionIndex = nextIndex
                currentQuestion = questions[nextIndex]
                kotlinx.coroutines.delay(1200)
                isBotTyping = false
                chatMessages = listOf(PcosChatMessage(currentQuestion.text, isFromBot = true, question = currentQuestion)) + chatMessages
                showUserInput = true
            } else {
                kotlinx.coroutines.delay(1200)
                chatMessages = listOf(PcosChatMessage("Thank you! I'm now calculating your results...", isFromBot = true)) + chatMessages
                quizViewModel.submitAssessment()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PCOS Assessment") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            val progress = (currentQuestionIndex) / questions.size.toFloat()

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            LazyColumn(
                state = lazyListState,
                reverseLayout = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (isBotTyping) {
                    item {
                        TypingIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                itemsIndexed(
                    items = chatMessages,
                    key = { index, _ -> "msg_$index" }
                ) { _, message ->
                    PcosMessageBubble(message)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            LaunchedEffect(chatMessages.size, isBotTyping) {
                if (lazyListState.firstVisibleItemIndex < 2) {
                    lazyListState.scrollToItem(index = 0)
                }
            }

            if (showUserInput && !isLoading && !isBotTyping) {
                UserInput(
                    question = currentQuestion,
                    onAnswer = { answerText, answerValue ->
                        chatMessages = listOf(PcosChatMessage(answerText, isFromBot = false)) + chatMessages
                        quizViewModel.answers[currentQuestion.id] = answerValue
                        nextQuestion()
                    }
                )
            }

            if (isLoading && !isBotTyping) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
        }
    }
}


@Composable
fun PcosMessageBubble(message: PcosChatMessage) {
    val alignment = if (message.isFromBot) Alignment.CenterStart else Alignment.CenterEnd
    val backgroundColor = if (message.isFromBot) Color.White else Color(0xFF9092FF)
    val textColor = if (message.isFromBot) Color.Black else Color.White
    val shape = if (message.isFromBot) {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Text(
            text = message.text,
            color = textColor,
            modifier = Modifier
                .clip(shape)
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun UserInput(question: Question, onAnswer: (String, Any) -> Unit) {
    var selectedAnswer by remember { mutableStateOf<Boolean?>(null) }
    // ✅ FIX: Re-introduce the coroutine scope to handle the delay.
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(question) {
        selectedAnswer = null
    }

    Surface(shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            when (question.answerType) {
                AnswerType.YES_NO -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                if (selectedAnswer == null) { // Prevent clicking again
                                    selectedAnswer = true
                                    // ✅ FIX: Launch a coroutine to add a delay before moving to the next question.
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(300) // 300ms delay to see the color change
                                        onAnswer("Yes", true)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedAnswer == true) Color(0xFF9092FF) else Color.LightGray,
                                contentColor = if (selectedAnswer == true) Color.White else Color.DarkGray
                            )
                        ) { Text("Yes") }

                        Button(
                            onClick = {
                                if (selectedAnswer == null) { // Prevent clicking again
                                    selectedAnswer = false
                                    // ✅ FIX: Launch a coroutine to add a delay.
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(300)
                                        onAnswer("No", false)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedAnswer == false) Color(0xFF9092FF) else Color.LightGray,
                                contentColor = if (selectedAnswer == false) Color.White else Color.DarkGray
                            )
                        ) { Text("No") }
                    }
                }
                AnswerType.NUMBER -> {
                    var textValue by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { textValue = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type your answer...") },
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Send
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    textValue.toIntOrNull()?.let {
                                        onAnswer(textValue, it)
                                    }
                                },
                                enabled = textValue.isNotBlank()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                            }
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_bubbles")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "a1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "a2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "a3"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha = listOf(alpha1, alpha2, alpha3)[index]
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = alpha))
            )
        }
    }
}