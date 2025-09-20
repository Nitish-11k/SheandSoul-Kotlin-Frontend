package com.example.sheandsoul_nick.data.remote

// Data to SEND when signing up
data class SignUpRequest(
    val email: String,
    val password: String
)

// Data to SEND when logging in
data class LoginRequest(
    val email: String,
    val password: String
)

// A generic response you might get from the backend
data class AuthResponse(
    val token: String?,
    val userId: Long?,
    val message: String
)

data class OtpVerificationRequest(
    val email: String,
    val otp: String
)

data class ResendOtpRequest(
    val email : String
)

data class CreateProfileRequest(
//    val userId: Long,
    val name: String,
    val nickname: String,
    val role: String,
    val age: Int,
    val height : Float,
    val weight: Float
)
data class ProfileResponse(
    val message: String,
    val profileId: String
)

data class GoogleSignInRequest(
    val idToken: String
)