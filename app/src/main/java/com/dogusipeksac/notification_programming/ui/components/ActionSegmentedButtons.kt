package com.dogusipeksac.notification_programming.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dogusipeksac.notification_programming.R
import com.dogusipeksac.notification_programming.data.local.NotificationAction
import com.dogusipeksac.notification_programming.ui.theme.PurpleMid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionSegmentedButtons(
    selected: NotificationAction,
    onSelected: (NotificationAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = NotificationAction.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        actions.forEachIndexed { index, action ->
            val isSelected = selected == action
            SegmentedButton(
                selected = isSelected,
                onClick = { onSelected(action) },
                shape = SegmentedButtonDefaults.itemShape(index, actions.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = PurpleMid,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                icon = {
                    Icon(
                        imageVector = when (action) {
                            NotificationAction.BLOCK -> Icons.Outlined.Block
                            NotificationAction.DELAY_AND_SHOW -> Icons.Outlined.Snooze
                        },
                        contentDescription = null
                    )
                }
            ) {
                Text(
                    text = when (action) {
                        NotificationAction.BLOCK -> stringResource(R.string.action_block)
                        NotificationAction.DELAY_AND_SHOW -> stringResource(R.string.action_delay)
                    }
                )
            }
        }
    }
}
