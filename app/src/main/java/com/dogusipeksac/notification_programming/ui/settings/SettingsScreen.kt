package com.dogusipeksac.notification_programming.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dogusipeksac.notification_programming.R
import com.dogusipeksac.notification_programming.domain.QuietHoursEvaluator
import com.dogusipeksac.notification_programming.ui.components.ActionSegmentedButtons
import com.dogusipeksac.notification_programming.ui.components.BrandAtmosphere
import com.dogusipeksac.notification_programming.ui.components.QuietTimePickerDialog
import com.dogusipeksac.notification_programming.ui.components.TimeSelectCard
import com.dogusipeksac.notification_programming.ui.permissions.PermissionChecker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    val postPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.onPermissionsRefreshed(
            PermissionChecker.snapshot(context, state.permissions?.listenerConnected == true)
        )
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onPermissionsRefreshed(
            PermissionChecker.snapshot(context, state.permissions?.listenerConnected == true)
        )
        onPauseOrDispose { }
    }

    val perms = state.permissions
    val defaultRule = state.defaultRule

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { inner ->
        Box(Modifier.fillMaxSize()) {
            BrandAtmosphere()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.default_rule_section), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.default_rule_section_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.rule_enabled), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = defaultRule.enabled,
                            onCheckedChange = viewModel::onDefaultEnabledChange
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimeSelectCard(
                        title = stringResource(R.string.start_label),
                        time = QuietHoursEvaluator.formatMinutes(defaultRule.quietStartMinutes),
                        onClick = { pickingStart = true },
                        modifier = Modifier.weight(1f)
                    )
                    TimeSelectCard(
                        title = stringResource(R.string.end_label),
                        time = QuietHoursEvaluator.formatMinutes(defaultRule.quietEndMinutes),
                        onClick = { pickingEnd = true },
                        modifier = Modifier.weight(1f),
                        accentSecondary = true
                    )
                }
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ActionSegmentedButtons(
                            selected = defaultRule.action,
                            onSelected = viewModel::onDefaultActionChange
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.permissions_section), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        PermissionRow(
                            title = stringResource(R.string.perm_listener),
                            granted = perms?.notificationListenerGranted == true,
                            onFix = {
                                context.startActivity(PermissionChecker.notificationListenerSettingsIntent())
                            }
                        )
                        PermissionRow(
                            title = stringResource(R.string.perm_listener_connected),
                            granted = perms?.listenerConnected == true,
                            fixLabel = stringResource(R.string.rebind_listener),
                            onFix = { PermissionChecker.requestRebind(context) }
                        )
                        PermissionRow(
                            title = stringResource(R.string.perm_post_notifications),
                            granted = perms?.postNotificationsGranted == true,
                            onFix = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    postPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                        PermissionRow(
                            title = stringResource(R.string.perm_exact_alarm),
                            granted = perms?.exactAlarmGranted == true,
                            onFix = {
                                context.startActivity(PermissionChecker.exactAlarmSettingsIntent(context))
                            }
                        )
                        PermissionRow(
                            title = stringResource(R.string.perm_battery),
                            granted = perms?.batteryOptimizationIgnored == true,
                            onFix = {
                                context.startActivity(PermissionChecker.ignoreBatteryOptimizationsIntent(context))
                            }
                        )
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    if (pickingStart) {
        QuietTimePickerDialog(
            title = stringResource(R.string.start_label),
            initialMinutes = defaultRule.quietStartMinutes,
            onConfirm = {
                viewModel.onDefaultStartChange(it)
                pickingStart = false
            },
            onDismiss = { pickingStart = false }
        )
    }
    if (pickingEnd) {
        QuietTimePickerDialog(
            title = stringResource(R.string.end_label),
            initialMinutes = defaultRule.quietEndMinutes,
            onConfirm = {
                viewModel.onDefaultEndChange(it)
                pickingEnd = false
            },
            onDismiss = { pickingEnd = false }
        )
    }
}

@Composable
private fun PermissionRow(
    title: String,
    granted: Boolean,
    fixLabel: String = stringResource(R.string.fix),
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (granted) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(if (granted) R.string.status_ok else R.string.status_missing),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!granted) {
            TextButton(onClick = onFix) {
                Text(fixLabel)
            }
        }
    }
}
