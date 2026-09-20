package com.dogusipeksac.notificationscheduler.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dogusipeksac.notificationscheduler.ui.theme.OrangeMid
import com.dogusipeksac.notificationscheduler.ui.theme.PurpleLight
import com.dogusipeksac.notificationscheduler.ui.theme.PurpleMid

/**
 * Dairesel time-picker tarzı seçici.
 * Mor degrade halka; ortada seçili saat büyük rakam.
 */
@Composable
fun CircularTimeSelector(
    title: String,
    time: String,
    minutesOfDay: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentSecondary: Boolean = false
) {
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(132.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val stroke = 7.dp.toPx()
                    val radius = size.minDimension / 2f - stroke
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(
                        color = track,
                        radius = radius,
                        style = Stroke(width = stroke)
                    )
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                PurpleMid,
                                PurpleLight,
                                if (accentSecondary) OrangeMid.copy(alpha = 0.65f) else PurpleMid,
                                PurpleMid
                            ),
                            center = center
                        ),
                        radius = radius,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = time,
                    style = MaterialTheme.typography.headlineSmall,
                    color = onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
