package com.example.sheandsoul_nick.features.auth.presentation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheandsoul_nick.data.remote.AuthResponse
import com.example.sheandsoul_nick.data.remote.CreateProfileRequest
import com.example.sheandsoul_nick.data.remote.GoogleSignInRequest
import com.example.sheandsoul_nick.data.remote.LoginRequest
import com.example.sheandsoul_nick.data.remote.MenstrualData
import com.example.sheandsoul_nick.data.remote.NextMenstrualResponse
import com.example.sheandsoul_nick.data.remote.OtpVerificationRequest
import com.example.sheandsoul_nick.data.remote.ResendOtpRequest
import com.example.sheandsoul_nick.data.remote.RetrofitClient
import com.example.sheandsoul_nick.data.remote.SignUpRequest
import com.example.sheandsoul_nick.data.remote.SuccessResponse
import kotlinx.coroutines.launch
import java.time.LocalDate

// Represents the result of an API call
sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val errorMessage: String) : AuthResult()
    data class SuccessGoogle(val message: String) : AuthResult()
    object Loading : AuthResult()
}

sealed class MenstrualResult {
    data class Success(val data: NextMenstrualResponse) : MenstrualResult()
    data class Error(val errorMessage: String) : MenstrualResult()
    object Loading : MenstrualResult()
}


@RequiresApi(Build.VERSION_CODES.O)
class AuthViewModel : ViewModel() {

    // --- Get the ApiService instance with the interceptor ---
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
    var periodLength: Int = 0  // 1 to 7
    var cycleLength: Int = 0 // 1 to 28
    var lastPeriodStartDate: LocalDate = LocalDate.now()
    var lastPeriodEndDate: LocalDate = LocalDate.now()


    // --- LiveData for API call results ---
    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    private val _otpResult = MutableLiveData<AuthResult>()
    val otpResult: LiveData<AuthResult> = _otpResult

    private val _resendResult = MutableLiveData<AuthResult>()
    val resendResult: LiveData<AuthResult> = _resendResult

    private val _profileCreationResult = MutableLiveData<AuthResult>()
    val profileCreationResult: LiveData<AuthResult> = _profileCreationResult

    private val _menstrualDataResult = MutableLiveData<AuthResult>()
    val menstrualDataResult: LiveData<AuthResult> = _menstrualDataResult

    private val _nextMenstrualResult = MutableLiveData<MenstrualResult>()
    val nextMenstrualResult: LiveData<MenstrualResult> = _nextMenstrualResult


    // --- All API Functions MUST be inside the class ---

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
                    _authResult.postValue(AuthResult.SuccessGoogle(body.message))
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
                    _authResult.postValue(AuthResult.Success(body.message))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Sign-up failed"
                    _authResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _authResult.postValue(
                    AuthResult.Error(
                        e.message ?: "An unexpected error occurred"
                    )
                )
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

                    // ▼▼▼ ADD THESE TWO LINES TO SAVE THE USER'S NAME ▼▼▼
                    name = body.name ?: "" // Use the name from the response
                    nickname = body.nickname ?: "" // Also save the nickname

                    _authResult.postValue(AuthResult.Success("Login successful!"))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Login failed"
                    _authResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _authResult.postValue(
                    AuthResult.Error(
                        e.message ?: "An unexpected error occurred"
                    )
                )
            }
        }
    }

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
                    _otpResult.postValue(AuthResult.Success(response.body()!!.message))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "OTP verification failed"
                    _otpResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _otpResult.postValue(
                    AuthResult.Error(
                        e.message ?: "An unexpected error occurred"
                    )
                )
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
                    _resendResult.postValue(AuthResult.Success(response.body()!!.message))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to resend OTP"
                    _resendResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _resendResult.postValue(
                    AuthResult.Error(
                        e.message ?: "An unexpected error occurred"
                    )
                )
            }
        }
    }

    fun createFullProfile() {
        _profileCreationResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val request = CreateProfileRequest(
                    name = name,
                    nickname = nickname,
                    role = role?.name ?: "USER",
                    age = age,
                    height = height,
                    weight = weight,
                )

                val response = apiService.createProfile(request)
                if (response.isSuccessful) {
                    _profileCreationResult.postValue(AuthResult.Success("Profile created successfully!"))
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Profile creation failed"
                    _profileCreationResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _profileCreationResult.postValue(
                    AuthResult.Error(
                        e.message ?: "An unexpected error occurred"
                    )
                )
            }
        }
    }

    fun createMenstrualData() {
        _menstrualDataResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val request = MenstrualData(
                    periodLength = periodLength,
                    cycleLength = cycleLength,
                    lastPeriodStartDate = lastPeriodStartDate.toString(),
                    lastPeriodEndDate = lastPeriodEndDate.toString()
                )

                val response = apiService.menstrualData(request)
                Log.d("She&Soul", "$response $token")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d("She&Soul", "$response $token")
                    _menstrualDataResult.postValue(AuthResult.Success(body.message))
                } else {
                    Log.d("She&Soul", "$response $token")
                    val errorMsg = response.errorBody()?.string() ?: "Menstrual data failed"
                    _menstrualDataResult.postValue(AuthResult.Error(errorMsg))
                }
            } catch (e: Exception) {
                _menstrualDataResult.postValue(
                    AuthResult.Error(e.message ?: "An unexpected error occurred")
                )
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


}
