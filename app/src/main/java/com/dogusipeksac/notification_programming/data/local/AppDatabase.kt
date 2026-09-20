package com.dogusipeksac.notification_programming.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [NotificationRule::class, PendingNotification::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationRuleDao(): NotificationRuleDao
    abstract fun pendingNotificationDao(): PendingNotificationDao
}
