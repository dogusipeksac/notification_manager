package com.dogusipeksac.notification_programming.data.repository

import com.dogusipeksac.notification_programming.data.local.PendingNotification
import com.dogusipeksac.notification_programming.data.local.PendingNotificationDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingNotificationRepository @Inject constructor(
    private val dao: PendingNotificationDao
) {
    suspend fun upsert(item: PendingNotification) = dao.upsert(item)

    suspend fun getDue(now: Long): List<PendingNotification> = dao.getDue(now)

    suspend fun getUpcoming(now: Long): List<PendingNotification> = dao.getUpcoming(now)

    suspend fun getAll(): List<PendingNotification> = dao.getAll()

    suspend fun deleteByKey(originalKey: String) = dao.deleteByKey(originalKey)

    suspend fun deleteByKeys(keys: List<String>) {
        if (keys.isEmpty()) return
        dao.deleteByKeys(keys)
    }
}
