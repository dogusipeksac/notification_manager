package com.dogusipeksac.notificationscheduler.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogusipeksac.notificationscheduler.data.local.AppLanguage
import com.dogusipeksac.notificationscheduler.data.local.DefaultRule
import com.dogusipeksac.notificationscheduler.data.local.NotificationAction
import com.dogusipeksac.notificationscheduler.data.repository.NotificationRuleRepository
import com.dogusipeksac.notificationscheduler.service.ListenerConnectionState
import com.dogusipeksac.notificationscheduler.ui.permissions.PermissionSnapshot
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
    val weekendOff: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
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
        ruleRepository.observeWeekendOff(),
        ruleRepository.observeAppLanguage(),
        permissions,
        listenerConnectionState.connected
    ) { defaultRule, weekendOff, language, perms, connected ->
        SettingsUiState(
            defaultRule = defaultRule,
            weekendOff = weekendOff,
            appLanguage = language,
            permissions = perms?.copy(listenerConnected = connected)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onPermissionsRefreshed(snapshot: PermissionSnapshot) {
        permissions.value = snapshot
    }

    fun onWeekendOffChange(enabled: Boolean) {
        viewModelScope.launch { ruleRepository.setWeekendOff(enabled) }
    }

    fun onLanguageChange(language: AppLanguage) {
        viewModelScope.launch {
            ruleRepository.setAppLanguage(language)
            // MainActivity dil Flow'unu dinleyip recreate eder.
        }
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
