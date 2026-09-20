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

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    }

    /** true = Cumartesi/Pazar sessiz saat kuralları uygulanmaz. */
    val weekendOff: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.WEEKEND_OFF] ?: false
    }

    suspend fun getDefaultRule(): DefaultRule = dataStore.data.first().toDefaultRule()

    suspend fun getWeekendOff(): Boolean = dataStore.data.first()[Keys.WEEKEND_OFF] ?: false

    suspend fun getThemeMode(): ThemeMode =
        dataStore.data.first()[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM

    suspend fun saveDefaultRule(rule: DefaultRule) {
        dataStore.edit { prefs ->
            prefs[Keys.ENABLED] = rule.enabled
            prefs[Keys.START] = rule.quietStartMinutes
            prefs[Keys.END] = rule.quietEndMinutes
            prefs[Keys.ACTION] = rule.action.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[Keys.THEME] = mode.name }
    }

    suspend fun setWeekendOff(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.WEEKEND_OFF] = enabled }
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
        val THEME = stringPreferencesKey("theme_mode")
        val WEEKEND_OFF = booleanPreferencesKey("weekend_off")
    }
}
