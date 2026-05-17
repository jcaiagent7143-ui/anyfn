/**
 * HomeScreen — the user's launchpad. Shows the function count, the bridge
 * status pill, and the four primary CTAs (scan, registry, bridge, settings).
 */
package dev.anyfn.ui.home

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.anyfn.ui.common.SectionCard
import dev.anyfn.ui.common.StatPill

@Composable
fun HomeScreen(
    onScan: () -> Unit,
    onRegistry: () -> Unit,
    onBridge: () -> Unit,
    onSettings: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("anyfn", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Text(
            "Every app on this phone, callable.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionCard(title = "Registry") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatPill(label = "functions", value = state.functionCount.toString())
                StatPill(label = "apps", value = state.appCount.toString())
                StatPill(label = "scans", value = state.scanCount.toString())
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onRegistry, modifier = Modifier.fillMaxWidth()) {
                Text("View registry")
            }
        }
        SectionCard(title = "Bridge") {
            Text(
                if (state.bridgeRunning) "Running on ${state.bridgeUrl}" else "Stopped",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onBridge, modifier = Modifier.fillMaxWidth()) {
                Text("Bridge status")
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onScan, modifier = Modifier.fillMaxWidth()) { Text("Scan device") }
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
    }
}
