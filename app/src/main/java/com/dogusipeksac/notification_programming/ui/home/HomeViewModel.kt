package com.dogusipeksac.notification_programming.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogusipeksac.notification_programming.data.local.DefaultRule
import com.dogusipeksac.notification_programming.data.local.NotificationRule
import com.dogusipeksac.notification_programming.data.repository.InstalledApp
import com.dogusipeksac.notification_programming.data.repository.InstalledAppsRepository
import com.dogusipeksac.notification_programming.data.repository.NotificationRuleRepository
import com.dogusipeksac.notification_programming.domain.QuietHoursEvaluator
import com.dogusipeksac.notification_programming.domain.RuleResolver
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

enum class AppListFilter { USER, ALL }

data class AppRowUi(
    val packageName: String,
    val appName: String,
    val badge: RuleBadge,
    val enabled: Boolean,
    val quietHoursLabel: String?
)

data class HomeUiState(
    val query: String = "",
    val filter: AppListFilter = AppListFilter.USER,
    val isLoading: Boolean = true,
    val apps: List<AppRowUi> = emptyList(),
    val visibleCount: Int = 0,
    val totalCount: Int = 0
)

private data class HomeListQuery(
    val query: String,
    val apps: List<InstalledApp>,
    val isLoading: Boolean,
    val filter: AppListFilter
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    private val ruleRepository: NotificationRuleRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val installed = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val loading = MutableStateFlow(true)
    private val filter = MutableStateFlow(AppListFilter.USER)

    val uiState: StateFlow<HomeUiState> = combine(
        combine(query, installed, loading, filter) { q, apps, isLoading, f ->
            HomeListQuery(q, apps, isLoading, f)
        },
        ruleRepository.observeRules(),
        ruleRepository.observeDefaultRule()
    ) { listQuery, rules, defaultRule ->
        val byPackage = rules.associateBy { it.packageName }
        val filteredInstalled = listQuery.apps.filter { app ->
            when (listQuery.filter) {
                AppListFilter.USER -> !app.isSystem || app.hasLauncher
                AppListFilter.ALL -> true
            }
        }
        val rows = filteredInstalled
            .filter { app ->
                listQuery.query.isBlank() ||
                    app.appName.contains(listQuery.query, ignoreCase = true) ||
                    app.packageName.contains(listQuery.query, ignoreCase = true)
            }
            .map { app -> toRow(app, byPackage, defaultRule) }
            .sortedWith(
                compareByDescending<AppRowUi> { it.badge == RuleBadge.CUSTOM }
                    .thenByDescending { it.enabled }
                    .thenBy { it.appName.lowercase() }
            )
        HomeUiState(
            query = listQuery.query,
            filter = listQuery.filter,
            isLoading = listQuery.isLoading,
            apps = rows,
            visibleCount = rows.size,
            totalCount = filteredInstalled.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        refreshApps()
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: AppListFilter) {
        filter.value = value
    }

    fun refreshApps() {
        viewModelScope.launch {
            loading.value = true
            installed.value = withContext(Dispatchers.IO) {
                installedAppsRepository.loadInstalledApps()
            }
            loading.value = false
        }
    }

    fun onToggle(row: AppRowUi, enabled: Boolean) {
        viewModelScope.launch {
            val existing = ruleRepository.getRule(row.packageName)
            val default = ruleRepository.getDefaultRule()
            if (existing != null) {
                ruleRepository.upsertRule(existing.copy(enabled = enabled))
                return@launch
            }
            ruleRepository.upsertRule(
                NotificationRule(
                    packageName = row.packageName,
                    appName = row.appName,
                    enabled = enabled,
                    quietStartMinutes = default.quietStartMinutes,
                    quietEndMinutes = default.quietEndMinutes,
                    action = default.action
                )
            )
        }
    }

    private fun toRow(
        app: InstalledApp,
        rulesByPackage: Map<String, NotificationRule>,
        defaultRule: DefaultRule
    ): AppRowUi {
        val specific = rulesByPackage[app.packageName]
        val resolved = RuleResolver.resolve(app.packageName, rulesByPackage, defaultRule)
        val badge = when {
            specific?.enabled == true -> RuleBadge.CUSTOM
            specific != null -> RuleBadge.NONE
            resolved != null -> RuleBadge.DEFAULT
            else -> RuleBadge.NONE
        }
        val hoursSource = specific ?: resolved
        val quietHoursLabel = hoursSource?.let {
            "${QuietHoursEvaluator.formatMinutes(it.quietStartMinutes)} – ${QuietHoursEvaluator.formatMinutes(it.quietEndMinutes)}"
        }
        return AppRowUi(
            packageName = app.packageName,
            appName = app.appName,
            badge = badge,
            enabled = resolved != null,
            quietHoursLabel = quietHoursLabel
        )
    }
}
