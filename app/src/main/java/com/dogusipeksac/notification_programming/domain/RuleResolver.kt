package com.dogusipeksac.notification_programming.domain

import com.dogusipeksac.notification_programming.data.local.DefaultRule
import com.dogusipeksac.notification_programming.data.local.NotificationRule

/**
 * Yalnızca kullanıcı tarafından eklenmiş (ve açık) uygulama kurallarını uygular.
 * Varsayılan kural yalnızca yeni uygulama eklerken şablon olarak kullanılır.
 */
object RuleResolver {
    fun resolve(
        packageName: String,
        rulesByPackage: Map<String, NotificationRule>,
        @Suppress("UNUSED_PARAMETER") defaultRule: DefaultRule
    ): NotificationRule? {
        return rulesByPackage[packageName]?.takeIf { it.enabled }
    }
}
