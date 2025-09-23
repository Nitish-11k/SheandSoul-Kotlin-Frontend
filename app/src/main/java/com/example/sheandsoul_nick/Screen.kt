package com.example.sheandsoul_nick

sealed class Screen(val route: String) {
    object Privacy : Screen("privacy")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Otp : Screen("otp")
    object Name : Screen("name")
    object Nickname : Screen("nickname")
    object RoleSelection : Screen("role_selection")
    object PartnerRole : Screen("partner_role")
    object AgeSelection : Screen("age_selection")
    object HeightSelection : Screen("height_selection")
    object WeightSelection : Screen("weight_selection")
    object Home : Screen("home_screen")
    object ArticleScreen : Screen("articles")
    object ArticleDetail : Screen("article_detail/{articleId}") {
        fun createRoute(articleId: Long) = "article_detail/$articleId"
    }
    object Music : Screen("music")
    object  Community : Screen("community_soon")

    object PcosQuiz : Screen("pcos_quiz")
    object PcosDashboard : Screen("pcos_dashboard")
    // Add a results screen if you want to show the risk level
    object PcosResult : Screen("pcos_result/{riskLevel}") {
        fun createRoute(riskLevel: String) = "pcos_result/$riskLevel"
    }
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{email}") {
        fun createRoute(email: String) = "reset_password/$email"
    }
    object UsualPeriodLengthSelection : Screen("period_selection")
    object UsualCycleLengthSelection : Screen("cycle_selection")
    object LastPeriodDateSelection : Screen("last_period_date_selection")

    object Profile : Screen("profile")
    object EditCycle : Screen("edit_cycle")
}