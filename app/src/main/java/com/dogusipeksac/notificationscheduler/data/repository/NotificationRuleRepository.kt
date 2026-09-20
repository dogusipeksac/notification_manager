package com.dogusipeksac.notificationscheduler.data.repository

import com.dogusipeksac.notificationscheduler.data.local.AppLanguage
import com.dogusipeksac.notificationscheduler.data.local.DefaultRule
import com.dogusipeksac.notificationscheduler.data.local.NotificationRule
import com.dogusipeksac.notificationscheduler.data.local.NotificationRuleDao
import com.dogusipeksac.notificationscheduler.data.local.SettingsDataStore
import com.dogusipeksac.notificationscheduler.data.local.ThemeMode
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

    fun observeThemeMode(): Flow<ThemeMode> = settingsDataStore.themeMode

    fun observeWeekendOff(): Flow<Boolean> = settingsDataStore.weekendOff

    fun observeHasSeenIntro(): Flow<Boolean> = settingsDataStore.hasSeenIntro

    fun observeAppLanguage(): Flow<AppLanguage> = settingsDataStore.appLanguage

    fun observeRule(packageName: String): Flow<NotificationRule?> = dao.observeByPackage(packageName)

    suspend fun getAllRules(): List<NotificationRule> = dao.getAll()

    suspend fun getRule(packageName: String): NotificationRule? = dao.getByPackage(packageName)

    suspend fun getDefaultRule(): DefaultRule = settingsDataStore.getDefaultRule()

    suspend fun getWeekendOff(): Boolean = settingsDataStore.getWeekendOff()

    suspend fun getAppLanguage(): AppLanguage = settingsDataStore.getAppLanguage()

    suspend fun upsertRule(rule: NotificationRule) = dao.upsert(rule)

    suspend fun deleteRule(packageName: String) = dao.delete(packageName)

    suspend fun saveDefaultRule(rule: DefaultRule) = settingsDataStore.saveDefaultRule(rule)

    suspend fun setThemeMode(mode: ThemeMode) = settingsDataStore.setThemeMode(mode)

    suspend fun setWeekendOff(enabled: Boolean) = settingsDataStore.setWeekendOff(enabled)

    suspend fun setHasSeenIntro(seen: Boolean) = settingsDataStore.setHasSeenIntro(seen)

    suspend fun setAppLanguage(language: AppLanguage) = settingsDataStore.setAppLanguage(language)

    suspend fun getHasSeenIntro(): Boolean = settingsDataStore.getHasSeenIntro()
}
