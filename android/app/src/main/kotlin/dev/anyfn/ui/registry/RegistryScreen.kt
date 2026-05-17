/**
 * RegistryScreen — list of discovered functions grouped by app, with a
 * confidence badge per row.
 */
package dev.anyfn.ui.registry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.anyfn.core.model.AppFunction

@Composable
fun RegistryScreen(
    onFunctionTap: (Long) -> Unit,
    onBack: () -> Unit,
    vm: RegistryViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Registry", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("Back") }
        }
        if (state.functions.isEmpty()) {
            Text(
                "Nothing here yet. Run a scan from the home screen.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 12.dp),
            ) {
                items(state.functions, key = { it.name }) { fn -> FunctionRow(fn, onFunctionTap) }
            }
        }
    }
}

@Composable
private fun FunctionRow(fn: AppFunction, onTap: (Long) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(fn.name, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
            Text(
                "${fn.appLabel} • confidence ${"%.2f".format(fn.confidence)}" +
                    if (fn.destructive) " • destructive" else "",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(fn.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
            TextButton(onClick = { onTap(fn.hashCode().toLong()) }, modifier = Modifier.padding(top = 4.dp)) {
                Text("Open playground")
            }
        }
    }
}
