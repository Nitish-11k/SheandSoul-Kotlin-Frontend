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
    object UsualPeriodLengthSelection : Screen("period_selection")
    object UsualCycleLengthSelection : Screen("cycle_selection")
    object LastPeriodDateSelection : Screen("last_period_date_selection")
    object Home : Screen("home_screen")
    object ArticleScreen : Screen("articles")
    object ArticleDetail : Screen("article_detail/{articleId}") {
        fun createRoute(articleId: Long) = "article_detail/$articleId"
    }

}