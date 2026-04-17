package com.coachantiarnaque.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.coachantiarnaque.ui.screens.*
import com.coachantiarnaque.viewmodel.EmailAnalysisViewModel
import com.coachantiarnaque.viewmodel.HomeViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    sharedText: String? = null
) {
    val homeViewModel: HomeViewModel = viewModel()
    val emailViewModel: EmailAnalysisViewModel = viewModel()

    // Si du texte a été partagé, naviguer vers l'analyse email
    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank()) {
            emailViewModel.setSharedContent(sharedText)
            navController.navigate(NavRoutes.EMAIL_ANALYSIS) {
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToAnalyze = { navController.navigate(NavRoutes.ANALYZE) },
                onNavigateToHistory = { navController.navigate(NavRoutes.HISTORY) },
                onNavigateToDetail = { id ->
                    navController.navigate(NavRoutes.detailRoute(id))
                },
                onNavigateToHelp = { navController.navigate(NavRoutes.HELP) },
                onNavigateToWebsiteCheck = { navController.navigate(NavRoutes.WEBSITE_CHECK) },
                onNavigateToEmailAnalysis = { navController.navigate(NavRoutes.EMAIL_ANALYSIS) },
                viewModel = homeViewModel
            )
        }

        composable(NavRoutes.ANALYZE) {
            AnalyzeScreen(
                onBack = { navController.popBackStack() },
                onPickSms = { navController.navigate(NavRoutes.SMS_PICKER) },
                viewModel = homeViewModel
            )
        }

        composable(NavRoutes.SMS_PICKER) {
            SmsPickerScreen(
                onBack = { navController.popBackStack() },
                onSmsSelected = { body, sender ->
                    homeViewModel.analyzeManualMessage(body, sender)
                    navController.popBackStack(NavRoutes.HOME, inclusive = false)
                }
            )
        }

        composable(
            route = NavRoutes.DETAIL,
            arguments = listOf(navArgument("messageId") { type = NavType.LongType })
        ) { backStackEntry ->
            val messageId = backStackEntry.arguments?.getLong("messageId") ?: return@composable
            AnalysisDetailScreen(
                messageId = messageId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onMessageClick = { id ->
                    navController.navigate(NavRoutes.detailRoute(id))
                }
            )
        }

        composable(NavRoutes.HELP) {
            HelpScreen(
                onBack = { navController.popBackStack() },
                viewModel = homeViewModel
            )
        }

        composable(NavRoutes.WEBSITE_CHECK) {
            WebsiteCheckScreen(
                onBack = { navController.popBackStack() },
                onAskContact = { navController.navigate(NavRoutes.HELP) }
            )
        }

        composable(NavRoutes.EMAIL_ANALYSIS) {
            EmailAnalysisScreen(
                onBack = { navController.popBackStack() },
                onResultReady = {
                    navController.navigate(NavRoutes.EMAIL_RESULT) {
                        launchSingleTop = true
                    }
                },
                viewModel = emailViewModel
            )
        }

        composable(NavRoutes.EMAIL_RESULT) {
            EmailResultScreen(
                onBack = {
                    emailViewModel.clearResult()
                    navController.popBackStack(NavRoutes.HOME, inclusive = false)
                },
                viewModel = emailViewModel
            )
        }
    }
}
