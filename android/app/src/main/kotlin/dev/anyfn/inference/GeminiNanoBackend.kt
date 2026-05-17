/**
 * GeminiNanoBackend
 *
 * Uses Android 14+ AICore (Gemini Nano) for on-device inference. AICore is
 * not available on every device, so [isReady] returns false unless the
 * underlying provider can be resolved.
 *
 * We deliberately avoid a compile-time dependency on AICore: the SDK is in
 * flux and not every device ships the artefacts. Instead we reflectively
 * probe `com.google.ai.edge.aicore.GenerativeModel` and return a graceful
 * "not ready" if the class isn't present, letting the runtime fall through
 * to Anthropic.
 */
package dev.anyfn.inference

import android.content.Context
import android.util.Log
import dev.anyfn.core.model.AppFunction

class GeminiNanoBackend(
    private val context: Context,
    @Suppress("unused") private val prompts: PromptLibrary,
) : LlmBackend {

    override suspend fun isReady(): Boolean = aiCoreClass != null

    override suspend fun inferFunctions(
        packageName: String,
        appLabel: String,
        uiTreeJson: String,
    ): Result<List<AppFunction>> {
        if (aiCoreClass == null) {
            return Result.failure(IllegalStateException("AICore not available on this device"))
        }
        Log.w(TAG, "Gemini Nano backend is feature-detected but not yet wired in v0.1 — falling through")
        return Result.failure(NotImplementedError("Gemini Nano backend ships in v0.2"))
    }

    private val aiCoreClass: Class<*>? = runCatching {
        Class.forName("com.google.ai.edge.aicore.GenerativeModel")
    }.getOrNull()

    companion object {
        private const val TAG = "anyfn.gemini"
    }
}
