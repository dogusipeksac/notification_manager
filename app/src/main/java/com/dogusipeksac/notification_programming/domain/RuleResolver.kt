package com.dogusipeksac.notification_programming.domain

import com.dogusipeksac.notification_programming.data.local.DefaultRule
import com.dogusipeksac.notification_programming.data.local.NotificationRule

/**
 * Uygulamaya özel kural yoksa varsayılan kurala düşer.
 * Özel kural `enabled = false` ise varsayılan uygulanmaz (bu uygulama muaf).
 */
object RuleResolver {
    fun resolve(
        packageName: String,
        rulesByPackage: Map<String, NotificationRule>,
        defaultRule: DefaultRule
    ): NotificationRule? {
        val specific = rulesByPackage[packageName]
        if (specific != null) {
            return specific.takeIf { it.enabled }
        }
        if (!defaultRule.enabled) return null
        return NotificationRule(
            packageName = packageName,
            appName = "",
            enabled = true,
            quietStartMinutes = defaultRule.quietStartMinutes,
            quietEndMinutes = defaultRule.quietEndMinutes,
            action = defaultRule.action
        )
    }
}
