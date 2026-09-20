package com.dogusipeksac.notification_programming.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingNotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PendingNotification)

    @Query("SELECT * FROM pending_notifications WHERE showAtEpochMillis <= :now")
    suspend fun getDue(now: Long): List<PendingNotification>

    @Query("SELECT * FROM pending_notifications WHERE showAtEpochMillis > :now")
    suspend fun getUpcoming(now: Long): List<PendingNotification>

    @Query("SELECT * FROM pending_notifications")
    suspend fun getAll(): List<PendingNotification>

    @Query("DELETE FROM pending_notifications WHERE originalKey = :originalKey")
    suspend fun deleteByKey(originalKey: String)

    @Query("DELETE FROM pending_notifications WHERE originalKey IN (:keys)")
    suspend fun deleteByKeys(keys: List<String>)
}
