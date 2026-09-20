package com.dogusipeksac.notification_programming.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogusipeksac.notification_programming.data.local.DefaultRule
import com.dogusipeksac.notification_programming.data.local.NotificationAction
import com.dogusipeksac.notification_programming.data.local.ThemeMode
import com.dogusipeksac.notification_programming.data.repository.NotificationRuleRepository
import com.dogusipeksac.notification_programming.service.ListenerConnectionState
import com.dogusipeksac.notification_programming.ui.permissions.PermissionSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val defaultRule: DefaultRule = DefaultRule(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val weekendOff: Boolean = false,
    val permissions: PermissionSnapshot? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val ruleRepository: NotificationRuleRepository,
    listenerConnectionState: ListenerConnectionState
) : ViewModel() {

    private val permissions = MutableStateFlow<PermissionSnapshot?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        ruleRepository.observeDefaultRule(),
        ruleRepository.observeThemeMode(),
        ruleRepository.observeWeekendOff(),
        permissions,
        listenerConnectionState.connected
    ) { defaultRule, themeMode, weekendOff, perms, connected ->
        SettingsUiState(
            defaultRule = defaultRule,
            themeMode = themeMode,
            weekendOff = weekendOff,
            permissions = perms?.copy(listenerConnected = connected)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onPermissionsRefreshed(snapshot: PermissionSnapshot) {
        permissions.value = snapshot
    }

    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch { ruleRepository.setThemeMode(mode) }
    }

    fun onWeekendOffChange(enabled: Boolean) {
        viewModelScope.launch { ruleRepository.setWeekendOff(enabled) }
    }

    fun onDefaultEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            val current = ruleRepository.getDefaultRule()
            ruleRepository.saveDefaultRule(current.copy(enabled = enabled))
        }
    }

    fun onDefaultStartChange(minutes: Int) {
        viewModelScope.launch {
            val current = ruleRepository.getDefaultRule()
            ruleRepository.saveDefaultRule(current.copy(quietStartMinutes = minutes))
        }
    }

    fun onDefaultEndChange(minutes: Int) {
        viewModelScope.launch {
            val current = ruleRepository.getDefaultRule()
            ruleRepository.saveDefaultRule(current.copy(quietEndMinutes = minutes))
        }
    }

    fun onDefaultActionChange(action: NotificationAction) {
        viewModelScope.launch {
            val current = ruleRepository.getDefaultRule()
            ruleRepository.saveDefaultRule(current.copy(action = action))
        }
    }
}
