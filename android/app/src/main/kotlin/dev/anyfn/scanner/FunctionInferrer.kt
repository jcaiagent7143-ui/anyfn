/**
 * FunctionInferrer
 *
 * Orchestrates a single app's discovery loop:
 *   1. Wait for the AccessibilityService to be connected.
 *   2. Launch the target app.
 *   3. Subscribe to window snapshots; collect up to [MAX_SNAPSHOTS].
 *   4. Hand each snapshot to UITreeExtractor → LLM → AppFunction list.
 *   5. Deduplicate by name and return the merged result.
 *
 * Errors are returned as a [Result] — never thrown across the module boundary.
 */
package dev.anyfn.scanner

import android.accessibilityservice.AccessibilityService
import android.util.Log
import dev.anyfn.accessibility.AnyfnAccessibilityService
import dev.anyfn.core.model.AppFunction
import dev.anyfn.inference.LlmBackend
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class FunctionInferrer @Inject constructor(
    private val discovery: AppDiscovery,
    private val extractor: UITreeExtractor,
    private val backend: LlmBackend,
) {

    suspend fun inferFor(app: InstalledApp): Result<List<AppFunction>> = runCatching {
        check(AnyfnAccessibilityService.isConnected()) {
            "Accessibility service is not connected — anyfn cannot read app UIs"
        }
        check(backend.isReady()) { "LLM backend is not ready" }

        discovery.launch(app)
        delay(LAUNCH_SETTLE_MS)

        val snapshots = collectSnapshots(app.packageName)
        if (snapshots.isEmpty()) error("no UI snapshots captured for ${app.packageName}")

        val collected = mutableMapOf<String, AppFunction>()
        snapshots.forEach { snapshot ->
            val res = backend.inferFunctions(
                packageName = app.packageName,
                appLabel = app.label,
                uiTreeJson = snapshot,
            )
            res.getOrNull()?.forEach { fn -> collected.putIfAbsent(fn.name, fn) }
            res.exceptionOrNull()?.let { Log.w(TAG, "inference error: ${it.message}") }
        }
        collected.values.sortedByDescending { it.confidence }
    }

    private suspend fun collectSnapshots(packageName: String): List<String> = buildList {
        withTimeoutOrNull(SNAPSHOT_WINDOW_MS) {
            for (i in 0 until MAX_SNAPSHOTS) {
                val snap = awaitSnapshot(packageName) ?: return@withTimeoutOrNull
                add(snap)
                delay(BETWEEN_SNAPSHOTS_MS)
            }
        }
    }

    private suspend fun awaitSnapshot(packageName: String): String? = suspendCancellableCoroutine { cont ->
        val sub = object : AnyfnAccessibilityService.Subscriber {
            override fun onWindowSnapshot(pkg: String, root: android.view.accessibility.AccessibilityNodeInfo) {
                if (pkg != packageName) return
                AnyfnAccessibilityService.unsubscribe(this)
                val tree = extractor.extract(root, screenLabel = "snapshot")
                if (cont.isActive) cont.resume(extractor.toJson(tree))
            }
        }
        AnyfnAccessibilityService.subscribe(sub)
        cont.invokeOnCancellation { AnyfnAccessibilityService.unsubscribe(sub) }
        // Also accept an immediate root if one is already present.
        AnyfnAccessibilityService.rootNode()?.let { root ->
            if (root.packageName?.toString() == packageName) {
                AnyfnAccessibilityService.unsubscribe(sub)
                val tree = extractor.extract(root, screenLabel = "current")
                if (cont.isActive) cont.resume(extractor.toJson(tree))
            }
        }
    }

    companion object {
        private const val TAG = "anyfn.infer"
        private const val LAUNCH_SETTLE_MS: Long = 2_500L
        private const val SNAPSHOT_WINDOW_MS: Long = 15_000L
        private const val BETWEEN_SNAPSHOTS_MS: Long = 1_500L
        private const val MAX_SNAPSHOTS: Int = 3

        @Suppress("unused")
        private const val FOREGROUND_FLAG: Int = AccessibilityService.GLOBAL_ACTION_HOME
    }
}
