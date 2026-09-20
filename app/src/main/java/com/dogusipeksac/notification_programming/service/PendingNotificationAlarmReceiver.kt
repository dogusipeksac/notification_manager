package com.dogusipeksac.notification_programming.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * AlarmManager exact alarm tetikleyicisi. Hilt inject etmez; hemen WorkManager'a
 * devreder (HiltWorkerFactory ile poster/scheduler enjekte edilir).
 */
class PendingNotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationScheduler.ACTION_SHOW_PENDING) return
        val pendingResult = goAsync()
        WorkManager.getInstance(context)
            .enqueue(OneTimeWorkRequestBuilder<PendingNotificationWorker>().build())
        pendingResult.finish()
    }
}
