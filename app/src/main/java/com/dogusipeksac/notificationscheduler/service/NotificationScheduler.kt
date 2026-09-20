package com.dogusipeksac.notificationscheduler.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dogusipeksac.notificationscheduler.data.local.NotificationRule
import com.dogusipeksac.notificationscheduler.data.local.PendingNotification
import com.dogusipeksac.notificationscheduler.data.repository.PendingNotificationRepository
import com.dogusipeksac.notificationscheduler.domain.QuietHoursEvaluator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DELAY_AND_SHOW için içeriği kaydeder ve sessiz saat bitişine zamanlama kurar.
 *
 * Tam zamanında gösterim: AlarmManager.setExactAndAllowWhileIdle (Doze'da bile mümkün olduğunca dakik).
 * Yedek: WorkManager — OEM alarm'ı düşürürse veya exact alarm izni yoksa esnek gecikmeyle çalışır.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pendingRepository: PendingNotificationRepository,
    private val extractor: NotificationContentExtractor
) {
    suspend fun delayAndShow(sbn: StatusBarNotification, rule: NotificationRule) {
        val showAt = QuietHoursEvaluator.nextQuietEndMillis(
            startMinutes = rule.quietStartMinutes,
            endMinutes = rule.quietEndMinutes,
            now = Calendar.getInstance()
        )
        val pending = extractor.extract(context, sbn, showAt)
        pendingRepository.upsert(pending)
        scheduleDelivery(pending)
        Log.i(TAG, "Ertelendi: ${pending.packageName} key=${pending.originalKey} showAt=$showAt")
    }

    suspend fun rescheduleAll() {
        val now = System.currentTimeMillis()
        pendingRepository.getUpcoming(now).forEach { scheduleDelivery(it) }
    }

    suspend fun ensureExactAlarmsForUpcoming() {
        val now = System.currentTimeMillis()
        pendingRepository.getUpcoming(now).forEach { setExactAlarm(it) }
    }

    private fun scheduleDelivery(pending: PendingNotification) {
        setExactAlarm(pending)
        enqueueWork(pending)
    }

    /**
     * Exact alarm: SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM verilmişse RTC_WAKEUP ile
     * sessiz saat bitişinde PendingNotificationAlarmReceiver tetiklenir.
     */
    private fun setExactAlarm(pending: PendingNotification) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        if (!canExact) {
            Log.w(TAG, "Exact alarm izni yok; yalnızca WorkManager yedeği kullanılacak.")
            return
        }
        val triggerAt = pending.showAtEpochMillis.coerceAtLeast(System.currentTimeMillis())
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            alarmPendingIntent(pending)
        )
    }

    private fun enqueueWork(pending: PendingNotification) {
        val delayMs = (pending.showAtEpochMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<PendingNotificationWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(pending.originalKey),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun alarmPendingIntent(pending: PendingNotification): PendingIntent {
        val intent = Intent(context, PendingNotificationAlarmReceiver::class.java).apply {
            action = ACTION_SHOW_PENDING
            putExtra(EXTRA_ORIGINAL_KEY, pending.originalKey)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            context,
            pending.originalKey.hashCode(),
            intent,
            flags
        )
    }

    private fun workName(originalKey: String): String = "pending_" + originalKey.hashCode()

    companion object {
        const val TAG = "BildirimZamanlayici"
        const val WORK_TAG = "pending_notifications"
        const val ACTION_SHOW_PENDING = "com.dogusipeksac.notificationscheduler.SHOW_PENDING"
        const val EXTRA_ORIGINAL_KEY = "original_key"
    }
}
