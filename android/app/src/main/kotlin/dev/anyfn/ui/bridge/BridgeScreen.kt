/**
 * BridgeScreen — start/stop the MCP server and show copy-pasteable
 * connection snippets.
 */
package dev.anyfn.ui.bridge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.anyfn.ui.common.SectionCard

@Composable
fun BridgeScreen(onBack: () -> Unit, vm: BridgeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val clipboard: ClipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bridge", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("Back") }
        }
        Text(
            if (state.running) "Running on ${state.endpoint}" else "Stopped",
            style = MaterialTheme.typography.bodyLarge,
            color = if (state.running) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.running) {
                OutlinedButton(onClick = vm::stop) { Text("Stop") }
            } else {
                Button(onClick = vm::start) { Text("Start") }
            }
        }
        SectionCard(title = "Claude Desktop config") {
            Text(state.claudeConfig, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(state.claudeConfig)) }) { Text("Copy") }
        }
        SectionCard(title = "adb forward") {
            Text(state.adbCommand, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(state.adbCommand)) }) { Text("Copy") }
        }
        SectionCard(title = "Self-test (curl)") {
            Text(state.curlCommand, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(state.curlCommand)) }) { Text("Copy") }
        }
        state.appFunctionsStatus?.let { Text("Android 16 AppFunctions: $it", style = MaterialTheme.typography.labelLarge) }
    }
}
