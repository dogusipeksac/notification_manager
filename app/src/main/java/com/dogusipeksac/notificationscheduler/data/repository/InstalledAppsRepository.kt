package com.dogusipeksac.notificationscheduler.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstalledAppsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    /**
     * Telefondaki tüm yüklü paketleri döner (sistem + kullanıcı).
     * QUERY_ALL_PACKAGES olmadan Android 11+ listesi eksik kalır.
     */
    fun loadInstalledApps(): List<InstalledApp> {
        val pm = context.packageManager
        val ownPackage = context.packageName
        val launcherPackages = launcherPackageNames(pm)

        return installedApplications(pm)
            .asSequence()
            .filter { it.packageName != ownPackage }
            .map { info ->
                val isPureSystem = info.flags and ApplicationInfo.FLAG_SYSTEM != 0 &&
                    info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0
                InstalledApp(
                    packageName = info.packageName,
                    appName = pm.getApplicationLabel(info).toString().ifBlank { info.packageName },
                    isSystem = isPureSystem,
                    hasLauncher = info.packageName in launcherPackages
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
            .toList()
    }

    fun appNameFor(packageName: String): String {
        val pm = context.packageManager
        return try {
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    private fun launcherPackageNames(pm: PackageManager): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    private fun installedApplications(pm: PackageManager): List<ApplicationInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }
    }
}
