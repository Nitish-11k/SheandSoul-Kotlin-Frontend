package com.example.sheandsoul_nick.features.auth.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheandsoul_nick.data.remote.ApiService
import com.example.sheandsoul_nick.data.remote.PcosAssessmentDetailsDto
import com.example.sheandsoul_nick.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException


sealed interface AssessmentUiState {
    data class Success(val details: PcosAssessmentDetailsDto) : AssessmentUiState
    object NoAssessment : AssessmentUiState
    object Loading : AssessmentUiState
    data class Error(val message: String) : AssessmentUiState
}
class PcosDashboardViewModel(authViewModel: AuthViewModel) : ViewModel() {

    private val apiService: ApiService = RetrofitClient.getInstance(authViewModel)

    // 👇 REPLACE THE OLD STATE VARIABLES WITH THE NEW ONE
    var assessmentState = mutableStateOf<AssessmentUiState>(AssessmentUiState.Loading)
        private set

    init {
        checkAssessmentStatus()
    }

    // 👇 REWRITE THIS FUNCTION
    private fun checkAssessmentStatus() {
        assessmentState.value = AssessmentUiState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getLatestPcosAssessment()
                if (response.isSuccessful && response.body() != null) {
                    assessmentState.value = AssessmentUiState.Success(response.body()!!)
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    assessmentState.value = AssessmentUiState.NoAssessment
                } else {
                    assessmentState.value = AssessmentUiState.Error(e.message())
                }
            } catch (e: Exception) {
                assessmentState.value = AssessmentUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}