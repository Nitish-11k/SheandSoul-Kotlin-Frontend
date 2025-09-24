package com.example.sheandsoul_nick

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sheandsoul_nick.features.auth.presentation.*
import com.example.sheandsoul_nick.features.home.HomeScreen
import com.example.sheandsoul_nick.ui.theme.SheAndSoulNickTheme

class MainActivity : ComponentActivity() {

    // Your notification permission logic is correct and remains here
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        setContent {
            val context = LocalContext.current

            SheAndSoulNickTheme {
                val authViewModel: AuthViewModel = viewModel()

                // ✅ 1. Get the session state from the ViewModel
                val isSessionChecked = authViewModel.isSessionChecked
                val isLoggedIn = authViewModel.token != null

                // ✅ 2. Show a splash screen while checking for a saved token
                if (!isSessionChecked) {
                    SplashScreen()
                } else {
                    // ✅ 3. Once checked, determine the correct starting screen
                    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Privacy.route
                    val navController = rememberNavController()
                    var userEmail by remember { mutableStateOf("") }

                    // ✅ 4. Use the dynamic startDestination for the NavHost
                    NavHost(navController = navController, startDestination = startDestination) {

                        // ALL YOUR EXISTING NAVIGATION ROUTES REMAIN THE SAME
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
                                authViewModel = authViewModel,
                                onNavigateToSignup = { navController.navigate(Screen.SignUp.route) },
                                onLoginSuccess = {
                                    navController.navigate(Screen.Home.route) {
                                        // Clear the entire back stack up to the graph's start destination
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                },
                                onForgotPasswordClicked = {
                                    navController.navigate(Screen.ForgotPassword.route)
                                },
                                onGoogleSignInSuccess = { isNewUser ->
                                    // If it's a new user, go to Name screen, otherwise go to Home
                                    val destination = if (isNewUser) Screen.Name.route else Screen.Home.route
                                    navController.navigate(destination) {
                                        // Clear the auth flow from the back stack
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }

                                }
                            )
                        }

                        // ... PASTE ALL OF YOUR OTHER composable() routes here, exactly as they were ...
                        // (I've included them below for completeness)

                        composable(Screen.SignUp.route) {
                            SignUpScreen(
                                authViewModel = authViewModel,
                                onNavigateToLogin = { navController.popBackStack() },
                                onSignUpSuccess = { email ->
                                    userEmail = email
                                    navController.navigate(Screen.Otp.route)
                                },
                                onGoogleSignInSuccess = { isNewUser ->
                                    val destination = if (isNewUser) Screen.Name.route else Screen.Home.route
                                    navController.navigate(destination) {
                                        popUpTo(Screen.SignUp.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.ForgotPassword.route) {
                            ForgotPasswordScreen(
                                onOtpSent = { email ->
                                    navController.navigate(Screen.ResetPassword.createRoute(email))
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.ResetPassword.route,
                            arguments = listOf(navArgument("email") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            ResetPasswordScreen(
                                email = email,
                                onPasswordResetSuccess = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Otp.route) {
                            OtpScreen(
                                authViewModel = authViewModel,
                                onVerificationSuccess = {
                                    navController.navigate(Screen.Name.route)
                                }
                            )
                        }

                        composable(Screen.Name.route) {
                            NameScreen(
                                authViewModel = authViewModel,
                                onContinueClicked = {
                                    navController.navigate(Screen.Nickname.route)
                                }
                            )
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
                                        navController.navigate(Screen.PartnerRole.route)
                                    }
                                }
                            )
                        }
                        composable(Screen.PartnerRole.route) {
                            PartnerComingSoonScreen(onGoBack = { navController.popBackStack() })
                        }
                        composable(Screen.AgeSelection.route) {
                            AgeSelectionScreen(onContinueClicked = {age->
                                navController.navigate(Screen.HeightSelection.route)
                            },
                                authViewModel = authViewModel
                            )
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
                                    navController.navigate(Screen.UsualPeriodLengthSelection.route)
                                }
                            )
                        }

                        composable(Screen.UsualPeriodLengthSelection.route) {
                            UsualPeriodLengthScreen(
                                authViewModel = authViewModel,
                                onContinueClicked = {
                                    navController.navigate(Screen.UsualCycleLengthSelection.route)
                                }
                            )
                        }

                        composable(Screen.UsualCycleLengthSelection.route){
                            UsualCycleLengthScreen(
                                onContinueClicked = {
                                    navController.navigate(Screen.LastPeriodDateSelection.route)
                                },
                                authViewModel = authViewModel
                            )
                        }

                        composable(Screen.LastPeriodDateSelection.route) {
                            LastPeriodDateScreen(
                                authViewModel = authViewModel,
                                onFinish = {
                                    navController.navigate(Screen.Home.route) {
                                        // Clear the whole auth flow from the back stack
                                        popUpTo(Screen.Privacy.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(
                                authViewModel = authViewModel,
                                onArticleClicked = { articleId ->
                                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                                },
                                onNavigateToArticles = { navController.navigate(Screen.ArticleScreen.route) },
                                onNavigateToCommunity = {
                                    navController.navigate(Screen.Community.route)
                                },
                                onNavigateToProfile = {
                                    navController.navigate(Screen.Music.route)
                                },
                                onNavigateToPartner = {
                                    navController.navigate(Screen.PartnerRole.route)
                                },
                                onProfileClick = {
                                    navController.navigate(Screen.Profile.route)
                                    Toast.makeText(context, "Profile Clicked!", Toast.LENGTH_SHORT)
                                        .show()

                                },
                                onNavigateToPcosQuiz = {
                                    navController.navigate(Screen.PcosQuiz.route)
                                },
                                onNavigateToPcosDashboard = { navController.navigate(Screen.PcosDashboard.route) },
                                onNavigateToEditCycle = {
                                    navController.navigate(Screen.EditCycle.route)
                                }
                            )
                        }

                        composable(Screen.ArticleScreen.route) {
                            ArticlesScreen(
                                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                                onNavigateToCommunity = {navController.navigate(Screen.Community.route)},
                                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                                onArticleClicked = { articleId: Long ->
                                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                                },
                                onNavigateToMusic = {
                                    navController.navigate(Screen.Music.route)
                                },
                                authViewModel = authViewModel
                            )
                        }

                        composable(Screen.EditCycle.route) {
                            EditCycleDetailsScreen(
                                authViewModel = authViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("music") {
                            MusicScreen(
                                authViewModel = authViewModel,
                                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                                onNavigateToArticles = { navController.navigate(Screen.ArticleScreen.route) },
                                onNavigateToCommunity = { navController.navigate(Screen.Community.route) },
                                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
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
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(Screen.Community.route){
                            CommunityComingSoonScreen(
                                onNavigateToHome = {
                                    navController.navigate(Screen.Home.route)
                                },
                                onNavigateToMusic = {
                                    navController.navigate(Screen.Music.route)
                                },
                                onNavigateToArticles = {
                                    navController.navigate(Screen.ArticleScreen.route)
                                },
                                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                            )
                        }
                        composable(Screen.PcosQuiz.route) {
                            PcosQuizScreen(
                                authViewModel = authViewModel,
                                onAssessmentComplete = { riskLevel ->
                                    Toast.makeText(context, "Your Assessed Risk Level: $riskLevel", Toast.LENGTH_LONG).show()
                                    navController.navigate(Screen.PcosDashboard.route) {
                                        popUpTo(Screen.PcosQuiz.route) { inclusive = true }
                                    }
                                },
                                onNavigateBack = {navController.popBackStack()}
                            )
                        }
                        composable(Screen.PcosDashboard.route) {
                            PcosDashboardScreen(
                                authViewModel = authViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onStartAssessment = {
                                    navController.navigate(Screen.PcosQuiz.route)
                                }
                            )
                        }
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                authViewModel = authViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToLogin = {
                                    navController.navigate(Screen.Login.route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


// ✅ 5. Add this composable for your loading screen
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // You can replace this with your app's logo
        CircularProgressIndicator()
    }
}