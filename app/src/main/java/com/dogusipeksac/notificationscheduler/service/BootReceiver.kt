package com.dogusipeksac.notificationscheduler.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Cihaz açıldığında sistem listener'ı genelde yeniden bağlar; yine de requestRebind
 * ve bekleyen ertelenmiş bildirim işlerini yeniden kuyruğa alırız.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.i(TAG, "BOOT_COMPLETED: listener rebind isteniyor, bekleyen işler yenileniyor.")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationListenerService.requestRebind(
                ComponentName(context, RuleEnforcingNotificationListener::class.java)
            )
        }

        WorkManager.getInstance(context)
            .enqueue(OneTimeWorkRequestBuilder<PendingNotificationWorker>().build())
    }

    companion object {
        private const val TAG = "BildirimZamanlayici"
    }
}
