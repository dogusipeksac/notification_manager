package com.dogusipeksac.notification_programming.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dogusipeksac.notification_programming.R

/** Marka logosu — kum saati + ay + bildirim noktası. */
@Composable
fun QuietHeroIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    Image(
        painter = painterResource(R.drawable.ic_brand_logo),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f))
    )
}
