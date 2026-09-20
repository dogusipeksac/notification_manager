package com.dogusipeksac.notificationscheduler.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogusipeksac.notificationscheduler.data.repository.NotificationRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val ruleRepository: NotificationRuleRepository
) : ViewModel() {

    val hasSeenIntro: StateFlow<Boolean> = ruleRepository.observeHasSeenIntro()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun markIntroSeen() {
        viewModelScope.launch { ruleRepository.setHasSeenIntro(true) }
    }
}
