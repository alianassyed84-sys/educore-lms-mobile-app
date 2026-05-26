package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.MainViewModel
import com.example.ui.admin.AdminMainScreen
import com.example.ui.auth.*
import com.example.ui.instructor.InstructorMainScreen
import com.example.ui.learner.CourseDetailScreen
import com.example.ui.learner.CoursePlayerScreen
import com.example.ui.learner.LearnerMainScreen
import com.example.ui.learner.PremiumUpgradeScreen
import com.example.ui.theme.EduCoreTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EduCoreTheme {
                EduCoreNavigationGraph(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EduCoreNavigationGraph(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize()
    ) {
        // ━━━━━━━ SPLASH SCREEN ━━━━━━━
        composable("splash") {
            SplashScreen(
                viewModel = viewModel,
                onNavigateNext = {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // ━━━━━━━ ONBOARDING SCREEN ━━━━━━━
        composable("onboarding") {
            OnboardingScreen(
                onFinished = {
                    navController.navigate("role_selection") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // ━━━━━━━ ROLE SELECTION SCREEN ━━━━━━━
        composable("role_selection") {
            RoleSelectionScreen(
                viewModel = viewModel,
                onRoleSelected = {
                    navController.navigate("login")
                }
            )
        }

        // ━━━━━━━ LOGIN SCREEN ━━━━━━━
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToSignup = {
                    navController.navigate("signup")
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                },
                onOtpRequired = {
                    navController.navigate("email_verification")
                },
                onLoginSuccess = {
                    val role = viewModel.selectedRole.value
                    val dest = when (role) {
                        "Admin" -> "admin_main"
                        "Instructor" -> "instructor_main"
                        else -> "learner_main"
                    }
                    navController.navigate(dest) {
                        popUpTo("login") { inclusive = true }
                        popUpTo("role_selection") { inclusive = true }
                    }
                }
            )
        }

        // ━━━━━━━ SIGN UP SCREEN ━━━━━━━
        composable("signup") {
            SignUpScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onSubmitSuccess = {
                    navController.navigate("email_verification")
                }
            )
        }

        // ━━━━━━━ EMAIL VERIFICATION SCREEN ━━━━━━━
        composable("email_verification") {
            EmailVerificationScreen(
                viewModel = viewModel,
                onSuccess = { role ->
                    val dest = when (role) {
                        "Admin" -> "admin_main"
                        "Instructor" -> "instructor_main"
                        else -> "learner_main"
                    }
                    navController.navigate(dest) {
                        popUpTo("email_verification") { inclusive = true }
                        popUpTo("login") { inclusive = true }
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        // ━━━━━━━ FORGOT PASSWORD SCREEN ━━━━━━━
        composable("forgot_password") {
            ForgotPasswordScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ━━━━━━━ COPANEL 1: LEARNER NEXUS ━━━━━━━
        composable("learner_main") {
            LearnerMainScreen(
                viewModel = viewModel,
                onNavigateToCourseDetail = { courseId ->
                    navController.navigate("course_detail/$courseId")
                },
                onNavigateToCoursePlayer = { courseId ->
                    navController.navigate("course_player/$courseId")
                },
                onNavigateToUpgrade = {
                    navController.navigate("upgrade_premium")
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("role_selection") {
                        popUpTo("learner_main") { inclusive = true }
                    }
                }
            )
        }

        // ━━━━━━━ COPANEL 2: INSTRUCTOR STUDIO ━━━━━━━
        composable("instructor_main") {
            InstructorMainScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate("role_selection") {
                        popUpTo("instructor_main") { inclusive = true }
                    }
                }
            )
        }

        // ━━━━━━━ COPANEL 3: COMMAND CENTER ━━━━━━━
        composable("admin_main") {
            AdminMainScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate("role_selection") {
                        popUpTo("admin_main") { inclusive = true }
                    }
                }
            )
        }

        // ━━━━━━━ SECONDARY SCREEN: COURSE DETAIL ━━━━━━━
        composable(
            route = "course_detail/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
            CourseDetailScreen(
                courseId = courseId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPlayer = { cid ->
                    navController.navigate("course_player/$cid")
                },
                onNavigateToUpgrade = {
                    navController.navigate("upgrade_premium")
                }
            )
        }

        // ━━━━━━━ SECONDARY SCREEN: COURSE PLAYER ━━━━━━━
        composable(
            route = "course_player/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
            CoursePlayerScreen(
                courseId = courseId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ━━━━━━━ SECONDARY SCREEN: PREMIUM UPGRADE ━━━━━━━
        composable("upgrade_premium") {
            PremiumUpgradeScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
