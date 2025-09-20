package com.example.sheandsoul_nick.data.remote

import com.google.gson.annotations.SerializedName

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
    @SerializedName("message")
    val message: String,

    @SerializedName("user_id")
    val userId: Long?,

    @SerializedName("email")
    val email: String?,

    // The key change: map "access_token" from the backend to the "token" variable
    @SerializedName("access_token")
    val token: String?,

    @SerializedName("token_type")
    val tokenType: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("nickname")
    val nickname: String?
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
    @SerializedName("userType")
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