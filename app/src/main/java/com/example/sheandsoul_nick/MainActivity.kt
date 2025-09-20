package com.example.sheandsoul_nick

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sheandsoul_nick.features.articles.ArticleDetailScreen
import com.example.sheandsoul_nick.features.auth.presentation.ArticlesScreen
import com.example.sheandsoul_nick.features.auth.presentation.*
import com.example.sheandsoul_nick.features.home.HomeScreen
import com.example.sheandsoul_nick.ui.theme.SheAndSoulNickTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            SheAndSoulNickTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                // 1. State to hold the email during the signup flow
                var userEmail by remember { mutableStateOf("") }

                NavHost(navController = navController, startDestination = Screen.Privacy.route) {

                    composable(Screen.Privacy.route) {
                        PrivacyScreen(
                            onAgree = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Privacy.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Login.route) {
                        LoginScreen(
                            onNavigateToSignup = { navController.navigate(Screen.SignUp.route) },
                            onLoginSuccess = {
                                // 2. Correct back stack clearing for login
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Privacy.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.SignUp.route) {
                        SignUpScreen(
                            authViewModel = authViewModel,
                            onNavigateToLogin = { navController.popBackStack() },
                            // 3. Capture the email and navigate
                            onSignUpSuccess = { email ->
                                userEmail = email
                                navController.navigate(Screen.Otp.route)
                            },
                            onGoogleSignInSuccess = {
                                navController.navigate(Screen.Name.route) // Google signup skips OTP
                            }
                        )
                    }
                    composable(Screen.Otp.route) {
                        OtpScreen(
                            // 4. Pass the captured email
                            authViewModel = authViewModel,
                            onVerificationSuccess = {
                                navController.navigate(Screen.Name.route)
                            }
                            // onResendClicked is now handled by the ViewModel
                        )
                    }

                    composable(Screen.Name.route) {
                        NameScreen(
                            authViewModel = authViewModel,
                            onContinueClicked = {
                                navController.navigate(Screen.Nickname.route)
                            })
                    }

                    composable(Screen.Nickname.route) {
                        NicknameScreen(
                            authViewModel = authViewModel,
                            onContinueClicked = { navController.navigate(Screen.RoleSelection.route) },
                            onSkipClicked = { navController.navigate(Screen.RoleSelection.route) }
                        )
                    }

                    composable(Screen.RoleSelection.route) {
                        RoleSelectionScreen(
                            authViewModel = authViewModel,
                            onContinueClicked = { selectedRole ->
                                if (selectedRole == Role.USER) {
                                    navController.navigate(Screen.AgeSelection.route)
                                } else {
                                    navController.navigate(Screen.PartnerRole.route) // 5. Corrected route
                                }
                            }
                        )
                    }
                    composable(Screen.PartnerRole.route) { // 5. Corrected route
                        PartnerComingSoonScreen(onGoBack = { navController.popBackStack() })
                    }
                    composable(Screen.AgeSelection.route) {
                        AgeSelectionScreen(onContinueClicked = { navController.navigate(Screen.HeightSelection.route) })
                    }

                    composable(Screen.HeightSelection.route) {
                        HeightSelectionScreen(
                            authViewModel = authViewModel,
                            onContinueClicked = { _, _ ->
                                navController.navigate(Screen.WeightSelection.route)
                            })
                    }

                    composable(Screen.WeightSelection.route) {
                        WeightSelectionScreen(
                            authViewModel = authViewModel,
                            onContinueClicked = {
                                // 6. Correct back stack clearing for signup flow
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.SignUp.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Home.route) { // 5. Corrected route
                        HomeScreen(
                            username = authViewModel.name,
                            onNavigateToArticles = { navController.navigate(Screen.ArticleScreen.route) },
                            onNavigateToCommunity = { /* TODO */
                            },
                            onNavigateToProfile = { /* TODO */
                            },
                            onNavigateToPartner = {
                                navController.navigate(Screen.PartnerRole.route) },
                            onProfileClick = {
                                Toast.makeText(context, "Profile Clicked!", Toast.LENGTH_SHORT).show()
                            },
                            onNotificationClick = {
                                Toast.makeText(context, "Notification Clicked!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    composable(Screen.ArticleScreen.route) {
                        ArticlesScreen(
                            onNavigateToHome = { navController.popBackStack() },
                            onNavigateToCommunity = { /* TODO */ },
                            onNavigateToProfile = { /* TODO */ },
                            // This is the line we fixed by adding ": Long"
                            onArticleClicked = { articleId: Long ->
                                navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                            },
                            onNavigateToMusic = {},
                            authViewModel = authViewModel
                        )
                    }
                    composable(
                        route = Screen.ArticleDetail.route,
                        arguments = listOf(navArgument("articleId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val articleId = backStackEntry.arguments?.getLong("articleId")
                        if (articleId != null) {
                            ArticleDetailScreen(
                                articleId = articleId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}