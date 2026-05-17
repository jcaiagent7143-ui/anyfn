/**
 * InvocationResult — the structured outcome of a single function call.
 *
 * Modeled as a sealed hierarchy so callers handle each shape explicitly
 * rather than parsing free-text errors.
 */
package dev.anyfn.core.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface InvocationResult {
    @Serializable
    data class Success(
        val output: String,
        val capturedScreens: Int = 0,
        val durationMs: Long = 0L,
    ) : InvocationResult

    @Serializable
    data class Failure(
        val reason: FailureReason,
        val message: String,
        val recoveryHint: String? = null,
    ) : InvocationResult

    @Serializable
    data class NeedsConfirmation(
        val prompt: String,
        val confirmToken: String,
    ) : InvocationResult
}

@Serializable
enum class FailureReason {
    APP_NOT_INSTALLED,
    ACCESSIBILITY_DENIED,
    ELEMENT_NOT_FOUND,
    STATE_TIMEOUT,
    UNEXPECTED_DIALOG,
    NETWORK_ERROR,
    PERMISSION_DENIED,
    LOGIN_REQUIRED,
    SECURE_SCREEN_BLOCKED,
    UNKNOWN,
}
