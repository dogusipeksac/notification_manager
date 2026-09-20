package com.dogusipeksac.notification_programming.service

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.service.notification.StatusBarNotification
import androidx.core.graphics.drawable.toBitmap
import com.dogusipeksac.notification_programming.data.local.PendingNotification
import com.dogusipeksac.notification_programming.data.repository.InstalledAppsRepository
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationContentExtractor @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository
) {
    fun extract(
        context: Context,
        sbn: StatusBarNotification,
        showAtEpochMillis: Long
    ): PendingNotification {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        return PendingNotification(
            originalKey = sbn.key,
            packageName = sbn.packageName,
            appName = installedAppsRepository.appNameFor(sbn.packageName),
            title = title,
            text = text,
            largeIconPng = extractLargeIconPng(context, sbn),
            postedAtEpochMillis = sbn.postTime,
            showAtEpochMillis = showAtEpochMillis
        )
    }

    private fun extractLargeIconPng(context: Context, sbn: StatusBarNotification): ByteArray? {
        val icon = sbn.notification.getLargeIcon() ?: return null
        val drawable = icon.loadDrawable(context) ?: return null
        val bitmap = when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> runCatching { drawable.toBitmap() }.getOrNull()
        } ?: return null
        val scaled = scaleDown(bitmap, MAX_ICON_PX)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
        return out.toByteArray()
    }

    private fun scaleDown(bitmap: Bitmap, maxPx: Int): Bitmap {
        val largest = maxOf(bitmap.width, bitmap.height)
        if (largest <= maxPx) return bitmap
        val scale = maxPx.toFloat() / largest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    companion object {
        private const val MAX_ICON_PX = 256
    }
}
