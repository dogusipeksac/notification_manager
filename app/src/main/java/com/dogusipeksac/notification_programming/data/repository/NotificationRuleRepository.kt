package com.dogusipeksac.notification_programming.data.repository

import com.dogusipeksac.notification_programming.data.local.DefaultRule
import com.dogusipeksac.notification_programming.data.local.NotificationRule
import com.dogusipeksac.notification_programming.data.local.NotificationRuleDao
import com.dogusipeksac.notification_programming.data.local.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRuleRepository @Inject constructor(
    private val dao: NotificationRuleDao,
    private val settingsDataStore: SettingsDataStore
) {
    fun observeRules(): Flow<List<NotificationRule>> = dao.observeAll()

    fun observeDefaultRule(): Flow<DefaultRule> = settingsDataStore.defaultRule

    fun observeRule(packageName: String): Flow<NotificationRule?> = dao.observeByPackage(packageName)

    suspend fun getAllRules(): List<NotificationRule> = dao.getAll()

    suspend fun getRule(packageName: String): NotificationRule? = dao.getByPackage(packageName)

    suspend fun getDefaultRule(): DefaultRule = settingsDataStore.getDefaultRule()

    suspend fun upsertRule(rule: NotificationRule) = dao.upsert(rule)

    suspend fun deleteRule(packageName: String) = dao.delete(packageName)

    suspend fun saveDefaultRule(rule: DefaultRule) = settingsDataStore.saveDefaultRule(rule)
}
