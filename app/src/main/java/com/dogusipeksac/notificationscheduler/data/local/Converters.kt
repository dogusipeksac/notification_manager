package com.dogusipeksac.notificationscheduler.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun toAction(value: String): NotificationAction = NotificationAction.valueOf(value)

    @TypeConverter
    fun fromAction(value: NotificationAction): String = value.name
}
