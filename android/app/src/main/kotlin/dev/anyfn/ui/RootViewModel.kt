/**
 * RootViewModel — exposes the small slice of global state the nav graph needs
 * (currently just onboarding completion). Anything richer should live on a
 * per-screen ViewModel rather than ballooning this one.
 */
package dev.anyfn.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anyfn.data.preferences.OnboardingPreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RootState(val onboardingComplete: Boolean = false)

@HiltViewModel
class RootViewModel @Inject constructor(
    private val onboardingPrefs: OnboardingPreferences,
) : ViewModel() {

    val state: StateFlow<RootState> = onboardingPrefs.completed
        .map { RootState(onboardingComplete = it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RootState())

    fun markOnboardingComplete() {
        viewModelScope.launch { onboardingPrefs.setCompleted(true) }
    }
}
