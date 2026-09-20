package com.dogusipeksac.notification_programming.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.dogusipeksac.notification_programming.R
import com.dogusipeksac.notification_programming.ui.components.BrandAtmosphere
import com.dogusipeksac.notification_programming.ui.components.QuietHeroIllustration
import com.dogusipeksac.notification_programming.ui.permissions.PermissionChecker
import com.dogusipeksac.notification_programming.ui.theme.PurpleMid

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var listenerGranted by remember {
        mutableStateOf(PermissionChecker.isNotificationListenerGranted(context))
    }
    var postGranted by remember {
        mutableStateOf(PermissionChecker.isPostNotificationsGranted(context))
    }

    LifecycleResumeEffect(Unit) {
        listenerGranted = PermissionChecker.isNotificationListenerGranted(context)
        postGranted = PermissionChecker.isPostNotificationsGranted(context)
        onPauseOrDispose { }
    }

    LaunchedEffect(listenerGranted) {
        if (listenerGranted) onFinished()
    }

    val postPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        postGranted = granted
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        Box(Modifier.fillMaxSize()) {
            BrandAtmosphere()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                QuietHeroIllustration(size = 156.dp)
                Spacer(Modifier.height(28.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                OnboardingPoint(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.onboarding_step1_title),
                    body = stringResource(R.string.onboarding_step1_body)
                )
                Spacer(Modifier.height(10.dp))
                OnboardingPoint(
                    icon = Icons.Outlined.HourglassEmpty,
                    title = stringResource(R.string.onboarding_step2_title),
                    body = stringResource(R.string.onboarding_step2_body)
                )
                Spacer(Modifier.height(10.dp))
                OnboardingPoint(
                    icon = Icons.Outlined.NotificationsOff,
                    title = stringResource(R.string.onboarding_step3_title),
                    body = stringResource(R.string.onboarding_step3_body)
                )
                Spacer(Modifier.height(32.dp))
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
                    textAlign = TextAlign.Center
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !postGranted) {
                    Spacer(Modifier.height(12.dp))
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
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OnboardingPoint(
    icon: ImageVector,
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Text(
                    body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
