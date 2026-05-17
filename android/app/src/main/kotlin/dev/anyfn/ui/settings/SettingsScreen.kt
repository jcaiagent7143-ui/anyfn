/**
 * SettingsScreen — adjust API keys, MCP port + secret, confirmation policy,
 * and debug knobs.
 */
package dev.anyfn.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.anyfn.ui.common.SectionCard

@Composable
fun SettingsScreen(onBack: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val s by vm.snapshot.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Settings", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("Back") }
        }
        SectionCard(title = "LLM provider") {
            OutlinedTextField(
                value = s.anthropicApiKey,
                onValueChange = vm::setApiKey,
                label = { Text("Anthropic API key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        SectionCard(title = "Bridge") {
            OutlinedTextField(
                value = s.mcpPort.toString(),
                onValueChange = { vm.setMcpPort(it.toIntOrNull() ?: s.mcpPort) },
                label = { Text("Port") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = s.mcpSharedSecret,
                onValueChange = vm::setSharedSecret,
                label = { Text("Shared secret (LAN mode)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            ToggleRow("Allow LAN connections", s.mcpLanMode, vm::setLanMode)
        }
        SectionCard(title = "Safety") {
            ToggleRow("Require confirmation for destructive actions", s.confirmDestructive, vm::setConfirmDestructive)
        }
        SectionCard(title = "Discovery") {
            ToggleRow("Include system apps", s.includeSystemApps, vm::setIncludeSystemApps)
        }
        SectionCard(title = "Debug") {
            ToggleRow("Dump UI trees to /sdcard", s.debugMode, vm::setDebugMode)
        }
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = value, onCheckedChange = onChange)
    }
}
