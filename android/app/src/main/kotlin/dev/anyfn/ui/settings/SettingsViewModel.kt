/**
 * SettingsViewModel — pass-through to [SettingsPreferences] with a StateFlow
 * the screen can collect for live updates.
 */
package dev.anyfn.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anyfn.data.preferences.SettingsPreferences
import dev.anyfn.data.preferences.SettingsSnapshot
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: SettingsPreferences,
) : ViewModel() {

    val snapshot: StateFlow<SettingsSnapshot> = prefs.snapshot
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsSnapshot())

    fun setApiKey(v: String) = viewModelScope.launch { prefs.setAnthropicApiKey(v) }
    fun setMcpPort(v: Int) = viewModelScope.launch { prefs.setMcpPort(v) }
    fun setSharedSecret(v: String) = viewModelScope.launch { prefs.setMcpSharedSecret(v) }
    fun setLanMode(v: Boolean) = viewModelScope.launch { prefs.setMcpLanMode(v) }
    fun setConfirmDestructive(v: Boolean) = viewModelScope.launch { prefs.setConfirmDestructive(v) }
    fun setIncludeSystemApps(v: Boolean) = viewModelScope.launch { prefs.setIncludeSystemApps(v) }
    fun setDebugMode(v: Boolean) = viewModelScope.launch { prefs.setDebugMode(v) }
}
