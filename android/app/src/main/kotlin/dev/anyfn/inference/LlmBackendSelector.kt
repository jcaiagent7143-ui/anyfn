/**
 * LlmBackendSelector
 *
 * Routes inference calls to the backend the user selected in settings.
 * If the chosen backend isn't ready (no API key, no AICore), falls through
 * to whichever backend is.
 */
package dev.anyfn.inference

import dev.anyfn.core.model.AppFunction
import dev.anyfn.data.preferences.LlmProvider
import dev.anyfn.data.preferences.SettingsPreferences
import kotlinx.coroutines.flow.first

class LlmBackendSelector(
    private val settings: SettingsPreferences,
    private val anthropic: AnthropicBackend,
    private val gemini: GeminiNanoBackend,
) : LlmBackend {

    override suspend fun isReady(): Boolean = pickPrimary().isReady() || pickFallback().isReady()

    override suspend fun inferFunctions(
        packageName: String,
        appLabel: String,
        uiTreeJson: String,
    ): Result<List<AppFunction>> {
        syncCredentials()
        val primary = pickPrimary()
        if (primary.isReady()) {
            val r = primary.inferFunctions(packageName, appLabel, uiTreeJson)
            if (r.isSuccess) return r
        }
        val fallback = pickFallback()
        if (fallback.isReady() && fallback !== primary) {
            return fallback.inferFunctions(packageName, appLabel, uiTreeJson)
        }
        return Result.failure(IllegalStateException("no LLM backend is ready"))
    }

    private suspend fun pickPrimary(): LlmBackend = when (settings.snapshot.first().provider) {
        LlmProvider.ANTHROPIC -> anthropic
        LlmProvider.GEMINI_NANO -> gemini
    }

    private suspend fun pickFallback(): LlmBackend = when (settings.snapshot.first().provider) {
        LlmProvider.ANTHROPIC -> gemini
        LlmProvider.GEMINI_NANO -> anthropic
    }

    private suspend fun syncCredentials() {
        anthropic.setApiKey(settings.snapshot.first().anthropicApiKey)
    }
}
