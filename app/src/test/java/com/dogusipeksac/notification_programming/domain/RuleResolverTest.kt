package com.dogusipeksac.notification_programming.domain

import com.dogusipeksac.notification_programming.data.local.DefaultRule
import com.dogusipeksac.notification_programming.data.local.NotificationAction
import com.dogusipeksac.notification_programming.data.local.NotificationRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RuleResolverTest {

    private val defaultRule = DefaultRule(
        enabled = true,
        quietStartMinutes = 22 * 60,
        quietEndMinutes = 8 * 60,
        action = NotificationAction.DELAY_AND_SHOW
    )

    @Test
    fun specificEnabledRule_isApplied() {
        val specific = rule("com.whatsapp", enabled = true, action = NotificationAction.BLOCK)
        val resolved = RuleResolver.resolve("com.whatsapp", mapOf(specific.packageName to specific), defaultRule)
        assertNotNull(resolved)
        assertEquals(NotificationAction.BLOCK, resolved!!.action)
        assertEquals(specific.quietStartMinutes, resolved.quietStartMinutes)
    }

    @Test
    fun specificDisabledRule_isIgnored() {
        val specific = rule("com.whatsapp", enabled = false, action = NotificationAction.BLOCK)
        val resolved = RuleResolver.resolve("com.whatsapp", mapOf(specific.packageName to specific), defaultRule)
        assertNull(resolved)
    }

    @Test
    fun missingRule_doesNotUseDefault() {
        val resolved = RuleResolver.resolve("com.whatsapp", emptyMap(), defaultRule)
        assertNull(resolved)
    }

    private fun rule(
        packageName: String,
        enabled: Boolean,
        action: NotificationAction
    ) = NotificationRule(
        packageName = packageName,
        appName = "WhatsApp",
        enabled = enabled,
        quietStartMinutes = 21 * 60,
        quietEndMinutes = 7 * 60,
        action = action
    )
}
