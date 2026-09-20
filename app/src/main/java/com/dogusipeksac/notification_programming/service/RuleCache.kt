package com.dogusipeksac.notification_programming.service

import com.dogusipeksac.notification_programming.data.local.DefaultRule
import com.dogusipeksac.notification_programming.data.local.NotificationRule
import com.dogusipeksac.notification_programming.data.repository.NotificationRuleRepository
import com.dogusipeksac.notification_programming.domain.RuleResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * onNotificationPosted her tetiklendiğinde Room/DataStore I/O yapmamak için
 * kuralları bellekte tutan cache. Uygulama açılışında senkron yüklenir,
 * sonrasında Flow ile güncel tutulur.
 */
@Singleton
class RuleCache @Inject constructor(
    private val repository: NotificationRuleRepository
) {
    @Volatile
    private var rulesByPackage: Map<String, NotificationRule> = emptyMap()

    @Volatile
    private var defaultRule: DefaultRule = DefaultRule()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun loadNow() {
        rulesByPackage = repository.getAllRules().associateBy { it.packageName }
        defaultRule = repository.getDefaultRule()
    }

    fun startObserving() {
        repository.observeRules()
            .onEach { list -> rulesByPackage = list.associateBy { it.packageName } }
            .launchIn(scope)
        repository.observeDefaultRule()
            .onEach { defaultRule = it }
            .launchIn(scope)
    }

    fun resolve(packageName: String): NotificationRule? {
        return RuleResolver.resolve(packageName, rulesByPackage, defaultRule)
    }
}
