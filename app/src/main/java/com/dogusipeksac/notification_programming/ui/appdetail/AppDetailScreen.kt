package com.dogusipeksac.notification_programming.ui.appdetail

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dogusipeksac.notification_programming.R
import com.dogusipeksac.notification_programming.data.local.NotificationAction
import com.dogusipeksac.notification_programming.domain.QuietHoursEvaluator
import com.dogusipeksac.notification_programming.ui.components.ActionSegmentedButtons
import com.dogusipeksac.notification_programming.ui.components.AppIcon
import com.dogusipeksac.notification_programming.ui.components.BrandAtmosphere
import com.dogusipeksac.notification_programming.ui.components.QuietTimePickerDialog
import com.dogusipeksac.notification_programming.ui.components.TimeSelectCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    viewModel: AppDetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { res ->
            snackbar.showSnackbar(context.getString(res))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_rule)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            packageName = state.packageName,
                            fallbackLabel = state.appName,
                            size = 64.dp
                        )
                        Spacer(Modifier.padding(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(state.appName, style = MaterialTheme.typography.titleLarge)
                            Text(
                                state.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.rule_enabled), style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.per_app_hours_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = state.enabled, onCheckedChange = viewModel::onEnabledChange)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.quiet_hours), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimeSelectCard(
                        title = stringResource(R.string.start_label),
                        time = QuietHoursEvaluator.formatMinutes(state.startMinutes),
                        onClick = { pickingStart = true },
                        modifier = Modifier.weight(1f)
                    )
                    TimeSelectCard(
                        title = stringResource(R.string.end_label),
                        time = QuietHoursEvaluator.formatMinutes(state.endMinutes),
                        onClick = { pickingEnd = true },
                        modifier = Modifier.weight(1f),
                        accentSecondary = true
                    )
                }
                if (state.startMinutes > state.endMinutes) {
                    Text(
                        text = stringResource(R.string.overnight_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(stringResource(R.string.action), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ActionSegmentedButtons(
                            selected = state.action,
                            onSelected = viewModel::onActionChange
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(
                                if (state.action == NotificationAction.BLOCK) {
                                    R.string.action_block_desc
                                } else {
                                    R.string.action_delay_desc
                                }
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (state.isCustomRule) {
                    TextButton(onClick = viewModel::resetToDefault, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.reset_to_default))
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    if (pickingStart) {
        QuietTimePickerDialog(
            title = stringResource(R.string.start_label),
            initialMinutes = state.startMinutes,
            onConfirm = {
                viewModel.onStartMinutesChange(it)
                pickingStart = false
            },
            onDismiss = { pickingStart = false }
        )
    }
    if (pickingEnd) {
        QuietTimePickerDialog(
            title = stringResource(R.string.end_label),
            initialMinutes = state.endMinutes,
            onConfirm = {
                viewModel.onEndMinutesChange(it)
                pickingEnd = false
            },
            onDismiss = { pickingEnd = false }
        )
    }
}
