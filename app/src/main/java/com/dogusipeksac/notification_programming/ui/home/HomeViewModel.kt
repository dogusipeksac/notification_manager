package com.dogusipeksac.notification_programming.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogusipeksac.notification_programming.data.local.NotificationRule
import com.dogusipeksac.notification_programming.data.repository.AppUsageRepository
import com.dogusipeksac.notification_programming.data.repository.InstalledAppsRepository
import com.dogusipeksac.notification_programming.data.repository.NotificationRuleRepository
import com.dogusipeksac.notification_programming.domain.QuietHoursEvaluator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class RuleBadge { NONE, CUSTOM, DEFAULT }

data class AppRowUi(
    val packageName: String,
    val appName: String,
    val badge: RuleBadge,
    val enabled: Boolean,
    val quietHoursLabel: String?,
    val usageLabel: String?
)

data class HomeUiState(
    val query: String = "",
    val isLoading: Boolean = true,
    val weekendOff: Boolean = false,
    val usageAccessGranted: Boolean = false,
    val apps: List<AppRowUi> = emptyList(),
    val visibleCount: Int = 0,
    val totalCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    private val ruleRepository: NotificationRuleRepository,
    private val appUsageRepository: AppUsageRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val usageByPackage = MutableStateFlow<Map<String, Long>>(emptyMap())
    private val usageAccessGranted = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        query,
        ruleRepository.observeRules(),
        ruleRepository.observeWeekendOff(),
        usageByPackage,
        usageAccessGranted
    ) { q, rules, weekendOff, usage, usageGranted ->
                val rows = rules
            .map { rule -> toRow(rule, usage, usageGranted) }
            .filter { row ->
                q.isBlank() ||
                    row.appName.contains(q, ignoreCase = true) ||
                    row.packageName.contains(q, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<AppRowUi> { it.enabled }
                    .thenByDescending { usage[it.packageName] ?: 0L }
                    .thenBy { it.appName.lowercase() }
            )
        HomeUiState(
            query = q,
            isLoading = false,
            weekendOff = weekendOff,
            usageAccessGranted = usageGranted,
            apps = rows,
            visibleCount = rows.size,
            totalCount = rules.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        refreshUsage()
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onWeekendOffChange(enabled: Boolean) {
        viewModelScope.launch { ruleRepository.setWeekendOff(enabled) }
    }

    fun onToggle(row: AppRowUi, enabled: Boolean) {
        viewModelScope.launch {
            val existing = ruleRepository.getRule(row.packageName) ?: return@launch
            ruleRepository.upsertRule(existing.copy(enabled = enabled))
        }
    }

    fun onRemove(packageName: String) {
        viewModelScope.launch { ruleRepository.deleteRule(packageName) }
    }

    fun refreshUsage() {
        viewModelScope.launch {
            val granted = appUsageRepository.hasUsageAccess()
            usageAccessGranted.value = granted
            usageByPackage.value = if (granted) {
                withContext(Dispatchers.IO) { appUsageRepository.todayForegroundUsageMillis() }
            } else {
                emptyMap()
            }
        }
    }

    private fun toRow(
        rule: NotificationRule,
        usage: Map<String, Long>,
        usageGranted: Boolean
    ): AppRowUi {
        val appName = rule.appName.ifBlank {
            installedAppsRepository.appNameFor(rule.packageName)
        }
        val millis = usage[rule.packageName] ?: 0L
        return AppRowUi(
            packageName = rule.packageName,
            appName = appName,
            badge = RuleBadge.CUSTOM,
            enabled = rule.enabled,
            quietHoursLabel = "${QuietHoursEvaluator.formatMinutes(rule.quietStartMinutes)} – ${QuietHoursEvaluator.formatMinutes(rule.quietEndMinutes)}",
            usageLabel = if (usageGranted) appUsageRepository.formatDuration(millis) else null
        )
    }
}
