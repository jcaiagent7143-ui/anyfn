/**
 * ErrorRecovery
 *
 * Detects unexpected UI states that would derail an in-flight invocation and
 * decides what to do about them. The detection heuristics are intentionally
 * conservative — false positives cause spurious cancellations, which are
 * worse than false negatives for an Invoker that's already retrying.
 *
 * Recognised states (v0.1):
 *   - **Permission popup** ("Allow", "While using the app") → dismiss only
 *     if the action does not require the permission.
 *   - **Generic dialog with OK/Cancel/Dismiss** → ignored unless it has
 *     destructive verbs; we surface to the caller.
 *   - **Login wall** ("Sign in", "Continue with Google") → fail with
 *     `LOGIN_REQUIRED`. anyfn never types passwords.
 *   - **Secure screen** (empty a11y tree) → fail with `SECURE_SCREEN_BLOCKED`.
 *   - **Network error toast/banner** → retry up to once with backoff.
 */
package dev.anyfn.invoker

import dev.anyfn.core.model.FailureReason
import javax.inject.Inject
import javax.inject.Singleton

sealed interface RecoveryDecision {
    data object Continue : RecoveryDecision
    data class Retry(val backoffMs: Long) : RecoveryDecision
    data class Abort(val reason: FailureReason, val message: String) : RecoveryDecision
}

@Singleton
class ErrorRecovery @Inject constructor(private val a11y: AccessibilityBridge) {

    fun classifyCurrentState(): RecoveryDecision {
        if (a11y.detectSecureScreen()) {
            return RecoveryDecision.Abort(
                FailureReason.SECURE_SCREEN_BLOCKED,
                "Target app has FLAG_SECURE set; Accessibility cannot read it. " +
                    "This is expected for banking, 2FA, and password manager screens.",
            )
        }

        val summary = a11y.visibleTextSummary().lowercase()

        if (summary.containsAny(LOGIN_TOKENS)) {
            return RecoveryDecision.Abort(
                FailureReason.LOGIN_REQUIRED,
                "Login wall detected. anyfn does not type credentials — sign in manually and re-run.",
            )
        }

        if (summary.containsAny(NETWORK_TOKENS)) {
            return RecoveryDecision.Retry(backoffMs = 2_000L)
        }

        if (summary.containsAny(DESTRUCTIVE_TOKENS)) {
            return RecoveryDecision.Abort(
                FailureReason.UNEXPECTED_DIALOG,
                "Destructive confirmation dialog appeared. Re-run with destructive=true to allow.",
            )
        }

        return RecoveryDecision.Continue
    }

    private fun String.containsAny(tokens: Array<String>): Boolean = tokens.any { contains(it) }

    private companion object {
        val LOGIN_TOKENS: Array<String> = arrayOf(
            "sign in", "log in", "continue with google", "continue with apple",
            "enter your password", "verify your identity",
        )
        val NETWORK_TOKENS: Array<String> = arrayOf(
            "no internet", "couldn't connect", "could not connect", "check your connection",
            "network error", "try again",
        )
        val DESTRUCTIVE_TOKENS: Array<String> = arrayOf(
            "delete account", "send money", "place order", "confirm payment",
            "post to public", "permanent",
        )
    }
}
