/**
 * ResultCapturer
 *
 * After a function's ui_path completes, we capture the visible text on
 * screen and ship it back to the agent as the function's structured result.
 *
 * For lists (e.g. "search results") we walk the topmost scrollable container
 * and emit one entry per direct child. For single-item screens we return
 * the combined text summary.
 */
package dev.anyfn.invoker

import android.view.accessibility.AccessibilityNodeInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResultCapturer @Inject constructor(private val a11y: AccessibilityBridge) {

    fun captureSummary(): String {
        val root = a11y.root() ?: return ""
        val list = firstScrollableList(root)
        return if (list != null) summariseList(list) else a11y.visibleTextSummary(maxChars = 2_048)
    }

    private fun firstScrollableList(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable && node.childCount > 1) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val match = firstScrollableList(child)
            if (match != null) return match
        }
        return null
    }

    private fun summariseList(list: AccessibilityNodeInfo): String = buildString {
        val items = (0 until list.childCount.coerceAtMost(MAX_ITEMS))
            .mapNotNull { list.getChild(it) }
        items.forEachIndexed { idx, item ->
            val out = StringBuilder()
            collectShortText(item, out)
            if (out.isNotEmpty()) {
                append(idx + 1).append(". ")
                append(out.toString().lineSequence().joinToString(" ").trim().take(160))
                append('\n')
            }
        }
    }

    private fun collectShortText(node: AccessibilityNodeInfo, out: StringBuilder, depth: Int = 0) {
        if (depth > 6) return
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { out.append(it).append(' ') }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { out.append(it).append(' ') }
        for (i in 0 until node.childCount) collectShortText(node.getChild(i) ?: continue, out, depth + 1)
    }

    private companion object {
        const val MAX_ITEMS = 10
    }
}
