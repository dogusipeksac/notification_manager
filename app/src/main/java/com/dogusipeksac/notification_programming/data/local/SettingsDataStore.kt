package com.dogusipeksac.notification_programming.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.settingsDataStore

    val defaultRule: Flow<DefaultRule> = dataStore.data.map { prefs -> prefs.toDefaultRule() }

    suspend fun getDefaultRule(): DefaultRule = dataStore.data.first().toDefaultRule()

    suspend fun saveDefaultRule(rule: DefaultRule) {
        dataStore.edit { prefs ->
            prefs[Keys.ENABLED] = rule.enabled
            prefs[Keys.START] = rule.quietStartMinutes
            prefs[Keys.END] = rule.quietEndMinutes
            prefs[Keys.ACTION] = rule.action.name
        }
    }

    private fun Preferences.toDefaultRule(): DefaultRule {
        val fallback = DefaultRule()
        return DefaultRule(
            enabled = this[Keys.ENABLED] ?: fallback.enabled,
            quietStartMinutes = this[Keys.START] ?: fallback.quietStartMinutes,
            quietEndMinutes = this[Keys.END] ?: fallback.quietEndMinutes,
            action = this[Keys.ACTION]?.let { runCatching { NotificationAction.valueOf(it) }.getOrNull() }
                ?: fallback.action
        )
    }

    private object Keys {
        val ENABLED = booleanPreferencesKey("default_enabled")
        val START = intPreferencesKey("default_quiet_start")
        val END = intPreferencesKey("default_quiet_end")
        val ACTION = stringPreferencesKey("default_action")
    }
}
