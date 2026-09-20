package com.dogusipeksac.notificationscheduler.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.dogusipeksac.notificationscheduler.R
import com.dogusipeksac.notificationscheduler.data.local.AppLanguage
import com.dogusipeksac.notificationscheduler.domain.QuietHoursEvaluator
import com.dogusipeksac.notificationscheduler.ui.components.ActionSegmentedButtons
import com.dogusipeksac.notificationscheduler.ui.components.BrandAtmosphere
import com.dogusipeksac.notificationscheduler.ui.components.CircularTimeSelector
import com.dogusipeksac.notificationscheduler.ui.components.PermissionStatusBadge
import com.dogusipeksac.notificationscheduler.ui.components.QuietTimePickerDialog
import com.dogusipeksac.notificationscheduler.ui.permissions.PermissionChecker
import com.dogusipeksac.notificationscheduler.ui.theme.PurpleMid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }
    var editingDefault by remember { mutableStateOf(false) }

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
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
                Text(
                    stringResource(R.string.language_section),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.language_label),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    stringResource(R.string.language_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        LanguageSelector(
                            selected = state.appLanguage,
                            onSelected = viewModel::onLanguageChange
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.weekend_off_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.weekendOff) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Weekend,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.weekend_off_title),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                stringResource(R.string.weekend_off_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.weekendOff,
                            onCheckedChange = viewModel::onWeekendOffChange,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = PurpleMid,
                                checkedThumbColor = Color.White
                            )
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.default_rule_section),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.default_rule_section),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (defaultRule.enabled) {
                                        stringResource(
                                            R.string.default_hours_summary,
                                            QuietHoursEvaluator.formatMinutes(defaultRule.quietStartMinutes),
                                            QuietHoursEvaluator.formatMinutes(defaultRule.quietEndMinutes)
                                        )
                                    } else {
                                        stringResource(R.string.default_rule_section_hint)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { editingDefault = !editingDefault }) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = stringResource(R.string.edit_default_rule)
                                )
                            }
                            Switch(
                                checked = defaultRule.enabled,
                                onCheckedChange = viewModel::onDefaultEnabledChange,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = PurpleMid,
                                    checkedThumbColor = Color.White
                                )
                            )
                        }
                        if (editingDefault) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CircularTimeSelector(
                                    title = stringResource(R.string.start_label),
                                    time = QuietHoursEvaluator.formatMinutes(defaultRule.quietStartMinutes),
                                    minutesOfDay = defaultRule.quietStartMinutes,
                                    onClick = { pickingStart = true },
                                    modifier = Modifier.weight(1f)
                                )
                                CircularTimeSelector(
                                    title = stringResource(R.string.end_label),
                                    time = QuietHoursEvaluator.formatMinutes(defaultRule.quietEndMinutes),
                                    minutesOfDay = defaultRule.quietEndMinutes,
                                    onClick = { pickingEnd = true },
                                    modifier = Modifier.weight(1f),
                                    accentSecondary = true
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            ActionSegmentedButtons(
                                selected = defaultRule.action,
                                onSelected = viewModel::onDefaultActionChange
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.permissions_section),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        PermissionRow(
                            title = stringResource(R.string.perm_listener),
                            granted = perms?.notificationListenerGranted == true,
                            onClick = {
                                context.startActivity(PermissionChecker.notificationListenerSettingsIntent())
                            }
                        )
                        PermissionRow(
                            title = stringResource(R.string.perm_post_notifications),
                            granted = perms?.postNotificationsGranted == true,
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    postPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                        PermissionRow(
                            title = stringResource(R.string.perm_exact_alarm),
                            granted = perms?.exactAlarmGranted == true,
                            onClick = {
                                context.startActivity(PermissionChecker.exactAlarmSettingsIntent(context))
                            }
                        )
                        PermissionRow(
                            title = stringResource(R.string.perm_battery),
                            granted = perms?.batteryOptimizationIgnored == true,
                            onClick = {
                                context.startActivity(PermissionChecker.ignoreBatteryOptimizationsIntent(context))
                            }
                        )
                        PermissionRow(
                            title = stringResource(R.string.perm_usage_access),
                            granted = perms?.usageAccessGranted == true,
                            onClick = {
                                context.startActivity(PermissionChecker.usageAccessSettingsIntent())
                            }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.about_section),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenAbout),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.about_open),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                stringResource(R.string.about_tagline),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun LanguageSelector(
    selected: AppLanguage,
    onSelected: (AppLanguage) -> Unit
) {
    val modes = AppLanguage.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, modes.size)
            ) {
                Text(
                    text = when (mode) {
                        AppLanguage.SYSTEM -> stringResource(R.string.language_system)
                        AppLanguage.TURKISH -> stringResource(R.string.language_turkish)
                        AppLanguage.ENGLISH -> stringResource(R.string.language_english)
                    },
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        PermissionStatusBadge(granted = granted)
    }
}
