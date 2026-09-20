package com.dogusipeksac.notification_programming.data.local

/**
 * Kural tanımlanmamış uygulamalar için DataStore'da tutulan varsayılan sessiz saat kuralı.
 */
data class DefaultRule(
    val enabled: Boolean = false,
    val quietStartMinutes: Int = 22 * 60,
    val quietEndMinutes: Int = 8 * 60,
    val action: NotificationAction = NotificationAction.DELAY_AND_SHOW
)
