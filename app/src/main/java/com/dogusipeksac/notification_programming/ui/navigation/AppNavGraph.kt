package com.dogusipeksac.notification_programming.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dogusipeksac.notification_programming.ui.about.AboutScreen
import com.dogusipeksac.notification_programming.ui.about.LegalDocumentScreen
import com.dogusipeksac.notification_programming.ui.addapps.AddAppsScreen
import com.dogusipeksac.notification_programming.ui.appdetail.AppDetailScreen
import com.dogusipeksac.notification_programming.ui.home.HomeScreen
import com.dogusipeksac.notification_programming.ui.onboarding.OnboardingScreen
import com.dogusipeksac.notification_programming.ui.permissions.PermissionChecker
import com.dogusipeksac.notification_programming.ui.settings.SettingsScreen
import com.dogusipeksac.notification_programming.R
import androidx.compose.ui.res.stringResource

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    var listenerGranted by remember {
        mutableStateOf(PermissionChecker.isNotificationListenerGranted(context))
    }
    LifecycleResumeEffect(Unit) {
        listenerGranted = PermissionChecker.isNotificationListenerGranted(context)
        onPauseOrDispose { }
    }

    val navController = rememberNavController()
    val startDestination = if (listenerGranted) Routes.HOME else Routes.ONBOARDING

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = hiltViewModel(),
                onOpenApp = { packageName ->
                    navController.navigate(Routes.appDetail(packageName))
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onAddApps = { navController.navigate(Routes.ADD_APPS) }
            )
        }
        composable(Routes.ADD_APPS) {
            AddAppsScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.APP_DETAIL,
            arguments = listOf(
                navArgument(Routes.ARG_PACKAGE_NAME) { type = NavType.StringType }
            )
        ) {
            AppDetailScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onOpenAbout = { navController.navigate(Routes.ABOUT) }
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(
                onBack = { navController.popBackStack() },
                onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },
                onOpenKvkk = { navController.navigate(Routes.KVKK) },
                onOpenTerms = { navController.navigate(Routes.TERMS) }
            )
        }
        composable(Routes.PRIVACY) {
            LegalDocumentScreen(
                title = stringResource(R.string.privacy_policy),
                body = stringResource(R.string.privacy_body),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.KVKK) {
            LegalDocumentScreen(
                title = stringResource(R.string.kvkk_title),
                body = stringResource(R.string.kvkk_body),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.TERMS) {
            LegalDocumentScreen(
                title = stringResource(R.string.terms_of_use),
                body = stringResource(R.string.terms_body),
                onBack = { navController.popBackStack() }
            )
        }
    }

    LaunchedEffect(listenerGranted) {
        val current = navController.currentDestination?.route
        if (!listenerGranted && current != null && current != Routes.ONBOARDING) {
            navController.navigate(Routes.ONBOARDING) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}
