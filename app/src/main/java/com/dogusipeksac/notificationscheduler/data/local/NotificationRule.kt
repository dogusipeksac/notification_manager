package com.dogusipeksac.notificationscheduler.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_rules")
data class NotificationRule(
    @PrimaryKey val packageName: String,
    val appName: String,
    val enabled: Boolean,
    /** Gün içi dakika, örn. 22:00 -> 1320 */
    val quietStartMinutes: Int,
    /** Gün içi dakika, örn. 08:00 -> 480 */
    val quietEndMinutes: Int,
    val action: NotificationAction
)
