/**
 * PlaygroundScreen — view a function's schema and ui_path, fill in inputs,
 * fire a real call, and watch the result come back.
 */
package dev.anyfn.ui.playground

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PlaygroundScreen(
    functionId: Long,
    onBack: () -> Unit,
    vm: PlaygroundViewModel = hiltViewModel(),
) {
    LaunchedEffect(functionId) { vm.load(functionId) }
    val state by vm.state.collectAsState()
    val inputs = remember { mutableStateMapOf<String, String>() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Text("← Back") }
        val fn = state.function
        if (fn == null) {
            Text("Loading…")
        } else {
            Text(fn.name, style = MaterialTheme.typography.headlineLarge, fontFamily = FontFamily.Monospace)
            Text(fn.description, style = MaterialTheme.typography.bodyLarge)
            fn.parameters.forEach { p ->
                OutlinedTextField(
                    value = inputs[p.name].orEmpty(),
                    onValueChange = { inputs[p.name] = it },
                    label = { Text("${p.name} (${p.type.name.lowercase()})") },
                    supportingText = { Text(p.description) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Button(
                onClick = { vm.invoke(fn.name, inputs.toMap()) },
                enabled = !state.running,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (state.running) "Running…" else "Run") }
            Spacer(Modifier.height(8.dp))
            state.output?.let {
                Text("Result", style = MaterialTheme.typography.titleMedium)
                Text(it, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
            }
            state.error?.let {
                Text("Error", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
