/**
 * OnboardingViewModel — observes Accessibility connectivity and the persisted
 * API key, and lets the user save a new key.
 */
package dev.anyfn.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anyfn.accessibility.AnyfnAccessibilityService
import dev.anyfn.data.preferences.SettingsPreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnboardingState(
    val accessibilityEnabled: Boolean = false,
    val hasApiKey: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: SettingsPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        val poll = flow {
            while (true) {
                emit(AnyfnAccessibilityService.isConnected())
                delay(500L)
            }
        }
        combine(poll, settings.snapshot) { connected, snap ->
            OnboardingState(
                accessibilityEnabled = connected,
                hasApiKey = snap.anthropicApiKey.isNotBlank(),
            )
        }.onEach { _state.value = it }.launchIn(viewModelScope)
    }

    fun saveApiKey(value: String) {
        viewModelScope.launch { settings.setAnthropicApiKey(value.trim()) }
    }
}
