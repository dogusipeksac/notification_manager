package com.dogusipeksac.notification_programming.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dogusipeksac.notification_programming.ui.theme.DarkElevated
import com.dogusipeksac.notification_programming.ui.theme.PurpleMid

@Composable
fun BrandAtmosphere(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val top = if (dark) DarkElevated.copy(alpha = 0.40f) else PurpleMid.copy(alpha = 0.10f)
    val mid = PurpleMid.copy(alpha = if (dark) 0.08f else 0.04f)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(top, mid, Color.Transparent))
            )
    )
}
