package com.dogusipeksac.notificationscheduler.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dogusipeksac.notificationscheduler.R
import com.dogusipeksac.notificationscheduler.ui.components.AppIcon
import com.dogusipeksac.notificationscheduler.ui.components.BrandAtmosphere
import com.dogusipeksac.notificationscheduler.ui.components.QuietHoursPill
import com.dogusipeksac.notificationscheduler.ui.permissions.PermissionChecker
import com.dogusipeksac.notificationscheduler.ui.theme.OrangeAccent
import com.dogusipeksac.notificationscheduler.ui.theme.PurpleMid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenApp: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onAddApps: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        viewModel.refreshUsage()
        onPauseOrDispose { }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddApps,
                containerColor = PurpleMid,
                contentColor = Color.White
            ) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_apps))
            }
        }
    ) { inner ->
        Box(Modifier.fillMaxSize()) {
            BrandAtmosphere()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
            ) {
                WeekendOffCard(
                    enabled = state.weekendOff,
                    onToggle = viewModel::onWeekendOffChange,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                if (state.totalCount > 0 && !state.usageAccessGranted) {
                    UsageAccessBanner(
                        onGrant = {
                            context.startActivity(PermissionChecker.usageAccessSettingsIntent())
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                if (state.totalCount > 0) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        placeholder = { Text(stringResource(R.string.search_apps)) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Text(
                        text = stringResource(R.string.managed_app_count, state.totalCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                when {
                    state.apps.isEmpty() && state.query.isBlank() -> EmptyManagedAppsState(onAddApps)
                    state.apps.isEmpty() -> EmptySearchState()
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.apps, key = { it.packageName }) { row ->
                                AppRowCard(
                                    row = row,
                                    onClick = { onOpenApp(row.packageName) },
                                    onToggle = { viewModel.onToggle(row, it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageAccessBanner(
    onGrant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Timelapse,
                contentDescription = null,
                tint = OrangeAccent
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.usage_access_banner_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(R.string.usage_access_banner_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onGrant) {
                Text(stringResource(R.string.usage_access_grant))
            }
        }
    }
}

@Composable
private fun WeekendOffCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Weekend,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.weekend_off_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(R.string.weekend_off_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = PurpleMid,
                    checkedThumbColor = Color.White,
                    checkedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun AppRowCard(
    row: AppRowUi,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(packageName = row.packageName, fallbackLabel = row.appName, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.appName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (row.usageLabel != null) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = OrangeAccent
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (row.usageLabel == stringResource(R.string.usage_fmt_zero)) {
                                stringResource(R.string.usage_today_none)
                            } else {
                                stringResource(R.string.usage_today, row.usageLabel)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = OrangeAccent
                        )
                    }
                }
                if (row.enabled && row.quietHoursLabel != null) {
                    Spacer(Modifier.height(6.dp))
                    QuietHoursPill(
                        text = row.quietHoursLabel,
                        badge = RuleBadge.CUSTOM
                    )
                } else if (!row.enabled) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.hours_none),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = row.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = PurpleMid,
                    checkedThumbColor = Color.White,
                    checkedBorderColor = Color.Transparent,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun EmptyManagedAppsState(onAddApps: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .clickable(onClick = onAddApps),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Apps,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.no_apps), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.no_apps_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.add_apps),
            style = MaterialTheme.typography.labelLarge,
            color = PurpleMid
        )
    }
}

@Composable
private fun EmptySearchState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.no_apps),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
