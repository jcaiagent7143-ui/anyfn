/**
 * HomeViewModel — observes the registry count and bridge status so the
 * home screen can render its stat pills without reaching into Room itself.
 */
package dev.anyfn.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anyfn.bridge.McpServer
import dev.anyfn.data.repository.FunctionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeState(
    val functionCount: Int = 0,
    val appCount: Int = 0,
    val scanCount: Int = 0,
    val bridgeRunning: Boolean = false,
    val bridgeUrl: String = "",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: FunctionRepository,
    @Suppress("unused") private val mcp: McpServer,
) : ViewModel() {

    val state: StateFlow<HomeState> = repository.observeAll()
        .map { fns ->
            HomeState(
                functionCount = fns.size,
                appCount = fns.map { it.packageName }.toSet().size,
                scanCount = 0,
                bridgeRunning = mcp.isRunning(),
                bridgeUrl = if (mcp.isRunning()) "ws://127.0.0.1:5174/ws" else "",
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HomeState())

    @Suppress("unused") private val _placeholder = MutableStateFlow(Unit)
}
