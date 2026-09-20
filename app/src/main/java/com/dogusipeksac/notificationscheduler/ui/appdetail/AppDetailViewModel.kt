package com.dogusipeksac.notificationscheduler.ui.appdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogusipeksac.notificationscheduler.R
import com.dogusipeksac.notificationscheduler.data.local.NotificationAction
import com.dogusipeksac.notificationscheduler.data.local.NotificationRule
import com.dogusipeksac.notificationscheduler.data.repository.InstalledAppsRepository
import com.dogusipeksac.notificationscheduler.data.repository.NotificationRuleRepository
import com.dogusipeksac.notificationscheduler.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppDetailUiState(
    val packageName: String = "",
    val appName: String = "",
    val enabled: Boolean = false,
    val startMinutes: Int = 22 * 60,
    val endMinutes: Int = 8 * 60,
    val action: NotificationAction = NotificationAction.DELAY_AND_SHOW,
    val isCustomRule: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ruleRepository: NotificationRuleRepository,
    private val installedAppsRepository: InstalledAppsRepository
) : ViewModel() {

    private val packageName: String = savedStateHandle.get<String>(Routes.ARG_PACKAGE_NAME).orEmpty()

    private val _uiState = MutableStateFlow(AppDetailUiState(packageName = packageName))
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<Int>()
    val messages: SharedFlow<Int> = _messages.asSharedFlow()

    init {
        viewModelScope.launch {
            val appName = installedAppsRepository.appNameFor(packageName)
            val existing = ruleRepository.getRule(packageName)
            val default = ruleRepository.getDefaultRule()
            _uiState.update {
                if (existing != null) {
                    it.copy(
                        appName = existing.appName.ifBlank { appName },
                        enabled = existing.enabled,
                        startMinutes = existing.quietStartMinutes,
                        endMinutes = existing.quietEndMinutes,
                        action = existing.action,
                        isCustomRule = true,
                        isLoading = false
                    )
                } else {
                    it.copy(
                        appName = appName,
                        enabled = false,
                        startMinutes = default.quietStartMinutes,
                        endMinutes = default.quietEndMinutes,
                        action = default.action,
                        isCustomRule = false,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(enabled = enabled) }
        persist(showMessage = false)
    }

    fun onStartMinutesChange(minutes: Int) {
        _uiState.update { it.copy(startMinutes = minutes, enabled = true) }
        persist(showMessage = true)
    }

    fun onEndMinutesChange(minutes: Int) {
        _uiState.update { it.copy(endMinutes = minutes, enabled = true) }
        persist(showMessage = true)
    }

    fun onActionChange(action: NotificationAction) {
        _uiState.update { it.copy(action = action) }
        persist(showMessage = false)
    }

    fun save() {
        persist(showMessage = true)
    }

    fun resetToDefault() {
        viewModelScope.launch {
            ruleRepository.deleteRule(packageName)
            val default = ruleRepository.getDefaultRule()
            _uiState.update {
                it.copy(
                    enabled = default.enabled,
                    startMinutes = default.quietStartMinutes,
                    endMinutes = default.quietEndMinutes,
                    action = default.action,
                    isCustomRule = false
                )
            }
            _messages.emit(R.string.rule_reset)
        }
    }

    private fun persist(showMessage: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.packageName.isBlank() || state.isLoading) return@launch
            ruleRepository.upsertRule(
                NotificationRule(
                    packageName = state.packageName,
                    appName = state.appName,
                    enabled = state.enabled,
                    quietStartMinutes = state.startMinutes,
                    quietEndMinutes = state.endMinutes,
                    action = state.action
                )
            )
            _uiState.update { it.copy(isCustomRule = true) }
            if (showMessage) {
                _messages.emit(R.string.rule_saved)
            }
        }
    }
}
