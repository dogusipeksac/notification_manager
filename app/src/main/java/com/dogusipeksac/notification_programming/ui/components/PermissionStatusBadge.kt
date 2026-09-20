package com.dogusipeksac.notification_programming.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dogusipeksac.notification_programming.R
import com.dogusipeksac.notification_programming.ui.theme.OrangeAccent
import com.dogusipeksac.notification_programming.ui.theme.SuccessContainerDark
import com.dogusipeksac.notification_programming.ui.theme.SuccessContainerLight
import com.dogusipeksac.notification_programming.ui.theme.SuccessGreen
import com.dogusipeksac.notification_programming.ui.theme.WarningContainerDark
import com.dogusipeksac.notification_programming.ui.theme.WarningContainerLight

@Composable
fun PermissionStatusBadge(
    granted: Boolean,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val container = when {
        granted && dark -> SuccessContainerDark
        granted -> SuccessContainerLight
        dark -> WarningContainerDark
        else -> WarningContainerLight
    }
    val content = if (granted) SuccessGreen else OrangeAccent
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = container
    ) {
        Text(
            text = stringResource(if (granted) R.string.status_granted else R.string.status_needed),
            style = MaterialTheme.typography.labelMedium,
            color = content,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
