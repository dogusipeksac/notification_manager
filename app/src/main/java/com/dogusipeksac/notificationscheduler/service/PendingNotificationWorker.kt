package com.dogusipeksac.notificationscheduler.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager yedek işi: vadesi gelen ertelenmiş bildirimleri post eder,
 * kalanlar için exact alarm'ları yeniden kurar (reboot sonrası da buradan toparlanır).
 */
@HiltWorker
class PendingNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val poster: DelayedNotificationPoster,
    private val scheduler: NotificationScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        poster.postDueNotifications()
        scheduler.ensureExactAlarmsForUpcoming()
        return Result.success()
    }
}
