package com.dogusipeksac.notification_programming.ui.addapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogusipeksac.notification_programming.data.local.NotificationRule
import com.dogusipeksac.notification_programming.data.repository.InstalledApp
import com.dogusipeksac.notification_programming.data.repository.InstalledAppsRepository
import com.dogusipeksac.notification_programming.data.repository.NotificationRuleRepository
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

enum class AddAppsFilter { USER, ALL }

data class SelectableAppUi(
    val packageName: String,
    val appName: String,
    val selected: Boolean
)

data class AddAppsUiState(
    val query: String = "",
    val filter: AddAppsFilter = AddAppsFilter.USER,
    val isLoading: Boolean = true,
    val apps: List<SelectableAppUi> = emptyList()
)

private data class AddAppsQuery(
    val query: String,
    val apps: List<InstalledApp>,
    val isLoading: Boolean,
    val filter: AddAppsFilter
)

@HiltViewModel
class AddAppsViewModel @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    private val ruleRepository: NotificationRuleRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val installed = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val loading = MutableStateFlow(true)
    private val filter = MutableStateFlow(AddAppsFilter.USER)

    val uiState: StateFlow<AddAppsUiState> = combine(
        combine(query, installed, loading, filter) { q, apps, isLoading, f ->
            AddAppsQuery(q, apps, isLoading, f)
        },
        ruleRepository.observeRules()
    ) { listQuery, rules ->
        val selected = rules.map { it.packageName }.toSet()
        val filtered = listQuery.apps
            .filter { app ->
                when (listQuery.filter) {
                    AddAppsFilter.USER -> !app.isSystem || app.hasLauncher
                    AddAppsFilter.ALL -> true
                }
            }
            .filter { app ->
                listQuery.query.isBlank() ||
                    app.appName.contains(listQuery.query, ignoreCase = true) ||
                    app.packageName.contains(listQuery.query, ignoreCase = true)
            }
            .map { app ->
                SelectableAppUi(
                    packageName = app.packageName,
                    appName = app.appName,
                    selected = app.packageName in selected
                )
            }
            .sortedWith(
                compareByDescending<SelectableAppUi> { it.selected }
                    .thenBy { it.appName.lowercase() }
            )
        AddAppsUiState(
            query = listQuery.query,
            filter = listQuery.filter,
            isLoading = listQuery.isLoading,
            apps = filtered
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddAppsUiState())

    init {
        refresh()
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: AddAppsFilter) {
        filter.value = value
    }

    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            installed.value = withContext(Dispatchers.IO) {
                installedAppsRepository.loadInstalledApps()
            }
            loading.value = false
        }
    }

    fun onToggle(app: SelectableAppUi) {
        viewModelScope.launch {
            if (app.selected) {
                ruleRepository.deleteRule(app.packageName)
            } else {
                val default = ruleRepository.getDefaultRule()
                ruleRepository.upsertRule(
                    NotificationRule(
                        packageName = app.packageName,
                        appName = app.appName,
                        enabled = true,
                        quietStartMinutes = default.quietStartMinutes,
                        quietEndMinutes = default.quietEndMinutes,
                        action = default.action
                    )
                )
            }
        }
    }
}
