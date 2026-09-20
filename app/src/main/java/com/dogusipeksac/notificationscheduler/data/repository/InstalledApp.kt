package com.dogusipeksac.notificationscheduler.data.repository

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isSystem: Boolean,
    val hasLauncher: Boolean
)
