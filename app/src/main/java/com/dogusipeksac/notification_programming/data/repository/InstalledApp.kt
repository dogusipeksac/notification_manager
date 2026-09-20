package com.dogusipeksac.notification_programming.data.repository

data class InstalledApp(
    val packageName: String,
    val appName: String,
    val isSystem: Boolean,
    val hasLauncher: Boolean
)
