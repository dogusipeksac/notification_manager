package com.dogusipeksac.notificationscheduler.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dogusipeksac.notificationscheduler.R
import com.dogusipeksac.notificationscheduler.data.local.SettingsDataStore
import com.dogusipeksac.notificationscheduler.ui.about.AboutScreen
import com.dogusipeksac.notificationscheduler.ui.about.LegalDocumentScreen
import com.dogusipeksac.notificationscheduler.ui.addapps.AddAppsScreen
import com.dogusipeksac.notificationscheduler.ui.appdetail.AppDetailScreen
import com.dogusipeksac.notificationscheduler.ui.home.HomeScreen
import com.dogusipeksac.notificationscheduler.ui.onboarding.OnboardingScreen
import com.dogusipeksac.notificationscheduler.ui.permissions.PermissionChecker
import com.dogusipeksac.notificationscheduler.ui.settings.SettingsScreen
import com.dogusipeksac.notificationscheduler.ui.theme.PurpleMid
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsEntryPoint {
    fun settingsDataStore(): SettingsDataStore
}

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val settings = remember {
        EntryPointAccessors.fromApplication(context, SettingsEntryPoint::class.java)
            .settingsDataStore()
    }
    var introReady by remember { mutableStateOf(false) }
    var hasSeenIntro by remember { mutableStateOf(false) }
    var listenerGranted by remember {
        mutableStateOf(PermissionChecker.isNotificationListenerGranted(context))
    }

    LaunchedEffect(Unit) {
        hasSeenIntro = settings.getHasSeenIntro()
        introReady = true
    }

    LifecycleResumeEffect(Unit) {
        listenerGranted = PermissionChecker.isNotificationListenerGranted(context)
        onPauseOrDispose { }
    }

    if (!introReady) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PurpleMid)
        }
        return
    }

    val needsOnboarding = !hasSeenIntro || !listenerGranted
    val navController = rememberNavController()
    val startDestination = if (needsOnboarding) Routes.ONBOARDING else Routes.HOME

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                viewModel = hiltViewModel(),
                onFinished = {
                    hasSeenIntro = true
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
