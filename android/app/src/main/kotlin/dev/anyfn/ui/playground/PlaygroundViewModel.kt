/**
 * PlaygroundViewModel — loads a function by id and invokes it via [ActionExecutor].
 */
package dev.anyfn.ui.playground

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anyfn.core.model.AppFunction
import dev.anyfn.core.model.InvocationResult
import dev.anyfn.data.repository.FunctionRepository
import dev.anyfn.invoker.ActionExecutor
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PlaygroundState(
    val function: AppFunction? = null,
    val running: Boolean = false,
    val output: String? = null,
    val error: String? = null,
)

@HiltViewModel
class PlaygroundViewModel @Inject constructor(
    private val repository: FunctionRepository,
    private val executor: ActionExecutor,
) : ViewModel() {

    private val _state = MutableStateFlow(PlaygroundState())
    val state: StateFlow<PlaygroundState> = _state.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            val fn = repository.observeAll().first()
                .firstOrNull { it.hashCode().toLong() == id }
            _state.value = _state.value.copy(function = fn)
        }
    }

    fun invoke(name: String, args: Map<String, String>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(running = true, output = null, error = null)
            when (val r = executor.call(name, args)) {
                is InvocationResult.Success -> _state.value = _state.value.copy(
                    running = false,
                    output = r.output,
                )
                is InvocationResult.Failure -> _state.value = _state.value.copy(
                    running = false,
                    error = "[${r.reason.name}] ${r.message}",
                )
                is InvocationResult.NeedsConfirmation -> _state.value = _state.value.copy(
                    running = false,
                    error = "needs __confirm=${r.confirmToken}: ${r.prompt}",
                )
            }
        }
    }
}
