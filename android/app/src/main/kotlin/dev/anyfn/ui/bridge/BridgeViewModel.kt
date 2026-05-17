/**
 * BridgeViewModel — owns the start/stop lifecycle of the MCP foreground
 * service and exposes the copy snippets the screen renders.
 */
package dev.anyfn.ui.bridge

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.anyfn.bridge.AdbBridge
import dev.anyfn.bridge.AppFunctionsExporter
import dev.anyfn.bridge.McpForegroundService
import dev.anyfn.bridge.McpServer
import dev.anyfn.data.preferences.SettingsPreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class BridgeState(
    val running: Boolean = false,
    val endpoint: String = "",
    val claudeConfig: String = "",
    val adbCommand: String = "",
    val curlCommand: String = "",
    val appFunctionsStatus: String? = null,
)

@HiltViewModel
class BridgeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val server: McpServer,
    private val adb: AdbBridge,
    private val exporter: AppFunctionsExporter,
    private val settings: SettingsPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(BridgeState())
    val state: StateFlow<BridgeState> = _state.asStateFlow()

    init {
        viewModelScope.launch { refresh() }
    }

    fun start() {
        context.startForegroundService(Intent(context, McpForegroundService::class.java))
        viewModelScope.launch {
            // give the service a beat to actually bind & start the engine
            kotlinx.coroutines.delay(400L)
            refresh()
        }
    }

    fun stop() {
        context.stopService(Intent(context, McpForegroundService::class.java))
        viewModelScope.launch {
            kotlinx.coroutines.delay(200L)
            refresh()
        }
    }

    private suspend fun refresh() {
        val port = settings.snapshot.first().mcpPort
        val running = server.isRunning()
        val statusLabel = when (val r = exporter.publishAll()) {
            is AppFunctionsExporter.PublishResult.Published -> "published ${r.count} functions"
            is AppFunctionsExporter.PublishResult.NotSupported -> r.reason
            is AppFunctionsExporter.PublishResult.Failed -> "failed: ${r.message}"
        }
        _state.value = BridgeState(
            running = running,
            endpoint = "ws://127.0.0.1:$port/ws",
            claudeConfig = adb.claudeDesktopConfig(port),
            adbCommand = adb.adbForwardCommand(port),
            curlCommand = adb.curlSelfTest(port),
            appFunctionsStatus = statusLabel,
        )
    }
}
