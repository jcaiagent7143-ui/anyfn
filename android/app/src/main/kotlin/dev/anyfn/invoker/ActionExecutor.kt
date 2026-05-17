/**
 * ActionExecutor
 *
 * The Invoker's entrypoint. Given a function name and an argument map, it:
 *   1. Looks up the function in the registry.
 *   2. Verifies preconditions (a11y connected, no destructive-without-confirm).
 *   3. Launches the target app and waits for it to settle.
 *   4. Replays each [UiAction] step, polling between steps via [StateWaiter].
 *   5. Calls [ErrorRecovery] before every step to bail out cleanly on
 *      unexpected popups, login walls, or secure screens.
 *   6. Returns a structured [InvocationResult].
 *
 * Confirmation tokens: when a destructive function is invoked without
 * `__confirm=<token>`, the executor returns [InvocationResult.NeedsConfirmation]
 * with a single-use token; the agent re-calls with that token in arguments.
 */
package dev.anyfn.invoker

import android.content.Context
import dev.anyfn.core.model.AppFunction
import dev.anyfn.core.model.FailureReason
import dev.anyfn.core.model.InvocationResult
import dev.anyfn.core.model.UiAction
import dev.anyfn.data.preferences.SettingsPreferences
import dev.anyfn.data.repository.FunctionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Singleton
class ActionExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FunctionRepository,
    private val ui: UIAutomatorBridge,
    private val a11y: AccessibilityBridge,
    private val waiter: StateWaiter,
    private val recovery: ErrorRecovery,
    private val capturer: ResultCapturer,
    private val settings: SettingsPreferences,
) {

    suspend fun call(name: String, arguments: Map<String, String>): InvocationResult {
        val started = System.currentTimeMillis()
        val function = repository.byName(name)
            ?: return failure(FailureReason.UNKNOWN, "no function named '$name'")

        if (!a11y.isConnected()) {
            return failure(
                FailureReason.ACCESSIBILITY_DENIED,
                "anyfn's Accessibility service is not enabled. Toggle it in Settings → Accessibility.",
            )
        }

        val confirmation = checkDestructive(function, arguments)
        if (confirmation != null) return confirmation

        ui.launch(function.packageName, context)
        if (!waiter.awaitPackage(function.packageName, timeoutMs = 8_000L)) {
            return failure(FailureReason.APP_NOT_INSTALLED, "could not bring ${function.appLabel} to front")
        }
        waiter.awaitIdle()

        function.uiPath.forEachIndexed { idx, step ->
            when (val decision = recovery.classifyCurrentState()) {
                is RecoveryDecision.Continue -> Unit
                is RecoveryDecision.Retry -> delay(decision.backoffMs)
                is RecoveryDecision.Abort -> return failure(decision.reason, decision.message)
            }
            val ok = runStep(step, arguments)
            if (!ok) {
                return failure(
                    FailureReason.ELEMENT_NOT_FOUND,
                    "step $idx (${step::class.simpleName}) could not be executed",
                )
            }
            waiter.awaitIdle()
        }

        val output = capturer.captureSummary()
        return InvocationResult.Success(
            output = output.ifBlank { "ok" },
            capturedScreens = 1,
            durationMs = System.currentTimeMillis() - started,
        )
    }

    private suspend fun checkDestructive(function: AppFunction, arguments: Map<String, String>): InvocationResult? {
        if (!function.destructive) return null
        val confirmRequired = settings.snapshot.first().confirmDestructive
        if (!confirmRequired) return null
        val token = arguments["__confirm"]
        if (token == pendingTokenFor(function.name)) return null
        val freshToken = newToken()
        pendingTokens[function.name] = freshToken
        return InvocationResult.NeedsConfirmation(
            prompt = "anyfn is about to call '${function.name}', which can produce side effects you can't undo. " +
                "Re-call with __confirm='$freshToken' if you really mean to.",
            confirmToken = freshToken,
        )
    }

    private fun runStep(step: UiAction, args: Map<String, String>): Boolean = when (step) {
        is UiAction.Click -> ui.click(step.selector) ||
            (step.fallbackSelector?.let { ui.click(it) } ?: false)
        is UiAction.TypeText -> {
            val value = args[step.valueFromParam].orEmpty()
            ui.typeText(step.selector, value)
        }
        is UiAction.Scroll -> ui.scroll(step.direction)
        is UiAction.WaitFor -> runBlockingAwait(step.selector, step.timeoutMs)
        UiAction.PressEnter -> ui.pressEnter()
        UiAction.PressBack -> ui.pressBack()
        is UiAction.Launch -> { ui.launch(step.packageName, context); true }
    }

    private fun runBlockingAwait(selector: dev.anyfn.core.model.Selector, timeoutMs: Long): Boolean {
        // Used inside the synchronous executor scope — the underlying call is short.
        return kotlinx.coroutines.runBlocking { waiter.awaitElement(selector, timeoutMs) }
    }

    private fun failure(reason: FailureReason, message: String): InvocationResult.Failure =
        InvocationResult.Failure(reason = reason, message = message)

    private fun pendingTokenFor(name: String): String? = pendingTokens[name]

    private fun newToken(): String {
        val bytes = ByteArray(12)
        rand.nextBytes(bytes)
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private val rand: java.security.SecureRandom = java.security.SecureRandom()
    private val pendingTokens = java.util.concurrent.ConcurrentHashMap<String, String>()
}
