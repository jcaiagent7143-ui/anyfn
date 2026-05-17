/**
 * OnboardingScreen — three-step intro: explain anyfn, prompt for the
 * Accessibility permission, prompt for the LLM key. Each step is gated on
 * the previous one so the user can't skip the bits that matter.
 */
package dev.anyfn.ui.onboarding

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingScreen(onComplete: () -> Unit, vm: OnboardingViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()
    var apiKey by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "Make every app agent-ready",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            "In 60 seconds, anyfn turns the apps already on your phone into callable tools for any AI agent. " +
                "No code changes. No SDK. Everything runs on this device.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(8.dp))
        Step(
            title = "1. Enable Accessibility",
            body = "anyfn needs Accessibility to read app UIs. It never sends UI content off-device unless you connect an LLM provider.",
            buttonLabel = if (state.accessibilityEnabled) "Enabled" else "Open Accessibility settings",
            done = state.accessibilityEnabled,
            onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            },
        )
        Step(
            title = "2. Paste your Anthropic key",
            body = "Used only for function inference. Or skip this and rely on on-device Gemini Nano if your phone supports it.",
            done = state.hasApiKey,
        ) {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("sk-ant-…") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.saveApiKey(apiKey) },
                enabled = apiKey.startsWith("sk-"),
            ) { Text("Save key") }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onComplete,
            enabled = state.accessibilityEnabled && state.hasApiKey,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Continue") }
        TextButton(onClick = onComplete) { Text("Skip — set up later") }
    }
}

@Composable
private fun Step(
    title: String,
    body: String,
    done: Boolean,
    buttonLabel: String? = null,
    onClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(body, style = MaterialTheme.typography.bodyMedium)
        if (buttonLabel != null && onClick != null) {
            Button(onClick = onClick, enabled = !done) { Text(buttonLabel) }
        }
        content?.invoke()
    }
}
