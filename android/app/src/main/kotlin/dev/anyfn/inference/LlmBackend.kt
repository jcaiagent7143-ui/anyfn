/**
 * LlmBackend
 *
 * Abstraction over the LLM we ask "what callable things does this app expose?"
 *
 * Both backends share the same prompt and return the same JSON shape so the
 * caller (FunctionInferrer) never has to know which one is active.
 */
package dev.anyfn.inference

import dev.anyfn.core.model.AppFunction

interface LlmBackend {
    /** True if this backend has everything it needs (network, API key, AICore, etc.) to run. */
    suspend fun isReady(): Boolean

    /** Sends the inference prompt with the given UI tree and returns the parsed function list. */
    suspend fun inferFunctions(
        packageName: String,
        appLabel: String,
        uiTreeJson: String,
    ): Result<List<AppFunction>>
}
