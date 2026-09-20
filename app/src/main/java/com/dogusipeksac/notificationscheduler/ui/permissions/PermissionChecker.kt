package com.dogusipeksac.notificationscheduler.ui.permissions

import android.Manifest
import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dogusipeksac.notificationscheduler.service.RuleEnforcingNotificationListener

data class PermissionSnapshot(
    val notificationListenerGranted: Boolean,
    val postNotificationsGranted: Boolean,
    val exactAlarmGranted: Boolean,
    val batteryOptimizationIgnored: Boolean,
    val usageAccessGranted: Boolean,
    val listenerConnected: Boolean
)

object PermissionChecker {

    fun snapshot(context: Context, listenerConnected: Boolean): PermissionSnapshot {
        return PermissionSnapshot(
            notificationListenerGranted = isNotificationListenerGranted(context),
            postNotificationsGranted = isPostNotificationsGranted(context),
            exactAlarmGranted = isExactAlarmGranted(context),
            batteryOptimizationIgnored = isIgnoringBatteryOptimizations(context),
            usageAccessGranted = isUsageAccessGranted(context),
            listenerConnected = listenerConnected
        )
    }

    fun isNotificationListenerGranted(context: Context): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
    }

    fun isPostNotificationsGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun isExactAlarmGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return false
        return alarmManager.canScheduleExactAlarms()
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(PowerManager::class.java) ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun isUsageAccessGranted(context: Context): Boolean {
        val appOps = context.getSystemService(android.app.AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    fun notificationListenerSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }

    fun usageAccessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    fun exactAlarmSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            appSettingsIntent(context)
        }
    }

    /**
     * Doze whitelist isteği. REQUEST_IGNORE_BATTERY_OPTIMIZATIONS manifest izni gerekir;
     * kullanıcı sistem diyaloğunda onaylar.
     */
    fun ignoreBatteryOptimizationsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    fun appSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    fun requestRebind(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationListenerService.requestRebind(
                ComponentName(context, RuleEnforcingNotificationListener::class.java)
            )
        }
    }
}
