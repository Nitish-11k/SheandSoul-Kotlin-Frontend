package com.example.sheandsoul_nick.data.remote

import com.google.gson.annotations.SerializedName

// Data to SEND when signing up
data class SignUpRequest(
    val email: String,
    val password: String,
    val deviceToken: String?
)

// Data to SEND when logging in
data class LoginRequest(
    val email: String,
    val password: String
)

// A generic response you might get from the backend
data class AuthResponse(
    @SerializedName("message")
    val message: String?, // ✅ Change this from String to String?

    @SerializedName("user_id")
    val userId: Long?,

    // ... the rest of the fields are already nullable, which is good
    @SerializedName("email")
    val email: String?,

    @SerializedName("access_token")
    val token: String?,

    @SerializedName("token_type")
    val tokenType: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("nickname")
    val nickname: String?,

    @SerializedName("is_profile_complete")
    val isProfileComplete: Boolean?
)

data class OtpVerificationRequest(
    val email: String,
    val otp: String
)
data class NotificationRequest(val token: String, val title: String, val body: String)

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
    val weight: Float,
    val deviceToken: String?
)
data class MenstrualData(
    val periodLength : Int,
    val cycleLength : Int,
    val lastPeriodStartDate : String,
    val lastPeriodEndDate: String
)
data class ProfileResponse(
    val message: String,
    val profileId: String
)

data class GoogleSignInRequest(
    val idToken: String
)

// Request DTO for the new 10-question assessment
data class PCOSAssesmentRequest(
    val cycleLengthDays: Int,
    val missedPeriodsInLastYear: Int,
    val hasSevereAcne: Boolean,
    val hasExcessHairGrowth: Boolean,
    val hasThinningHair: Boolean,
    val hasOvarianCystsConfirmedByUltrasound: Boolean,
    val hasWeightGainOrObesity: Boolean,
    val hasDarkSkinPatches: Boolean,
    val hasFamilyHistoryOfPCOS: Boolean,
    val experiencesHighStress: Boolean,
    val hasMoodSwings: Boolean,
    val hasSleepDisturbances: Boolean,
    val experiencesFatigue: Boolean,
    val hasStrongCravings: Boolean,
    val hasInsulinResistance: Boolean,
    val hasPelvicPain: Boolean,
    val hasFrequentHeadaches: Boolean,
    val hasDifficultyConceiving: Boolean,
    val hasHighBloodPressure: Boolean,
    val hasSkinTags: Boolean
)

// Response DTO from the PCOS assessment endpoint
data class PcosAssessmentResponse(
    @SerializedName("riskLevel")
    val riskLevel: String,
    @SerializedName("message")
    val message: String
)

data class PcosStatusResponse(
    @SerializedName("hasAssessmentData")
    val hasAssessmentData: Boolean
)

data class PcosAssessmentDetailsDto(
    @SerializedName("riskLevel") val riskLevel: String,
    @SerializedName("assessmentDate") val assessmentDate: String,
    @SerializedName("cycleLengthDays") val cycleLengthDays: Int,
    @SerializedName("missedPeriodsInLastYear") val missedPeriodsInLastYear: Int,
    @SerializedName("hasSevereAcne") val hasSevereAcne: Boolean,
    @SerializedName("hasExcessHairGrowth") val hasExcessHairGrowth: Boolean,
    @SerializedName("hasThinningHair") val hasThinningHair: Boolean,
    @SerializedName("hasOvarianCystsConfirmedByUltrasound") val hasOvarianCystsConfirmedByUltrasound: Boolean,
    @SerializedName("hasWeightGainOrObesity") val hasWeightGainOrObesity: Boolean,
    @SerializedName("hasDarkSkinPatches") val hasDarkSkinPatches: Boolean,
    @SerializedName("hasFamilyHistoryOfPCOS") val hasFamilyHistoryOfPCOS: Boolean,
    @SerializedName("experiencesHighStress") val experiencesHighStress: Boolean
)

// ... (keep all existing data classes)

// 👇 ADD THESE NEW DATA CLASSES AT THE END OF THE FILE
data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

data class SimpleMessageResponse(
    @SerializedName("message")
    val message: String?,
    @SerializedName("error")
    val error: String?
)

data class SuccessResponse(
    val message : String
)
data class NextMenstrualResponse(
    val nextPeriodStartDate: String,
    val nextPeriodEndDate: String,
    val nextOvulationDate: String,
    val nextOvulationEndDate: String,
    val nextFertileWindowStartDate: String,
    val nextFertileWindowEndDate: String,
    val nextFollicularStartDate: String,
    val nextFollicularEndDate: String,
    val nextLutealStartDate: String,
    val nextLutealEndDate: String
)
data class UserProfileDto(
    @SerializedName("userId") val userId: Long,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String?,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("userType") val userType: String?,
    @SerializedName("age") val age: Int?,
    @SerializedName("height") val height: Float?,
    @SerializedName("weight") val weight: Float?,
    @SerializedName("periodLength") val periodLength: Int?,
    @SerializedName("cycleLength") val cycleLength: Int?,
    @SerializedName("lastPeriodStartDate") val lastPeriodStartDate: String? // Dates come as Strings
)

data class ChatRequest(val message: String)
data class ChatResponse(val response: String)
// ... at the end of AuthDto.kt

data class UserNoteDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class CreateNoteRequest(val title: String, val content: String)

data class MenstrualCycleLogDto(
    @SerializedName("id") val id: Long,
    @SerializedName("periodLength") val periodLength: Int,
    @SerializedName("cycleLength") val cycleLength: Int,
    @SerializedName("periodStartDate") val periodStartDate: String, // "YYYY-MM-DD"
    @SerializedName("periodEndDate") val periodEndDate: String,   // "YYYY-MM-DD"
    @SerializedName("logDate") val logDate: String              // "YYYY-MM-DD"
)