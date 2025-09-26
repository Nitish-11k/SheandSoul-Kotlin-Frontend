package com.example.sheandsoul_nick.features.auth.presentation

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sheandsoul_nick.data.SessionManager
import com.example.sheandsoul_nick.data.remote.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val errorMessage: String) : AuthResult()
    data class SuccessGoogle(val message: String, val isNewUser: Boolean) : AuthResult()
    object Loading : AuthResult()
}

sealed class MenstrualResult {
    data class Success(val data: NextMenstrualResponse) : MenstrualResult()
    data class Error(val errorMessage: String) : MenstrualResult()
    object Loading : MenstrualResult()
}


@RequiresApi(Build.VERSION_CODES.O)
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application.applicationContext)
    var isSessionChecked by mutableStateOf(false)
        private set

    private val _isProfileComplete = mutableStateOf<Boolean?>(null)
    val isProfileComplete: State<Boolean?> = _isProfileComplete

    private val apiService = RetrofitClient.getInstance(this)

    var email: String = ""
    var userId: Long? = null
    var token: String? = null
    var name: String by mutableStateOf("")
    var nickname: String by mutableStateOf("")
    var role: Role? = null
    var age: Int by mutableStateOf(0)
    var height: Float by mutableStateOf(0.0f)
    var weight: Float by mutableStateOf(0.0f)
    var period_length: Int by mutableStateOf(0)
    var cycle_length: Int by mutableStateOf(0)
    var last_period_start_date: LocalDate = LocalDate.now()
    var last_period_end_date: LocalDate = LocalDate.now()

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult
    private val _otpResult = MutableLiveData<AuthResult>()
    val otpResult: LiveData<AuthResult> = _otpResult
    private val _resendResult = MutableLiveData<AuthResult>()
    val resendResult: LiveData<AuthResult> = _resendResult
    private val _profileCreationResult = MutableLiveData<AuthResult>()
    val profileCreationResult: LiveData<AuthResult> = _profileCreationResult
    private val _resetPasswordResult = MutableLiveData<AuthResult>()
    val resetPasswordResult: LiveData<AuthResult> = _resetPasswordResult
    private val _forgotPasswordResult = MutableLiveData<AuthResult>()
    val forgotPasswordResult: LiveData<AuthResult> = _forgotPasswordResult
    private val _nextMenstrualResult = MutableLiveData<MenstrualResult>()
    val nextMenstrualResult: LiveData<MenstrualResult> = _nextMenstrualResult
    private val _menstrualUpdateResult = MutableLiveData<AuthResult>()
    val menstrualUpdateResult: LiveData<AuthResult> = _menstrualUpdateResult

    init {
        checkActiveSession()
    }

    // ✅ FIX: Added the missing loadUserProfile function
    // This function will be called from your ProfileScreen.
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                // IMPORTANT: You need an API endpoint that returns all user details.
                // I am assuming it's called `getUserProfile()` here.
                val response = apiService.getUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    val userProfile = response.body()!!
                    // Update all the ViewModel properties with the fetched data
                    name = userProfile.name ?: ""
                    age = userProfile.age ?: 0
                    height = userProfile.height ?: 0.0f
                    weight = userProfile.weight ?: 0.0f
                    period_length = userProfile.periodLength ?: 0
                    cycle_length = userProfile.cycleLength ?: 0
                } else {
                    Log.e("AuthViewModel", "Failed to load user profile.")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading user profile", e)
            }
        }
    }


    private fun fetchProfileStatus(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.getProfileStatus()
                if (response.isSuccessful && response.body() != null) {
                    val status = response.body()!!
                    _isProfileComplete.value = status.isProfileComplete ?: false
                    name = status.name ?: ""
                    nickname = status.nickname ?: ""
                    Log.d("AuthViewModel", "Profile status fetched. Is complete? ${_isProfileComplete.value}")
                } else {
                    Log.w("AuthViewModel", "Failed to get profile status for token. Logging out.")
                    logout()
                    _isProfileComplete.value = false
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching profile status", e)
                logout()
                _isProfileComplete.value = false
            } finally {
                onComplete()
            }
        }
    }

    private fun checkActiveSession() {
        viewModelScope.launch {
            val savedToken = sessionManager.authTokenFlow.firstOrNull()
            if (!savedToken.isNullOrBlank()) {
                token = savedToken
                fetchProfileStatus {
                    isSessionChecked = true
                }
            } else {
                token = null
                _isProfileComplete.value = false
                isSessionChecked = true
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val request = GoogleSignInRequest(idToken)
                val response = apiService.signInWithGoogle(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    userId = body.userId
                    token = body.token
                    name = body.name ?: ""
                    nickname = body.nickname ?: ""
                    _isProfileComplete.value = body.isProfileComplete ?: false
                    token?.let { sessionManager.saveAuthToken(it) }
                    syncFcmToken()
                    _authResult.postValue(AuthResult.SuccessGoogle(body.message ?: "Sign-in successful!", body.isProfileComplete == false))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Google Sign-In failed"
                    _authResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _authResult.postValue(AuthResult.Error(e.message ?: "An error occurred"))
            }
        }
    }

    fun loginUser(email: String, password: String) {
        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = apiService.loginUser(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    userId = body.userId
                    token = body.token
                    name = body.name ?: ""
                    nickname = body.nickname ?: ""
                    _isProfileComplete.value = body.isProfileComplete ?: false
                    token?.let { sessionManager.saveAuthToken(it) }
                    syncFcmToken()
                    _authResult.postValue(AuthResult.Success("Login successful!"))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Login failed"
                    _authResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _authResult.postValue(AuthResult.Error(e.message ?: "An error occurred"))
            }
        }
    }

    // ... other functions like signUpUser, finalizeOnboardingAndSaveData, logout, etc. remain the same
    fun signUpUser(email: String, password: String) {
        this.email = email
        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {val fcmToken = sessionManager.fcmTokenFlow.firstOrNull()
                Log.d("AuthViewModel", "FCM token for signup: $fcmToken")

                // 2. Create the request object, now including the token
                val request = SignUpRequest(email, password, fcmToken)
                val response = apiService.signUpUser(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    userId = body.userId
                    token = body.token
                    _isProfileComplete.value = body.isProfileComplete ?: false
                    token?.let { sessionManager.saveAuthToken(it) }
                    syncFcmToken()
                    _authResult.postValue(AuthResult.Success(body.message ?: "Sign-up Successful"))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Sign-up failed"
                    _authResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _authResult.postValue(AuthResult.Error(e.message ?: "An error occurred"))
            }
        }
    }

    fun finalizeOnboardingAndSaveData() {
        _profileCreationResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val fcmToken = sessionManager.fcmTokenFlow.firstOrNull()

                val profileRequest = CreateProfileRequest(
                    name = name,
                    nickname = nickname,
                    role = role?.name ?: "USER",
                    age = age,
                    height = height,
                    weight = weight,
                    deviceToken = fcmToken
                )
                val profileResponse = apiService.createProfile(profileRequest)

                if (!profileResponse.isSuccessful) {
                    val errorMsg = profileResponse.errorBody()?.string() ?: "Profile creation failed"
                    _profileCreationResult.postValue(AuthResult.Error(errorMsg))
                    return@launch
                }

                val menstrualRequest = MenstrualData(
                    periodLength = period_length,
                    cycleLength = cycle_length,
                    lastPeriodStartDate = last_period_start_date.toString(),
                    lastPeriodEndDate = last_period_end_date.toString()
                )
                val menstrualResponse = apiService.menstrualData(menstrualRequest)

                if (menstrualResponse.isSuccessful) {
                    _profileCreationResult.postValue(AuthResult.Success("Onboarding complete! Welcome."))
                    _isProfileComplete.value = true
                } else {
                    val errorMsg = menstrualResponse.errorBody()?.string() ?: "Failed to save menstrual data"
                    _profileCreationResult.postValue(AuthResult.Error(errorMsg))
                }

            } catch (e: Exception) {
                _profileCreationResult.postValue(AuthResult.Error(e.message ?: "An unexpected error occurred."))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearAuthToken()
            token = null
            userId = null
            name = ""
            nickname = ""
            email = ""
            age = 0
            _isProfileComplete.value = false
        }
    }

    private fun syncFcmToken() {
        if (token == null) return
        viewModelScope.launch {
            val fcmToken = sessionManager.fcmTokenFlow.firstOrNull()
            if (!fcmToken.isNullOrBlank()) {
                try {
                    val request = DeviceTokenRequest(deviceToken = fcmToken)
                    apiService.updateDeviceToken(request)
                } catch (e: Exception) {
                    Log.e("FCM_SYNC", "Error syncing FCM token.", e)
                }
            }
        }
    }

    fun updateName(newName: String) { name = newName }
    fun updateNickname(newNickname: String) { nickname = newNickname }

    fun verifyOtp(otp: String) {
        if (email.isBlank()) { _otpResult.postValue(AuthResult.Error("Email not found.")); return }
        _otpResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.verifyOtp(OtpVerificationRequest(email, otp))
                if (response.isSuccessful) _otpResult.postValue(AuthResult.Success(response.body()?.message ?: "Email verified!"))
                else _otpResult.postValue(AuthResult.Error(response.errorBody()?.string() ?: "OTP verification failed"))
            } catch (e: Exception) { _otpResult.postValue(AuthResult.Error(e.message ?: "An error occurred")) }
        }
    }

    fun resendOtp() {
        if (email.isBlank()) { _resendResult.postValue(AuthResult.Error("Email not found.")); return }
        _resendResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.resendOtp(ResendOtpRequest(email))
                if (response.isSuccessful) _resendResult.postValue(AuthResult.Success(response.body()?.message ?: "OTP resent!"))
                else _resendResult.postValue(AuthResult.Error(response.errorBody()?.string() ?: "Failed to resend OTP"))
            } catch (e: Exception) { _resendResult.postValue(AuthResult.Error(e.message ?: "An error occurred")) }
        }
    }

    fun forgotPassword(email: String) {
        _forgotPasswordResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) _forgotPasswordResult.postValue(AuthResult.Success(response.body()?.message ?: "OTP sent."))
                else _forgotPasswordResult.postValue(AuthResult.Error(response.errorBody()?.string() ?: "Failed to send OTP"))
            } catch (e: Exception) { _forgotPasswordResult.postValue(AuthResult.Error(e.message ?: "An error occurred")) }
        }
    }

    fun resetPassword(email: String, otp: String, newPassword: String) {
        _resetPasswordResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.resetPassword(ResetPasswordRequest(email, otp, newPassword))
                if (response.isSuccessful) _resetPasswordResult.postValue(AuthResult.Success(response.body()!!.message ?: "Password reset successfully."))
                else _resetPasswordResult.postValue(AuthResult.Error(response.errorBody()?.string() ?: "Failed to reset password"))
            } catch (e: Exception) { _resetPasswordResult.postValue(AuthResult.Error(e.message ?: "An error occurred")) }
        }
    }

    fun getNextMenstrualDetails() {
        _nextMenstrualResult.value = MenstrualResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getNextMenstrualDetails()
                if (response.isSuccessful) _nextMenstrualResult.postValue(MenstrualResult.Success(response.body()!!))
                else _nextMenstrualResult.postValue(MenstrualResult.Error(response.errorBody()?.string() ?: "Failed to fetch details"))
            } catch (e: Exception) { _nextMenstrualResult.postValue(MenstrualResult.Error(e.message ?: "An error occurred")) }
        }
    }
    fun updateMenstrualCycleData() {
        _menstrualUpdateResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val request = MenstrualData(period_length, cycle_length, last_period_start_date.toString(), last_period_end_date.toString())
                val response = apiService.menstrualData(request)
                if (response.isSuccessful) _menstrualUpdateResult.postValue(AuthResult.Success("Cycle updated!"))
                else _menstrualUpdateResult.postValue(AuthResult.Error(response.errorBody()?.string() ?: "Failed to update data"))
            } catch (e: Exception) { _menstrualUpdateResult.postValue(AuthResult.Error(e.message ?: "An error occurred")) }
        }
    }
}