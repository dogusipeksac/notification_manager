package com.dogusipeksac.notificationscheduler.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dogusipeksac.notificationscheduler.ui.home.RuleBadge
import com.dogusipeksac.notificationscheduler.ui.theme.OrangeAccent

/**
 * Zaman rozeti: aktif özel kuralda primary container;
 * "yeniden gösterilecek" vurgusu için turuncu yalnızca CUSTOM'da ikon aksanı.
 */
@Composable
fun QuietHoursPill(
    text: String,
    badge: RuleBadge,
    modifier: Modifier = Modifier
) {
    val container = when (badge) {
        RuleBadge.CUSTOM, RuleBadge.DEFAULT -> MaterialTheme.colorScheme.primaryContainer
        RuleBadge.NONE -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val content = when (badge) {
        RuleBadge.CUSTOM, RuleBadge.DEFAULT -> MaterialTheme.colorScheme.onPrimaryContainer
        RuleBadge.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val icon = when (badge) {
        RuleBadge.CUSTOM -> Icons.Outlined.HourglassEmpty
        RuleBadge.DEFAULT -> Icons.Outlined.Schedule
        RuleBadge.NONE -> Icons.Outlined.NotificationsOff
    }
    val iconTint = if (badge == RuleBadge.CUSTOM) OrangeAccent else content
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = container
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = iconTint
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
