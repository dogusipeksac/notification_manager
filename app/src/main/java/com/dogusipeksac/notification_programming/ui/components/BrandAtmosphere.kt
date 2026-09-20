package com.dogusipeksac.notification_programming.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.dogusipeksac.notification_programming.ui.theme.BrandOrange
import com.dogusipeksac.notification_programming.ui.theme.BrandPurple

@Composable
fun BrandAtmosphere(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandPurple.copy(alpha = 0.22f),
                        BrandOrange.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                )
            )
    )
}
