package com.dogusipeksac.notificationscheduler.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * DELAY_AND_SHOW ile gizlenen bildirimin, sessiz saat bitince yeniden gösterilecek kopyası.
 * [originalKey] StatusBarNotification.key — aynı bildirimin güncellenmesinde tek satır tutulur.
 */
@Entity(tableName = "pending_notifications")
data class PendingNotification(
    @PrimaryKey val originalKey: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val largeIconPng: ByteArray?,
    val postedAtEpochMillis: Long,
    val showAtEpochMillis: Long
)
