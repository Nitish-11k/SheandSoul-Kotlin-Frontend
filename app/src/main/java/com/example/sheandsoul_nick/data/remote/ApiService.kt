package com.example.sheandsoul_nick.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/signup")
    suspend fun signUpUser(@Body request: SignUpRequest): Response<AuthResponse>

    @POST("api/authenticate")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/verify-email")
    suspend fun verifyOtp(@Body request: OtpVerificationRequest): Response<AuthResponse>

    // Add this function to your ApiService.kt interface
    @POST("api/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): Response<AuthResponse>

    // Add this function to your ApiService.kt interface
    @POST("api/profile") // Adjust the endpoint to match your backend
    suspend fun createProfile(@Body request: CreateProfileRequest): Response<ProfileResponse>

    @POST("api/google") // The endpoint path on your backend
    suspend fun signInWithGoogle(@Body request: GoogleSignInRequest): Response<AuthResponse>

    @GET("api/article/get") // Corrected the endpoint
    suspend fun getArticles(): Response<List<ArticleDto>>

    @GET("api/article/{id}")
    suspend fun getArticleById(@Path("id") articleId: Long): Response<ArticleDto>

}