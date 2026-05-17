/**
 * StateWaiter
 *
 * Coordinates "wait until the UI is doing what we expect" between two
 * different sources of truth:
 *   - UI Automator's `waitForIdle` (good signal for the active window)
 *   - the live Accessibility tree (good signal for slower content changes)
 *
 * Most actions only need [awaitIdle]. [awaitElement] is the surface area
 * the executor uses to verify pre/post-conditions on a step.
 */
package dev.anyfn.invoker

import dev.anyfn.core.model.Selector
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class StateWaiter @Inject constructor(
    private val ui: UIAutomatorBridge,
    private val a11y: AccessibilityBridge,
) {

    suspend fun awaitIdle(timeoutMs: Long = 3_000L) {
        ui.waitForIdle(timeoutMs)
    }

    suspend fun awaitElement(selector: Selector, timeoutMs: Long = 5_000L): Boolean {
        val result = withTimeoutOrNull(timeoutMs) {
            while (true) {
                if (ui.find(selector, 300L) != null) return@withTimeoutOrNull true
                if (a11y.findFirst(selector) != null) return@withTimeoutOrNull true
                delay(POLL_MS)
            }
            @Suppress("UNREACHABLE_CODE") false
        }
        return result ?: false
    }

    suspend fun awaitPackage(packageName: String, timeoutMs: Long = 5_000L): Boolean {
        val ok = withTimeoutOrNull(timeoutMs) {
            while (ui.currentPackage() != packageName) delay(POLL_MS)
            true
        }
        return ok == true
    }

    companion object {
        private const val POLL_MS: Long = 250L
    }
}
