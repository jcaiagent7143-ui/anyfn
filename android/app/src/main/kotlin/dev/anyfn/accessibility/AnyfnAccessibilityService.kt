/**
 * AnyfnAccessibilityService
 *
 * The eyes of anyfn. Two distinct responsibilities:
 *
 *  1. **Scan time** — when a scan is in flight, snapshot the root window's
 *     [AccessibilityNodeInfo] tree on `TYPE_WINDOW_STATE_CHANGED` events for
 *     the package currently being scanned, and hand the snapshot to whichever
 *     [Subscriber] is registered.
 *
 *  2. **Invocation time** — provide a read-only handle the Invoker can use to
 *     read element state, detect popups, and capture results. Actions
 *     themselves go through UI Automator; we only fall back to Accessibility
 *     gestures when UIA cannot reach a node (e.g. WebView-only screens).
 *
 * The service is intentionally passive: it does not autonomously click,
 * navigate, or send events. All gestures are explicit calls from the Invoker.
 */
package dev.anyfn.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

class AnyfnAccessibilityService : AccessibilityService() {

    fun interface Subscriber {
        fun onWindowSnapshot(packageName: String, root: AccessibilityNodeInfo)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance.set(this)
        Log.i(TAG, "anyfn accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val ev = event ?: return
        val pkg = ev.packageName?.toString() ?: return
        if (ev.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            ev.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) return
        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString() != pkg) return
        subscribers.forEach { sub ->
            runCatching { sub.onWindowSnapshot(pkg, root) }
                .onFailure { Log.w(TAG, "subscriber threw", it) }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "accessibility service interrupted")
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance.compareAndSet(this, null)
        return super.onUnbind(intent)
    }

    companion object {
        private const val TAG = "anyfn.a11y"
        private val instance = AtomicReference<AnyfnAccessibilityService?>(null)
        private val subscribers = CopyOnWriteArrayList<Subscriber>()

        fun isConnected(): Boolean = instance.get() != null

        fun rootNode(): AccessibilityNodeInfo? = instance.get()?.rootInActiveWindow

        /** Dispatches a global action (BACK, HOME, RECENTS, …) on the live service, if connected. */
        fun performGlobalAction(action: Int): Boolean =
            instance.get()?.performGlobalAction(action) ?: false

        fun subscribe(subscriber: Subscriber) {
            subscribers.add(subscriber)
        }

        fun unsubscribe(subscriber: Subscriber) {
            subscribers.remove(subscriber)
        }
    }
}
