package com.dogusipeksac.notificationscheduler.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dogusipeksac.notificationscheduler.R
import com.dogusipeksac.notificationscheduler.data.local.PendingNotification
import com.dogusipeksac.notificationscheduler.data.repository.PendingNotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Saklanan içerikle kendi uygulamamız adına bildirim post eder.
 * Android 13+ için POST_NOTIFICATIONS izni zorunludur; aksi halde notify sessizce no-op olur.
 */
@Singleton
class DelayedNotificationPoster @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pendingRepository: PendingNotificationRepository
) {
    suspend fun postDueNotifications() {
        val now = System.currentTimeMillis()
        val due = pendingRepository.getDue(now)
        if (due.isEmpty()) return
        due.forEach { item ->
            runCatching { post(item) }
                .onFailure { Log.e(TAG, "Ertelenmiş bildirim gösterilemedi: ${item.originalKey}", it) }
        }
        pendingRepository.deleteByKeys(due.map { it.originalKey })
        Log.i(TAG, "Ertelenmiş ${due.size} bildirim gösterildi.")
    }

    private fun post(item: PendingNotification) {
        ensureChannel()
        val largeIcon = item.largeIconPng?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        val contentTitle = item.title?.takeIf { it.isNotBlank() } ?: item.appName
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setColor(ContextCompat.getColor(context, R.color.brand_primary))
            .setContentTitle(contentTitle)
            .setContentText(item.text)
            .setSubText(item.appName)
            .setAutoCancel(true)
            .setWhen(item.postedAtEpochMillis)
            .setShowWhen(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon)
        }
        launchIntent(item)?.let { builder.setContentIntent(it) }

        val id = item.originalKey.hashCode()
        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    private fun launchIntent(item: PendingNotification): PendingIntent? {
        val launch = context.packageManager.getLaunchIntentForPackage(item.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: return null
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, item.originalKey.hashCode(), launch, flags)
    }

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.delayed_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.delayed_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "delayed_notifications"
        private const val TAG = "BildirimZamanlayici"
    }
}
