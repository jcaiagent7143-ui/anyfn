/**
 * UITreeExtractor — converts the live [AccessibilityNodeInfo] tree into a
 * compact JSON representation suitable for sending to an LLM.
 *
 * Compaction rules (kept in lockstep with `prompts/ui_tree_compaction.md`):
 *  - Drop nodes that are invisible, non-interactive, and have no text.
 *  - Collapse text-only leaves into the parent when they're the only child.
 *  - Cap depth at [MAX_DEPTH] and child count at [MAX_CHILDREN] per node.
 *  - Truncate text fields at [MAX_TEXT_LEN] characters with an ellipsis.
 *
 * The goal is to keep prompts under ~4 KB while preserving enough signal
 * for the inferer to recognise buttons, search boxes, lists, and tabs.
 */
package dev.anyfn.scanner

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class UITreeExtractor @Inject constructor(private val json: Json) {

    fun extract(root: AccessibilityNodeInfo, screenLabel: String = "main"): UiTree {
        val nodes = walk(root, depth = 0)
        return UiTree(
            screen = screenLabel,
            rootPackage = root.packageName?.toString().orEmpty(),
            node = nodes ?: UiNode("Root", emptyList()),
        )
    }

    fun toJson(tree: UiTree): String = json.encodeToString(tree)

    private fun walk(node: AccessibilityNodeInfo?, depth: Int): UiNode? {
        if (node == null || depth > MAX_DEPTH) return null
        if (!node.isVisibleToUser && !node.isClickable && node.text.isNullOrBlank() &&
            node.contentDescription.isNullOrBlank()
        ) {
            // pure layout node with no signal — descend through children only
            val children = collectChildren(node, depth)
            return if (children.isEmpty()) null else UiNode("Group", children)
        }

        val type = simplifyClassName(node.className?.toString() ?: "View")
        val text = node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.truncate()
        val desc = node.contentDescription?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.truncate()
        val id = node.viewIdResourceName?.substringAfterLast('/')

        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val children = collectChildren(node, depth)

        return UiNode(
            type = type,
            children = children,
            id = id,
            text = text,
            contentDescription = desc,
            clickable = node.isClickable.takeIf { it },
            editable = node.isEditable.takeIf { it },
            scrollable = node.isScrollable.takeIf { it },
            bounds = "${bounds.left},${bounds.top},${bounds.right},${bounds.bottom}",
        )
    }

    private fun collectChildren(node: AccessibilityNodeInfo, depth: Int): List<UiNode> {
        val count = node.childCount.coerceAtMost(MAX_CHILDREN)
        return (0 until count).mapNotNull { walk(node.getChild(it), depth + 1) }
    }

    private fun simplifyClassName(fq: String): String = fq.substringAfterLast('.')

    private fun String.truncate(limit: Int = MAX_TEXT_LEN): String =
        if (length <= limit) this else take(limit - 1) + "…"

    companion object {
        const val MAX_DEPTH: Int = 14
        const val MAX_CHILDREN: Int = 24
        const val MAX_TEXT_LEN: Int = 80
    }
}

@Serializable
data class UiTree(
    val screen: String,
    val rootPackage: String,
    val node: UiNode,
)

@Serializable
data class UiNode(
    val type: String,
    val children: List<UiNode> = emptyList(),
    val id: String? = null,
    val text: String? = null,
    val contentDescription: String? = null,
    val clickable: Boolean? = null,
    val editable: Boolean? = null,
    val scrollable: Boolean? = null,
    val bounds: String? = null,
)
