package com.dogusipeksac.notificationscheduler.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NotificationListenerService bağlandı/koptu durumunu ayarlar ekranına yansıtır.
 */
@Singleton
class ListenerConnectionState @Inject constructor() {
    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    fun setConnected(value: Boolean) {
        _connected.value = value
    }
}
