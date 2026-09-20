package com.dogusipeksac.notificationscheduler.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import com.dogusipeksac.notificationscheduler.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUsageRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun todayForegroundUsageMillis(): Map<String, Long> {
        if (!hasUsageAccess()) return emptyMap()
        val manager = context.getSystemService(UsageStatsManager::class.java) ?: return emptyMap()
        val end = System.currentTimeMillis()
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            ?: return emptyMap()

        return stats
            .asSequence()
            .filter { it.totalTimeInForeground > 0L }
            .groupBy { it.packageName }
            .mapValues { (_, list) -> list.maxOf { it.totalTimeInForeground } }
    }

    fun formatDuration(millis: Long): String {
        if (millis <= 0L) return context.getString(R.string.usage_fmt_zero)
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(millis).coerceAtLeast(1)
        val hours = (totalMinutes / 60).toInt()
        val minutes = (totalMinutes % 60).toInt()
        return when {
            hours > 0 && minutes > 0 ->
                context.getString(R.string.usage_fmt_hours_minutes, hours, minutes)
            hours > 0 -> context.getString(R.string.usage_fmt_hours, hours)
            else -> context.getString(R.string.usage_fmt_minutes, minutes)
        }
    }
}
