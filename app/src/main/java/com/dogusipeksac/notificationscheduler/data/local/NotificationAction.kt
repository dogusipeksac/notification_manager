package com.dogusipeksac.notificationscheduler.data.local

/**
 * Sessiz saatlerde bildirime uygulanacak aksiyon.
 *
 * [BLOCK]: Bildirimi tamamen bastır, saklama.
 * [DELAY_AND_SHOW]: Bildirimi gizle, içeriği kaydet, sessiz saat bitince yeniden göster.
 */
enum class NotificationAction {
    BLOCK,
    DELAY_AND_SHOW
}
