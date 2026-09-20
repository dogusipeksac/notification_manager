package com.dogusipeksac.notificationscheduler.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.dogusipeksac.notificationscheduler.R
import com.dogusipeksac.notificationscheduler.ui.components.BrandAtmosphere
import com.dogusipeksac.notificationscheduler.ui.components.QuietHeroIllustration
import com.dogusipeksac.notificationscheduler.ui.permissions.PermissionChecker
import com.dogusipeksac.notificationscheduler.ui.theme.OrangeAccent
import com.dogusipeksac.notificationscheduler.ui.theme.PurpleMid
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private data class IntroPage(
    val titleRes: Int,
    val bodyRes: Int,
    val icon: ImageVector,
    val isPermission: Boolean = false
)

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var listenerGranted by remember {
        mutableStateOf(PermissionChecker.isNotificationListenerGranted(context))
    }
    var postGranted by remember {
        mutableStateOf(PermissionChecker.isPostNotificationsGranted(context))
    }

    val pages = remember {
        listOf(
            IntroPage(R.string.onboarding_page1_title, R.string.onboarding_page1_body, Icons.Outlined.HourglassEmpty),
            IntroPage(R.string.onboarding_page2_title, R.string.onboarding_page2_body, Icons.Outlined.Apps),
            IntroPage(R.string.onboarding_page3_title, R.string.onboarding_page3_body, Icons.Outlined.DarkMode),
            IntroPage(R.string.onboarding_page4_title, R.string.onboarding_page4_body, Icons.Outlined.Weekend),
            IntroPage(
                R.string.onboarding_page5_title,
                R.string.onboarding_page5_body,
                Icons.Outlined.NotificationsActive,
                isPermission = true
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })

    LifecycleResumeEffect(Unit) {
        listenerGranted = PermissionChecker.isNotificationListenerGranted(context)
        postGranted = PermissionChecker.isPostNotificationsGranted(context)
        onPauseOrDispose { }
    }

    LaunchedEffect(listenerGranted) {
        if (listenerGranted && pagerState.currentPage == pages.lastIndex) {
            viewModel.markIntroSeen()
            onFinished()
        }
    }

    val postPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        postGranted = granted
    }

    fun finishIntro() {
        viewModel.markIntroSeen()
        if (listenerGranted) {
            onFinished()
        } else {
            scope.launch { pagerState.animateScrollToPage(pages.lastIndex) }
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        Box(Modifier.fillMaxSize()) {
            BrandAtmosphere()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (pagerState.currentPage < pages.lastIndex) {
                        TextButton(onClick = { finishIntro() }) {
                            Text(stringResource(R.string.onboarding_skip))
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    val pageOffset = (
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        ).absoluteValue
                    IntroPageContent(
                        page = pages[page],
                        listenerGranted = listenerGranted,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val scale = lerp(0.92f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                scaleX = scale
                                scaleY = scale
                                alpha = lerp(0.55f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                            }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pages.size) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (selected) 22.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) PurpleMid
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                )
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    val isLast = pagerState.currentPage == pages.lastIndex
                    if (isLast) {
                        Button(
                            onClick = {
                                context.startActivity(PermissionChecker.notificationListenerSettingsIntent())
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleMid)
                        ) {
                            Text(stringResource(R.string.open_notification_access))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.onboarding_settings_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !postGranted) {
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    postPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Text(stringResource(R.string.grant_post_notifications))
                            }
                        }
                        if (listenerGranted) {
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.markIntroSeen()
                                    onFinished()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                            ) {
                                Text(stringResource(R.string.onboarding_continue_home))
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleMid)
                        ) {
                            Text(
                                if (pagerState.currentPage == pages.lastIndex - 1) {
                                    stringResource(R.string.onboarding_start)
                                } else {
                                    stringResource(R.string.onboarding_next)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroPageContent(
    page: IntroPage,
    listenerGranted: Boolean,
    modifier: Modifier = Modifier
) {
    val appear by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(420),
        label = "pageAppear"
    )
    Column(
        modifier = modifier
            .padding(horizontal = 28.dp)
            .graphicsLayer {
                alpha = appear
                translationY = (1f - appear) * 24f
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (page.isPermission) {
            QuietHeroIllustration(size = 148.dp)
        } else {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(page.bodyRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (page.isPermission) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (listenerGranted) {
                    stringResource(R.string.permission_granted)
                } else {
                    stringResource(R.string.permission_not_granted)
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (listenerGranted) OrangeAccent else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
