package com.example.sheandsoul_nick.features.auth.presentation

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

// Sealed classes remain the same
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
// ✅ 1. Change to AndroidViewModel to get the application context
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // ✅ 2. Initialize SessionManager and add session state variables
    private val sessionManager = SessionManager(application.applicationContext)
    var isSessionChecked by mutableStateOf(false)
        private set

    private val apiService = RetrofitClient.getInstance(this)

    // --- Properties to hold user data ---
    var email: String = ""
    var userId: Long? = null
    var token: String? = null
    var name: String = ""
    var nickname: String = ""
    var role: Role? = null
    var age: Int = 0
    var height: Float = 0.0f
    var weight: Float = 0.0f
    var period_length: Int = 0
    var cycle_length: Int = 0
    var last_period_start_date: LocalDate = LocalDate.now()
    var last_period_end_date: LocalDate = LocalDate.now()

    // --- LiveData for API call results ---
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

    // ✅ 3. Add an init block to check the session when the app starts
    init {
        checkActiveSession()
    }
    private fun fetchUserProfile(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                if (token == null) {
                    onComplete() // Nothing to fetch
                    return@launch
                }
                val response = apiService.getUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    // Populate ViewModel fields from the response
                    userId = profile.userId
                    email = profile.email
                    name = profile.name ?: ""
                    nickname = profile.nickname ?: ""
                    age = profile.age ?: 0
                    height = profile.height ?: 0.0f
                    weight = profile.weight ?: 0.0f
                    period_length = profile.periodLength ?: 0
                    cycle_length = profile.cycleLength ?: 0
                    if (profile.lastPeriodStartDate != null) {
                        last_period_start_date = LocalDate.parse(profile.lastPeriodStartDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    Log.d("AuthViewModel", "Profile fetched successfully for ${profile.name}")
                } else {
                    // This can happen if the token is invalid/expired
                    Log.w("AuthViewModel", "Failed to fetch profile, logging out.")
                    logout()
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching profile", e)
                logout() // Log out on network error to be safe
            } finally {
                onComplete() // Signal completion in all cases
            }
        }
    }
    private fun checkActiveSession() {
        viewModelScope.launch {
            val savedToken = sessionManager.authTokenFlow.firstOrNull()
            if (!savedToken.isNullOrBlank()) {
                token = savedToken
                // Fetch profile and THEN mark session as checked
                fetchUserProfile {
                    isSessionChecked = true
                }
            } else {
                // No token, so session is checked and user is not logged in
                isSessionChecked = true
            }
        }
    }
    private fun syncFcmToken() {
        // Only sync if we have a user auth token
        if (token == null) return

        viewModelScope.launch {
            val fcmToken = sessionManager.fcmTokenFlow.firstOrNull()
            if (!fcmToken.isNullOrBlank()) {
                try {
                    val request = DeviceTokenRequest(deviceToken = fcmToken)
                    val response = apiService.updateDeviceToken(request)
                    if (response.isSuccessful) {
                        Log.d("FCM_SYNC", "FCM token successfully synced with backend.")
                    } else {
                        Log.e("FCM_SYNC", "Failed to sync FCM token. Response: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("FCM_SYNC", "Error syncing FCM token.", e)
                }
            }
        }
    }

    // ... (updateName, updateNickname, finalizeOnboardingAndSaveData functions are fine) ...
    fun finalizeOnboardingAndSaveData() {
        _profileCreationResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                // ... (The profile creation part remains the same)
                val profileRequest = CreateProfileRequest(
                    name = name,
                    nickname = nickname,
                    role = role?.name ?: "USER",
                    age = age,
                    height = height,
                    weight = weight
                )
                val profileResponse = apiService.createProfile(profileRequest)

                if (!profileResponse.isSuccessful) {
                    val errorMsg = profileResponse.errorBody()?.string() ?: "Profile creation failed"
                    _profileCreationResult.postValue(AuthResult.Error(errorMsg))
                    return@launch
                }

                // --- MODIFICATION: Convert LocalDate to String here ---
                val menstrualRequest = MenstrualData(
                    periodLength = period_length,
                    cycleLength = cycle_length,
                    lastPeriodStartDate = last_period_start_date.toString(), // e.g., "2025-09-22"
                    lastPeriodEndDate = last_period_end_date.toString()      // e.g., "2025-09-26"
                )
                val menstrualResponse = apiService.menstrualData(menstrualRequest)

                if (menstrualResponse.isSuccessful) {
                    _profileCreationResult.postValue(AuthResult.Success("Onboarding complete! Welcome."))
                } else {
                    val errorMsg = menstrualResponse.errorBody()?.string() ?: "Failed to save menstrual data"
                    _profileCreationResult.postValue(AuthResult.Error(errorMsg))
                }

            } catch (e: Exception) {
                _profileCreationResult.postValue(
                    AuthResult.Error(e.message ?: "An unexpected error occurred.")
                )
            }
        }
    }

    fun updateName(newName: String) {
        name = newName
    }

    fun updateNickname(newNickname: String) {
        nickname = newNickname
    }


    // ✅ 4. Update ALL login/signup methods to save the token
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
                    val isNewUser = body.isProfileComplete == false

                    // Save the token
                    token?.let { sessionManager.saveAuthToken(it) }
                    syncFcmToken()

                    _authResult.postValue(AuthResult.SuccessGoogle(body.message ?: "Sign-in successful!", isNewUser))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Google Sign-In failed"
                    _authResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _authResult.postValue(AuthResult.Error(e.message ?: "An error occurred"))
            }
        }
    }

    fun signUpUser(email: String, password: String) {
        this.email = email
        _authResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val request = SignUpRequest(email, password)
                val response = apiService.signUpUser(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    userId = body.userId
                    token = body.token

                    // Save the token
                    token?.let { sessionManager.saveAuthToken(it) }
                    syncFcmToken()

                    _authResult.postValue(AuthResult.Success(body.message ?: "Sign-up Successful"))
                } else {
                    // ... (error handling)
                    val errorMsg = response.errorBody()?.string() ?: "Login failed"
                    _authResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                // ... (exception handling)
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

                    // Save the token
                    token?.let { sessionManager.saveAuthToken(it) }
                    syncFcmToken()

                    _authResult.postValue(AuthResult.Success("Login successful!"))
                } else {
                    // ... (error handling)
                }
            } catch (e: Exception) {
                // ... (exception handling)
            }
        }
    }

    // ... (verifyOtp, resendOtp, forgotPassword, resetPassword, and getNextMenstrualDetails remain the same) ...
    fun verifyOtp(otp: String) {
        if (email.isBlank()) {
            _otpResult.postValue(AuthResult.Error("Email not found."))
            return
        }
        _otpResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val request = OtpVerificationRequest(email, otp)
                val response = apiService.verifyOtp(request)
                if (response.isSuccessful && response.body() != null) {
                    _otpResult.postValue(AuthResult.Success(response.body()!!.message ?: "Email verified Successfully"))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "OTP verification failed"
                    _otpResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _otpResult.postValue(AuthResult.Error(e.message ?: "An unexpected error occurred"))
            }
        }
    }

    fun resendOtp() {
        if (email.isBlank()) {
            _resendResult.postValue(AuthResult.Error("Email not found."))
            return
        }
        _resendResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val request = ResendOtpRequest(email)
                val response = apiService.resendOtp(request)
                if (response.isSuccessful && response.body() != null) {
                    _resendResult.postValue(AuthResult.Success(response.body()!!.message ?: "OTP resent!"))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to resend OTP"
                    _resendResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _resendResult.postValue(AuthResult.Error(e.message ?: "An unexpected error occurred"))
            }
        }
    }

    // --- ADDED BACK: Forgot Password Function ---
    fun forgotPassword(email: String) {
        _forgotPasswordResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful && response.body() != null) {
                    _forgotPasswordResult.postValue(AuthResult.Success(response.body()!!.message ?: "OTP sent."))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to send OTP"
                    _forgotPasswordResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _forgotPasswordResult.postValue(AuthResult.Error(e.message ?: "An error occurred"))
            }
        }
    }

    // --- ADDED BACK: Reset Password Function ---
    fun resetPassword(email: String, otp: String, newPassword: String) {
        _resetPasswordResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val request = ResetPasswordRequest(email, otp, newPassword)
                val response = apiService.resetPassword(request)
                if (response.isSuccessful && response.body() != null) {
                    _resetPasswordResult.postValue(AuthResult.Success(response.body()!!.message ?: "Password reset successfully."))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to reset password"
                    _resetPasswordResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _resetPasswordResult.postValue(AuthResult.Error(e.message ?: "An error occurred"))
            }
        }
    }

    fun getNextMenstrualDetails() {
        _nextMenstrualResult.value = MenstrualResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getNextMenstrualDetails()
                if (response.isSuccessful && response.body() != null) {
                    _nextMenstrualResult.postValue(MenstrualResult.Success(response.body()!!))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to fetch menstrual details"
                    _nextMenstrualResult.postValue(MenstrualResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _nextMenstrualResult.postValue(
                    MenstrualResult.Error(e.message ?: "Unexpected error occurred")
                )
            }
        }
    }
    fun updateMenstrualCycleData() {
        _menstrualUpdateResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val menstrualRequest = MenstrualData(
                    periodLength = period_length,
                    cycleLength = cycle_length,
                    lastPeriodStartDate = last_period_start_date.toString(),
                    lastPeriodEndDate = last_period_end_date.toString()
                )
                val response = apiService.menstrualData(menstrualRequest)
                if (response.isSuccessful) {
                    _menstrualUpdateResult.postValue(AuthResult.Success("Cycle updated successfully!"))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to update cycle data"
                    _menstrualUpdateResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _menstrualUpdateResult.postValue(AuthResult.Error(e.message ?: "An unexpected error occurred."))
            }
        }
    }


    // ✅ 5. Add a public logout function
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearAuthToken()
            token = null
            userId = null
            name = ""
            nickname = ""
            email = ""
            age = 0
        }
    }
}