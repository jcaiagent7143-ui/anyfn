/**
 * AccessibilityBridge
 *
 * Read-only counterpart to [UIAutomatorBridge]. Used for:
 *   - sampling the live UI tree to detect popups, secure screens, login walls
 *   - capturing the post-invocation state to return to the agent
 *   - falling back to a node when UI Automator's view of the world disagrees
 *     with what Accessibility sees (e.g. WebView-heavy screens)
 */
package dev.anyfn.invoker

import android.view.accessibility.AccessibilityNodeInfo
import dev.anyfn.accessibility.AnyfnAccessibilityService
import dev.anyfn.core.model.Selector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessibilityBridge @Inject constructor() {

    fun isConnected(): Boolean = AnyfnAccessibilityService.isConnected()

    fun root(): AccessibilityNodeInfo? = AnyfnAccessibilityService.rootNode()

    fun findFirst(selector: Selector): AccessibilityNodeInfo? = root()?.let { walk(it, selector) }

    fun visibleTextSummary(maxChars: Int = 1024): String {
        val r = root() ?: return ""
        val out = StringBuilder()
        collectText(r, out, maxChars)
        return out.toString()
    }

    fun detectSecureScreen(): Boolean {
        val r = root() ?: return false
        // Heuristic: an entirely empty tree often means FLAG_SECURE is set.
        return r.childCount == 0 && r.text.isNullOrBlank() && r.contentDescription.isNullOrBlank()
    }

    private fun walk(node: AccessibilityNodeInfo, selector: Selector): AccessibilityNodeInfo? {
        if (matches(node, selector)) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val match = walk(child, selector)
            if (match != null) return match
        }
        return null
    }

    private fun matches(node: AccessibilityNodeInfo, selector: Selector): Boolean {
        if (selector.byResourceId != null && node.viewIdResourceName == selector.byResourceId) return true
        if (selector.byText != null && node.text?.toString() == selector.byText) return true
        if (selector.byContentDescription != null && node.contentDescription?.toString() == selector.byContentDescription) return true
        if (selector.byClassName != null && node.className?.toString() == selector.byClassName) return true
        return false
    }

    private fun collectText(node: AccessibilityNodeInfo, out: StringBuilder, max: Int) {
        if (out.length >= max) return
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { out.append(it).append('\n') }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { out.append(it).append('\n') }
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i) ?: continue, out, max)
            if (out.length >= max) return
        }
    }
}
