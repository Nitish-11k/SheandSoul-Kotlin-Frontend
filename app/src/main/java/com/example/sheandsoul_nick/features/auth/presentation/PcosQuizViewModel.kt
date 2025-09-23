package com.example.sheandsoul_nick.features.auth.presentation

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheandsoul_nick.data.remote.ApiService
import com.example.sheandsoul_nick.data.remote.PCOSAssesmentRequest
import com.example.sheandsoul_nick.data.remote.PcosAssessmentResponse
import com.example.sheandsoul_nick.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// Represents the state of the API submission
sealed class QuizResult {
    data class Success(val response: PcosAssessmentResponse) : QuizResult()
    data class Error(val message: String) : QuizResult()
    object Loading : QuizResult()
}

// Data class to define a question in our quiz
data class Question(
    val id: Int,
    val text: String,
    val answerType: AnswerType
)

enum class AnswerType { NUMBER, YES_NO }

class PcosQuizViewModel(authViewModel: AuthViewModel) : ViewModel() {

    private val apiService: ApiService = RetrofitClient.getInstance(authViewModel)

    // A flow to emit one-time events to the UI, like navigation or toasts
    private val _quizResult = MutableSharedFlow<QuizResult>()
    val quizResult = _quizResult.asSharedFlow()

    // The list of 10 questions for our quiz
    val questions = listOf(
        Question(1, "On average, how many days are there between your periods?", AnswerType.NUMBER),
        Question(2, "In the last 12 months, how many periods have you missed entirely?", AnswerType.NUMBER),
        Question(3, "Do you experience severe or persistent acne?", AnswerType.YES_NO),
        Question(4, "Do you have unwanted, coarse, dark hair growth on your face, chest, or back?", AnswerType.YES_NO),
        Question(5, "Are you experiencing thinning hair or hair loss from your scalp?", AnswerType.YES_NO),
        Question(6, "Have you ever been told by a doctor that you have polycystic ovaries after an ultrasound?", AnswerType.YES_NO),
        Question(7, "Do you struggle with weight gain or find it very difficult to lose weight?", AnswerType.YES_NO),
        Question(8, "Have you noticed any dark, velvety patches of skin, especially on your neck or underarms?", AnswerType.YES_NO),
        Question(9, "Does your mother or sister have a PCOS diagnosis?", AnswerType.YES_NO),
        Question(10, "Would you describe your daily stress levels as consistently high?", AnswerType.YES_NO)
    )

    // A map to store the user's answers, linking question ID to the answer
    val answers = mutableStateMapOf<Int, Any>()

    fun submitAssessment() {
        viewModelScope.launch {
            _quizResult.emit(QuizResult.Loading)
            try {
                // Build the request DTO from the stored answers
                val request = PCOSAssesmentRequest(
                    cycleLengthDays = answers[1] as? Int ?: 0,
                    missedPeriodsInLastYear = answers[2] as? Int ?: 0,
                    hasSevereAcne = answers[3] as? Boolean ?: false,
                    hasExcessHairGrowth = answers[4] as? Boolean ?: false,
                    hasThinningHair = answers[5] as? Boolean ?: false,
                    hasOvarianCystsConfirmedByUltrasound = answers[6] as? Boolean ?: false,
                    hasWeightGainOrObesity = answers[7] as? Boolean ?: false,
                    hasDarkSkinPatches = answers[8] as? Boolean ?: false,
                    hasFamilyHistoryOfPCOS = answers[9] as? Boolean ?: false,
                    experiencesHighStress = answers[10] as? Boolean ?: false
                )

                val response = apiService.submitPcosAssessment(request)

                if (response.isSuccessful && response.body() != null) {
                    _quizResult.emit(QuizResult.Success(response.body()!!))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("PcosQuizVM", "API Error: $errorMsg")
                    _quizResult.emit(QuizResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("PcosQuizVM", "Exception: ${e.message}", e)
                _quizResult.emit(QuizResult.Error(e.message ?: "An unexpected error occurred"))
            }
        }
    }
}