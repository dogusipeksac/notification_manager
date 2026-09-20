package com.dogusipeksac.notification_programming.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationRuleDao {
    @Query("SELECT * FROM notification_rules ORDER BY appName COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<NotificationRule>>

    @Query("SELECT * FROM notification_rules")
    suspend fun getAll(): List<NotificationRule>

    @Query("SELECT * FROM notification_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackage(packageName: String): NotificationRule?

    @Query("SELECT * FROM notification_rules WHERE packageName = :packageName LIMIT 1")
    fun observeByPackage(packageName: String): Flow<NotificationRule?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: NotificationRule)

    @Query("DELETE FROM notification_rules WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
