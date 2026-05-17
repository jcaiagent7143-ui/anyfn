/**
 * UIAutomatorBridge
 *
 * v0.1 implementation note: UI Automator's `UiDevice.getInstance(Instrumentation)`
 * requires an `Instrumentation` handle that only exists in androidTest source —
 * not in normal app code. The legitimate way to drive UI from an
 * AccessibilityService is `AccessibilityNodeInfo.performAction(...)` and
 * `AccessibilityService.dispatchGesture(...)`.
 *
 * This bridge therefore delegates click/typing/scroll/key actions to the
 * Accessibility framework. We keep the class name "UIAutomatorBridge" for
 * caller continuity; the architecture stays correct if we add UI Automator
 * back in a future test-only flavour.
 */
package dev.anyfn.invoker

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import dev.anyfn.accessibility.AnyfnAccessibilityService
import dev.anyfn.core.model.ScrollDirection
import dev.anyfn.core.model.Selector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UIAutomatorBridge @Inject constructor(
    private val a11y: AccessibilityBridge,
) {

    fun launch(packageName: String, context: Context) {
        val launch = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: error("No launch intent for $packageName")
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
    }

    fun pressBack(): Boolean = AnyfnAccessibilityService.performGlobalAction(
        android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK,
    )

    fun pressHome(): Boolean = AnyfnAccessibilityService.performGlobalAction(
        android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME,
    )

    /** Best-effort: most apps treat "type then ACTION_CLICK on send" as enter. */
    fun pressEnter(): Boolean = false

    fun find(selector: Selector, timeoutMs: Long = 5_000L): AccessibilityNodeInfo? =
        a11y.findFirst(selector)

    fun click(selector: Selector, timeoutMs: Long = 5_000L): Boolean {
        val node = find(selector, timeoutMs) ?: return false
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun typeText(selector: Selector, text: String, timeoutMs: Long = 5_000L): Boolean {
        val node = find(selector, timeoutMs) ?: return false
        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text,
            )
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    fun scroll(direction: ScrollDirection, steps: Int = 6): Boolean {
        val root = a11y.root() ?: return false
        val scrollable = findScrollable(root) ?: return false
        val action = when (direction) {
            ScrollDirection.DOWN, ScrollDirection.RIGHT -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            ScrollDirection.UP, ScrollDirection.LEFT -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        }
        return scrollable.performAction(action)
    }

    fun waitForIdle(timeoutMs: Long = 3_000L) {
        // Accessibility events drive idle detection in [StateWaiter]; no-op here.
    }

    fun currentPackage(): String? = a11y.root()?.packageName?.toString()

    fun deviceInfo(): String = "anyfn a11y-bridge on android ${Build.VERSION.SDK_INT}"

    private fun findScrollable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val match = findScrollable(child)
            if (match != null) return match
        }
        return null
    }
}
