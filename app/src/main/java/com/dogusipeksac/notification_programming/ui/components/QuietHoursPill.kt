package com.dogusipeksac.notification_programming.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dogusipeksac.notification_programming.ui.home.RuleBadge

@Composable
fun QuietHoursPill(
    text: String,
    badge: RuleBadge,
    modifier: Modifier = Modifier
) {
    val container = when (badge) {
        RuleBadge.CUSTOM -> MaterialTheme.colorScheme.secondaryContainer
        RuleBadge.DEFAULT -> MaterialTheme.colorScheme.primaryContainer
        RuleBadge.NONE -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val content = when (badge) {
        RuleBadge.CUSTOM -> MaterialTheme.colorScheme.onSecondaryContainer
        RuleBadge.DEFAULT -> MaterialTheme.colorScheme.onPrimaryContainer
        RuleBadge.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = container
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = content
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = content,
                maxLines = 1
            )
        }
    }
}
