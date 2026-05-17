/**
 * ScannerScreen — kicks off [ScanScheduler] and renders progress.
 */
package dev.anyfn.ui.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import dev.anyfn.scanner.ScanProgress
import dev.anyfn.ui.common.ProgressRow

@Composable
fun ScannerScreen(onDone: () -> Unit, vm: ScannerViewModel = hiltViewModel()) {
    val progress by vm.progress.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Scan", style = MaterialTheme.typography.headlineLarge)
        when (val p = progress) {
            is ScanProgress.Idle -> {
                Text("Tap below to scan every user-installed app.")
                Button(onClick = vm::startScan) { Text("Start scan") }
            }
            is ScanProgress.Running -> {
                Text("Scanning ${p.currentApp}…", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                ProgressRow(current = p.currentIndex, total = p.totalApps, label = "Apps")
                Text(
                    "${p.functionsSoFar} functions discovered so far",
                    style = MaterialTheme.typography.labelLarge,
                )
                OutlinedButton(onClick = vm::cancel) { Text("Cancel") }
            }
            is ScanProgress.Done -> {
                Text(
                    "Done. ${p.functionsFound} functions across ${p.appsScanned} apps.",
                    style = MaterialTheme.typography.titleLarge,
                )
                if (p.errors.isNotEmpty()) {
                    Text(
                        "Issues:\n" + p.errors.joinToString("\n"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Back to home") }
            }
            is ScanProgress.Failed -> {
                Text("Scan failed: ${p.message}", color = MaterialTheme.colorScheme.error)
                Button(onClick = vm::startScan) { Text("Retry") }
            }
        }
    }
}
