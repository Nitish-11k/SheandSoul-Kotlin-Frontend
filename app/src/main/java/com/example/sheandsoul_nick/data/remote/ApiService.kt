package com.example.sheandsoul_nick.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class DeviceTokenRequest(val deviceToken: String)
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

    @GET("api/profile/status")
    suspend fun getProfileStatus(): Response<AuthResponse>

    @GET("api/article/{id}")
    suspend fun getArticleById(@Path("id") articleId: Long): Response<ArticleDto>

    @GET("api/v1/music/get") // Endpoint from the backend controller
    suspend fun getMusic(): Response<List<MusicDto>>

    @POST("api/pcos/assess")
    suspend fun submitPcosAssessment(@Body request: PCOSAssesmentRequest): Response<PcosAssessmentResponse>

    @GET("api/pcos/assessment/status")
    suspend fun getPcosAssessmentStatus(): Response<PcosStatusResponse>

    @GET("api/pcos/assessment/latest")
    suspend fun getLatestPcosAssessment(): Response<PcosAssessmentDetailsDto>

    @POST("api/password/forgot")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<SimpleMessageResponse>

    @POST("api/password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<SimpleMessageResponse>

    @PUT("/api/menstrual-data")
    suspend fun menstrualData(@Body request: MenstrualData): Response<MenstrualData>

    @GET("/api/next-period")
    suspend fun getNextMenstrualDetails(): Response<NextMenstrualResponse>

    @POST("api/notification/send")
    suspend fun sendTestNotification(@Body request: NotificationRequest): Response<Unit>

    @PUT("api/profile/device-token")
    suspend fun updateDeviceToken(@Body request: DeviceTokenRequest): Response<Unit> // The response body is not important
    @GET("api/profile/me")
    suspend fun getUserProfile(): Response<UserProfileDto>
}
