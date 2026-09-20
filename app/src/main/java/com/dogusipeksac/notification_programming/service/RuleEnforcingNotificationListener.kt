package com.dogusipeksac.notification_programming.service

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dogusipeksac.notification_programming.data.local.NotificationAction
import com.dogusipeksac.notification_programming.domain.QuietHoursEvaluator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Sistem bildirim akışını dinler. Runtime dialog ile istenemez; kullanıcı
 * Ayarlar > Bildirim erişimi'nden bu uygulamayı açmalıdır
 * ([android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS]).
 *
 * Hilt: sistem servisi olduğu için constructor inject yok, [AndroidEntryPoint] + field inject.
 */
@AndroidEntryPoint
class RuleEnforcingNotificationListener : NotificationListenerService() {

    @Inject lateinit var ruleCache: RuleCache
    @Inject lateinit var scheduler: NotificationScheduler
    @Inject lateinit var connectionState: ListenerConnectionState

    private val listenerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        connectionState.setConnected(true)
        Log.i(TAG, "NotificationListener bağlandı.")
    }

    override fun onListenerDisconnected() {
        connectionState.setConnected(false)
        Log.w(TAG, "NotificationListener koptu; sistem yeniden bağlayabilir.")
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        listenerScope.cancel()
        super.onDestroy()
    }

    /**
     * Ana iş parçacığında kısa tutulur: kural Map cache'den okunur (disk yok).
     * İptal + Room yazma / zamanlama IO dispatcher'da yapılır.
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (shouldIgnore(sbn)) return

        val rule = ruleCache.resolve(sbn.packageName) ?: return
        val nowMinutes = QuietHoursEvaluator.currentMinutesOfDay(Calendar.getInstance())
        if (!QuietHoursEvaluator.isInQuietHours(nowMinutes, rule.quietStartMinutes, rule.quietEndMinutes)) {
            return
        }

        // Sessiz saatteyiz: orijinal bildirimi durum çubuğundan kaldır.
        cancelNotification(sbn.key)

        when (rule.action) {
            NotificationAction.BLOCK -> {
                Log.d(TAG, "BLOCK ${sbn.packageName} key=${sbn.key}")
            }
            NotificationAction.DELAY_AND_SHOW -> {
                listenerScope.launch {
                    runCatching { scheduler.delayAndShow(sbn, rule) }
                        .onFailure { Log.e(TAG, "Erteleme kaydı başarısız", it) }
                }
            }
        }
    }

    private fun shouldIgnore(sbn: StatusBarNotification): Boolean {
        if (sbn.packageName == packageName) return true
        if (sbn.isOngoing) return true
        val flags = sbn.notification.flags
        if (flags and Notification.FLAG_GROUP_SUMMARY != 0) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return true
        }
        return false
    }

    companion object {
        private const val TAG = "BildirimZamanlayici"
    }
}
