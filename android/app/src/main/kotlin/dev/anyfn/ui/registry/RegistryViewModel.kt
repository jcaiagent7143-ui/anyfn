/**
 * RegistryViewModel — observes the full function registry.
 */
package dev.anyfn.ui.registry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anyfn.core.model.AppFunction
import dev.anyfn.data.repository.FunctionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RegistryState(val functions: List<AppFunction> = emptyList())

@HiltViewModel
class RegistryViewModel @Inject constructor(
    repository: FunctionRepository,
) : ViewModel() {

    val state: StateFlow<RegistryState> = repository.observeAll()
        .map { RegistryState(functions = it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RegistryState())
}
